package xml;

import org.junit.Test;

public class XMLPressTest {
    @Test
    public void testNormalizeSource() {
        String text = "<effect id=\"PerfectShimmyEnabled\" change=\"1\"/>";
        System.out.println("trim = " + normalizeXmlText(text));
    }

    private static String normalizeXmlText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        // 1. 移除标签外的空白字符（标签之间的换行、缩进等）
        String normalized = text.replaceAll(">\\s+<", "><");
        // 2. 压缩标签内的连续空格为单个空格
        normalized = normalized.replaceAll("\\s+", "");
        // 3. 移除开头和结尾的空白
        normalized = normalized.trim();
        return normalized;
    }
}
