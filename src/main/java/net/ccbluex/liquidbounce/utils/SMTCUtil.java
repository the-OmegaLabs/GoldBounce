package net.ccbluex.liquidbounce.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SMTCUtil {
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    // 存储媒体信息
    private static String currentTitle = "";
    private static String currentArtist = "";
    private static String currentAlbum = "";
    private static boolean isPlaying = false;

    // 调试信息
    private static final List<String> debugMessages = new ArrayList<>();
    private static boolean hasError = false;

    // 初始化标志
    private static boolean initialized = false;

    public static void initialize() {
        debugMessages.clear();
        addDebugMessage("系统: " + System.getProperty("os.name"));

        if (!IS_WINDOWS) {
            addDebugMessage("SMTC功能只支持Windows8/10/11");
            return;
        }

        // 检查PowerShell是否可用
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("powershell", "-command", "echo 'PowerShell Test'");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 使用UTF-8编码
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line = reader.readLine();
                addDebugMessage("[SMTC] 测试输出: " + line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                addDebugMessage("[SMTC] 初始化成功");
                initialized = true;
                updateMediaInfo();
            } else {
                addDebugMessage("[SMTC] 初始化错误: " + exitCode);
                hasError = true;
            }
        } catch (Exception e) {
            addDebugMessage("[SMTC] 测试输出失败: " + e.getMessage());
            hasError = true;
        }
    }

    public static void updateMediaInfo() {
        if (!IS_WINDOWS || !initialized) return;

        try {
            String script =
                    "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8;" +
                            "try {" +
                            "  $sessions = Get-Process | Where-Object {$_.MainWindowTitle -ne ''} | Select-Object MainWindowTitle;" +
                            "  foreach ($session in $sessions) {" +
                            "    $title = $session.MainWindowTitle;" +
                            "    if ($title -match '(.+)\\s+-\\s+(.+)\\s+-\\s+(.+)') {" +
                            "      Write-Output ('Title: ' + $matches[1].Trim());" +
                            "      Write-Output ('Artist: ' + $matches[2].Trim());" +
                            "      Write-Output ('Album: ' + $matches[3].Trim());" +
                            "      Write-Output ('Status: Playing');" +
                            "      exit 0;" +
                            "    } elseif ($title -match '(.+)\\s+-\\s+(.+)') {" +
                            "      Write-Output ('Title: ' + $matches[1].Trim());" +
                            "      Write-Output ('Artist: ' + $matches[2].Trim());" +
                            "      Write-Output ('Album: Unknown');" +
                            "      Write-Output ('Status: Playing');" +
                            "      exit 0;" +
                            "    }" +
                            "  }" +
                            "  Write-Output 'No media session found.';" +
                            "} catch {" +
                            "  Write-Output ('Error: ' + $_.Exception.Message);" +
                            "}";

            ProcessBuilder processBuilder = new ProcessBuilder("powershell", "-command", script);
            processBuilder.redirectErrorStream(true);
            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
            Process process = processBuilder.start();

            // 使用UTF-8编码
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                boolean foundSession = false;
                StringBuilder errorMessage = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Title: ")) {
                        currentTitle = line.substring(7);
                        foundSession = true;
                    } else if (line.startsWith("Artist: ")) {
                        currentArtist = line.substring(8);
                    } else if (line.startsWith("Album: ")) {
                        currentAlbum = line.substring(7);
                    } else if (line.startsWith("Status: ")) {
                        isPlaying = line.substring(8).equalsIgnoreCase("Playing");
                    } else if (line.equals("No media session found.")) {
                        // 尝试备用方法
                        tryAlternativeMethod();
                    } else if (line.startsWith("Error: ")) {
                        errorMessage.append(line.substring(7));
                    }
                }

                if (foundSession) {
                    addDebugMessage("成功获取媒体信息:");
                    addDebugMessage("标题: " + currentTitle);
                    addDebugMessage("艺术家: " + currentArtist);
                    addDebugMessage("状态: " + (isPlaying ? "播放中" : "已暂停"));
                } else if (errorMessage.length() > 0) {
                    addDebugMessage("PowerShell错误: " + errorMessage);
                    // 尝试备用方法
                    tryAlternativeMethod();
                }
            }

            process.waitFor();
        } catch (Exception e) {
            addDebugMessage("获取媒体信息失败: " + e.getMessage());
            // 尝试备用方法
            tryAlternativeMethod();
        }
    }

    private static void tryAlternativeMethod() {
        try {
            addDebugMessage("尝试备用方法获取媒体信息...");

            String script =
                    "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8;" +
                            "try {" +
                            "  $wshShell = New-Object -ComObject WScript.Shell;" +
                            "  $activeWindow = $wshShell.AppActivate('Windows Media Player') -or " +
                            "                  $wshShell.AppActivate('Spotify') -or " +
                            "                  $wshShell.AppActivate('iTunes') -or " +
                            "                  $wshShell.AppActivate('Groove') -or " +
                            "                  $wshShell.AppActivate('Music');" +
                            "  if ($activeWindow) {" +
                            "    $title = $wshShell.SendKeys('%{Tab}');" +
                            "    Start-Sleep -Milliseconds 100;" +
                            "    $activeTitle = (Get-Process | Where-Object {$_.MainWindowTitle -ne '' -and ($_.ProcessName -eq 'wmplayer' -or $_.ProcessName -eq 'Spotify' -or $_.ProcessName -eq 'iTunes' -or $_.ProcessName -eq 'Music')}).MainWindowTitle;" +
                            "    if ($activeTitle -match '(.+)\\s+-\\s+(.+)') {" +
                            "      Write-Output ('Title: ' + $matches[1].Trim());" +
                            "      Write-Output ('Artist: ' + $matches[2].Trim());" +
                            "      Write-Output ('Status: Playing');" +
                            "    } else {" +
                            "      Write-Output ('Title: ' + $activeTitle);" +
                            "      Write-Output ('Status: Playing');" +
                            "    }" +
                            "  } else {" +
                            "    Write-Output 'No media player found.';" +
                            "  }" +
                            "} catch {" +
                            "  Write-Output ('Error: ' + $_.Exception.Message);" +
                            "}";

            ProcessBuilder processBuilder = new ProcessBuilder("powershell", "-command", script);
            processBuilder.redirectErrorStream(true);
            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                boolean foundSession = false;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Title: ")) {
                        currentTitle = line.substring(7);
                        foundSession = true;
                    } else if (line.startsWith("Artist: ")) {
                        currentArtist = line.substring(8);
                    } else if (line.startsWith("Status: ")) {
                        isPlaying = line.substring(8).equalsIgnoreCase("Playing");
                    } else if (line.equals("No media player found.")) {
                        addDebugMessage("备用方法: 没有找到媒体播放器");
                    }
                }

                if (foundSession) {
                    addDebugMessage("备用方法: 成功获取媒体信息");
                    addDebugMessage("标题: " + currentTitle);
                    addDebugMessage("艺术家: " + currentArtist);
                }
            }

            process.waitFor();
        } catch (Exception e) {
            addDebugMessage("备用方法失败: " + e.getMessage());
        }
    }

    private static void addDebugMessage(String message) {
        debugMessages.add("[SMTC] " + message);
        // 限制调试消息数量，防止内存泄漏
        if (debugMessages.size() > 20) {
            debugMessages.remove(0);
        }
        System.out.println("[SMTC Debug] " + message);
    }

    public static String getCurrentTitle() {
        return currentTitle != null ? currentTitle : "";
    }

    public static String getCurrentArtist() {
        return currentArtist != null ? currentArtist : "";
    }

    public static String getCurrentAlbum() {
        return currentAlbum != null ? currentAlbum : "";
    }

    public static boolean isPlaying() {
        return isPlaying;
    }

    public static boolean hasMediaInfo() {
        return !getCurrentTitle().isEmpty() || !getCurrentArtist().isEmpty() || !getCurrentAlbum().isEmpty();
    }

    public static List<String> getDebugMessages() {
        return debugMessages;
    }

    public static boolean hasError() {
        return hasError;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    // 添加一个测试方法，用于手动设置媒体信息（调试用）
    public static void setTestData() {
        currentTitle = "测试歌曲";
        currentArtist = "测试艺术家";
        currentAlbum = "测试专辑";
        isPlaying = true;
        addDebugMessage("已设置测试数据");
    }
}
