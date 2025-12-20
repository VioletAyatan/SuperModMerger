package tool;

import cn.hutool.core.io.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TestTool {

    /**
     * 获取困兽安装路径
     *
     * @return 困兽安装路径
     */
    public static StringBuilder getDltbPath() {
        var dltbFound = false;
        String steamInstallPath = getSteamInstallPath();
        if (steamInstallPath == null) {
            System.out.println("未检测到Steam安装");
            return null;
        }
        System.out.println("Steam主目录：" + steamInstallPath);
        StringBuilder dltbPath = new StringBuilder(String.join(File.separator, "steamapps", "common", "Dying Light The Beast"));
        List<String> libraryFolders = getLibraryFolders(steamInstallPath);
        for (String libraryFolder : libraryFolders) {
            if (FileUtil.exist(libraryFolder + File.separator + dltbPath)) {
                dltbPath.insert(0, libraryFolder + File.separator);
                System.out.println("找到困兽主目录：" + dltbPath);
                dltbFound = true;
            }
        }
        if (!dltbFound) {
            System.out.println("未检测到困兽安装");
            return null;
        }
        return dltbPath;
    }

    // 从注册表读取 SteamPath
    private static String getSteamInstallPath() {
        try {
            // 查询注册表命令
            Process process = Runtime.getRuntime().exec("reg query HKEY_CURRENT_USER\\Software\\Valve\\Steam /v SteamPath");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("SteamPath")) {
                    // 解析输出，例如: "    SteamPath    REG_SZ    C:/Program Files (x86)/Steam"
                    String[] parts = line.trim().split("\\s{4,}"); // 根据长空格分割
                    if (parts.length >= 3) {
                        // 将路径中的正斜杠替换为系统默认分隔符（可选）
                        return parts[2].replace("/", File.separator);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 读取并解析 VDF 文件
    private static List<String> getLibraryFolders(String steamPath) {
        List<String> paths = new ArrayList<>();
        // 默认将主目录作为一个库（以防VDF解析失败，虽然VDF里通常也会包含主目录）
        // paths.add(steamPath);

        File vdfFile = Paths.get(steamPath, "steamapps", "libraryfolders.vdf").toFile();

        if (!vdfFile.exists()) {
            System.out.println("未找到 libraryfolders.vdf");
            return paths;
        }

        try {
            String content = new String(Files.readAllBytes(vdfFile.toPath()));

            // 使用正则粗略匹配 "path" "X:\\xxx" 结构
            // 注意：VDF 格式中 Key 和 Value 都是带引号的
            Pattern pattern = Pattern.compile("\"path\"\\s+\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                String libPath = matcher.group(1);
                // VDF 中的路径通常是双反斜杠转义的，Java正则抓取后可能需要处理
                // 但通常直接读取到的就是正常路径，除了一些转义字符
                // 这里的正则取出来的是引号内的原始内容

                // 将双反斜杠转换为单反斜杠 (VDF转义)
                libPath = libPath.replace("\\\\", "\\");
                paths.add(libPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return paths;
    }

}
