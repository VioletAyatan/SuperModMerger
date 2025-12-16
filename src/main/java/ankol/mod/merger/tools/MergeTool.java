package ankol.mod.merger.tools;

import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MergeTool {
    /**
     * 构建文件树
     *
     * @param path 文件路径，可以是文件夹或者zip文件
     * @return 文件树MAP key是文件名
     */
    public static Map<String, FileTree> buildFileTree(Path path) {
        File file = path.toFile();
        if (!file.exists()) {
            throw new IllegalArgumentException("非法参数，路径" + path + "不存在");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException("不支持传入文件夹");
        } else if (StrUtil.endWithAny(file.getName(), ".zip", ".pak")) {
            return buildFileTreeFromZip(file);
        } else {
            throw new RuntimeException("不支持的文件格式" + file.getName());
        }
    }

    private static Map<String, FileTree> buildFileTreeFromZip(File file) {
        Map<String, FileTree> fileTreeMap = new HashMap<>();
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String entryName = zipEntry.getName();
                String fileName = getEntryFileName(entryName);
                if (!fileTreeMap.containsKey(fileName)) {
                    fileTreeMap.put(fileName, new FileTree(fileName, entryName));
                } else {
                    System.err.println("检测到相同的文件名：" + fileName + "但路径不一致：[" + entryName + "] [" + fileTreeMap.get(fileName).getFullPathName() + "]");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileTreeMap;
    }

    private static String getEntryFileName(String entryName) {
        if (StrUtil.isNotBlank(entryName)) {
            return entryName.substring(entryName.lastIndexOf("/") + 1);
        }
        return null;
    }

    static void main() {
        //原版文件中是不会出现文件名重名的情况的
        Map<String, FileTree> fileTreeMap = buildFileTree(Path.of("D:\\SteamLibrary\\steamapps\\common\\Dying Light The Beast\\ph_ft\\source\\data0.pak"));
        System.out.println("fileTreeMap = " + fileTreeMap);
    }
}
