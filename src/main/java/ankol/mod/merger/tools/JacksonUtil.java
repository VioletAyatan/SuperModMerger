package ankol.mod.merger.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Jackson工具类
 * <p>
 * Jackson是一个流行的Json库，这个工具封装了一些简单的方法实现常用的json转换
 *
 * @author lck
 */
public abstract class JacksonUtil {
    private static ObjectMapper objectMapper;

    static {
        objectMapper = JsonMapper.builder()
                // 用于确定遇到未知属性时，是否应导致失败（抛出 JsonMappingException）
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // 属性序列化设置为大小写不敏感
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .build();
    }

    /**
     * 获取工具类使用的ObjectMapper
     *
     * @return {@link ObjectMapper}
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * 设置工具类使用的{@link ObjectMapper}
     * <p>
     * 默认情况下不需要修改，但如果你想替换工具类默认使用的{@link ObjectMapper}，你可以使用本方法进行替换
     *
     * @param objectMapper jackson的{@link ObjectMapper}
     */
    public static void setObjectMapper(ObjectMapper objectMapper) {
        JacksonUtil.objectMapper = objectMapper;
    }

    /**
     * 将对象序列化为 JSON 字符串格式。
     * <p>
     * 该方法适用于将对象转换为 JSON 字符串，以便在应用程序中进一步处理或输出。
     * <p>
     *
     * @param obj 要序列化为 JSON 字符串的对象。
     * @return 表示该对象的 JSON 字符串。
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将对象序列化为 JSON 格式并直接写入指定的 {@link OutputStream}
     * <p>
     * 该方法适用于将 JSON 数据流式传输到外部目标（如文件、网络）。
     *
     * @param obj          要序列化为 JSON 格式的对象。
     * @param outputStream 用于写入 JSON 数据的 OutputStream。
     */
    public static void toJson(Object obj, OutputStream outputStream) {
        try {
            objectMapper.writeValue(outputStream, obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Json转对象
     *
     * @param json   json字符串
     * @param target 对象class
     * @param <T>    目标类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> target) {
        try {
            return objectMapper.readValue(json, target);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Json转对象
     *
     * @param bytes  byte数组
     * @param target 对象class
     * @param <T>    目标类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(byte[] bytes, Class<T> target) {
        try {
            return objectMapper.readValue(bytes, target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Json转对象
     *
     * @param inputStream 输入流
     * @param target      对象class
     * @param <T>         目标类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(InputStream inputStream, Class<T> target) {
        try {
            return objectMapper.readValue(inputStream, target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Json转对象
     * <p>
     * 对于带泛型的对象请使用此方法，使用{@link TypeReference}传入明确的类型
     * <p>
     * Java示例：
     * <pre>{@code
     * //创建TypeReference
     * new TypeReference<List<String>>(){}
     * }</pre>
     * Kotlin示例：
     * <pre>{@code
     * //使用更加简单的包装函数
     * val type = jacksonTypeRef<List<String>>()
     * }</pre>
     *
     * @param inputStream   json输入流
     * @param typeReference 类型引用，参考：{@link TypeReference}
     * @param <T>           目标类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(InputStream inputStream, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Json转对象
     * <p>
     * 对于带泛型的对象请使用此方法，使用{@link TypeReference}传入明确的类型
     * <p>
     * Java示例：
     * <pre>{@code
     * //创建TypeReference
     * new TypeReference<List<String>>(){}
     * }</pre>
     * Kotlin示例：
     * <pre>{@code
     * //使用更加简单的包装函数
     * val type = jacksonTypeRef<List<String>>()
     * }</pre>
     *
     * @param json          json字符串
     * @param typeReference 类型引用，参考：{@link TypeReference}
     * @param <T>           目标类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断字符串是否是一个JSON格式
     *
     * @param json 字符串
     * @return {@link Boolean} json格式是否合法
     */
    public static boolean isJson(String json) {
        try {
            if (json != null && !json.isEmpty()) {
                objectMapper.readTree(json);
                return true;
            }
            return false;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * Json转为 {@link JsonNode}
     *
     * @param json JSON字符串
     * @return {@link JsonNode}
     */
    public static JsonNode toTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * javaBean转成JsonTree
     *
     * @param obj 要转换的对象
     * @param <T> JsonNode类型
     * @return {@link JsonNode}
     */
    public static <T extends JsonNode> T toTree(Object obj) {
        return objectMapper.valueToTree(obj);
    }

    /**
     * 读取输入流中的内容转换为{@link JsonNode}
     *
     * @param inputStream 输入流
     * @return {@link JsonNode}
     */
    public static JsonNode toTree(InputStream inputStream) {
        try {
            return objectMapper.readTree(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}