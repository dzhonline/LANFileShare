// MyHttpServer.java
package com.dzh.lanfileshare;

import android.os.Environment;

import java.io.*;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class MyHttpServer extends NanoHTTPD {

    private final File rootDir;

    public MyHttpServer(int port) {
        super(port);
        rootDir = Environment.getExternalStorageDirectory();

        File shareDir = new File(rootDir, "Download/LANFileShare");
        if (!shareDir.exists()) {
            shareDir.mkdirs();
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        Map<String, String> params = session.getParms();
        String uri = session.getUri();

        try {
            if (uri.equals("/upload") && method == Method.POST) {
                return handleUpload(session);
            } else if (uri.equals("/mkdir") && method == Method.POST) {
                return handleMkdir(session);
            } else if (uri.startsWith("/download")) {
                return downloadFile(params);
            } else if (uri.startsWith("/delete")) {
                return deleteFile(params);
            }
        } catch (Exception e) {
            return newFixedLengthResponse("❌ 操作失败：" + e.getMessage());
        }

        return newFixedLengthResponse(showFileBrowser(params));
    }

    private Response downloadFile(Map<String, String> params) {
        try {
            String path = URLDecoder.decode(params.getOrDefault("path", ""), "UTF-8");
            File file = new File(rootDir, path);
            if (!file.exists()) return newFixedLengthResponse("❌ 文件不存在");

            InputStream in = new FileInputStream(file);
            String mimeType = URLConnection.guessContentTypeFromName(file.getName());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            String encodedFileName = URLEncoder.encode(file.getName(), "UTF-8").replace("+", "%20");
            String contentDisposition = "attachment; filename=\"" + file.getName() + "\"; filename*=UTF-8''" + encodedFileName;

            LogHelper.addLog("✅ 下载文件：" + path);
            Response res = newChunkedResponse(Response.Status.OK, mimeType, in);
            res.addHeader("Content-Disposition", contentDisposition);
            return res;

        } catch (Exception e) {
            return newFixedLengthResponse("❌ 下载失败：" + e.getMessage());
        }
    }

    private Response deleteFile(Map<String, String> params) {
        try {
            String path = URLDecoder.decode(params.getOrDefault("path", ""), "UTF-8");

            File file = new File(rootDir, path);
            if (file.exists()) {
                boolean deleted = deleteRecursively(file);
                if (deleted) {
                    LogHelper.addLog("✅ 删除成功：" + path);
                    return newFixedLengthResponse("✅ 删除成功");
                } else {
                    LogHelper.addLog("❌ 删除失败: " + file.getAbsolutePath());
                    LogHelper.addLog("📄 文件是否存在：" + file.exists());
                    LogHelper.addLog("📄 是否是文件：" + file.isFile());
                    LogHelper.addLog("📁 是否是文件夹：" + file.isDirectory());
                    LogHelper.addLog("🔒 是否可写：" + file.canWrite());
                    LogHelper.addLog("🔒 是否可读：" + file.canRead());
                    return newFixedLengthResponse("❌ 删除失败！");
                }
            } else {
                return newFixedLengthResponse("❌ 文件不存在");
            }
        } catch (Exception e) {
            return newFixedLengthResponse("❌ 删除出错：" + e.getMessage());
        }
    }

    private boolean deleteRecursively(File f) {
        if (f.isDirectory()) {
            File[] subs = f.listFiles();
            if (subs != null) {
                for (File s : subs) {
                    deleteRecursively(s);
                }
            }
        }
        return f.delete();
    }

    private Response handleUpload(IHTTPSession session) {
        try {
            Map<String, String> files = new HashMap<>();
            session.parseBody(files);

            String targetPath = URLDecoder.decode(session.getParms().getOrDefault("path", ""), "UTF-8");
            String fileName = session.getParms().get("file");
            String tempPath = files.get("file");

            if (fileName == null || fileName.trim().isEmpty()) {
                return newFixedLengthResponse("❌ 未选择文件");
            }

            File targetDir = new File(rootDir, targetPath);
            if (!targetDir.exists()) targetDir.mkdirs();

            File temp = new File(tempPath);
            File outFile = new File(targetDir, fileName);
            boolean copied = copyFile(temp, outFile);
            temp.delete();

            if (copied) {
                LogHelper.addLog("✅ 上传：" + targetPath + "/" + fileName);
                return newFixedLengthResponse("✅ 上传成功");
            } else {
                return newFixedLengthResponse("❌ 上传失败");
            }

        } catch (Exception e) {
            return newFixedLengthResponse("❌ 异常：" + e.getMessage());
        }
    }

    private Response handleMkdir(IHTTPSession session) {
        try {
            session.parseBody(new HashMap<>());

            String folderName = session.getParms().get("foldername");
            String relative = URLDecoder.decode(session.getParms().getOrDefault("path", ""), "UTF-8");

            if (folderName == null || folderName.trim().isEmpty()) {
                return newFixedLengthResponse("❌ 文件夹名不能为空");
            }

            File base = new File(rootDir, relative);
            File dir = new File(base, folderName);
            if (dir.exists()) return newFixedLengthResponse("❌ 已存在");

            if (dir.mkdirs()) {
                LogHelper.addLog("📁 创建目录：" + dir.getAbsolutePath());
                return newFixedLengthResponse("✅ 文件夹创建成功");
            } else {
                return newFixedLengthResponse("❌ 创建失败");
            }

        } catch (Exception e) {
            return newFixedLengthResponse("❌ 异常：" + e.getMessage());
        }
    }

    private boolean copyFile(File src, File dst) {
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dst)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            return true;
        } catch (IOException e) {
            LogHelper.addLog("❌ 文件复制失败：" + e.getMessage());
            return false;
        }
    }

    public String showFileBrowser(Map<String, String> params) {
        String path;
        try {
            path = URLDecoder.decode(params.getOrDefault("path", ""), "UTF-8");
        } catch (Exception e) {
            path = "Download/LANFileShare";
        }

        File currentDir = new File(rootDir, path);
        if (!currentDir.exists()) currentDir.mkdirs();

        long total = currentDir.getTotalSpace();
        long free = currentDir.getUsableSpace();
        String totalStr = formatSize(total);
        String freeStr = formatSize(free);

        StringBuilder fileList = new StringBuilder();

        try {
            if (!currentDir.getCanonicalPath().equals(rootDir.getCanonicalPath())) {
                String parentPath = currentDir.getCanonicalPath()
                        .replace(rootDir.getCanonicalPath(), "")
                        .replaceAll("/[^/]+$", "")
                        .replaceFirst("^/", "");
                fileList.append(String.format("""
                        <li class="file-item">
                            <span class="name"><a href="/?path=%s">⬅ 返回上一级</a></span>
                            <span class="btns"></span>
                        </li>
                        """, URLEncoder.encode(parentPath, "UTF-8")));
            }
        } catch (IOException ignored) {
        }

        File[] files = currentDir.listFiles();
        if (files != null && files.length > 0) {
            Arrays.sort(files, (f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().compareToIgnoreCase(f2.getName());
            });

            for (File f : files) {
                if (!f.exists()) continue;
                // 取消注释以下行可过滤隐藏文件：
                // if (f.getName().startsWith(".")) continue;

                String name = f.getName();
                String encodedPath = URLEncoder.encode(path + "/" + name);

                if (f.isDirectory()) {
                    fileList.append(String.format("""
                            <li class="file-item">
                                <span class="name">📁 <a href="/?path=%s">%s</a></span>
                                <span class="btns">
                                    <a href="javascript:void(0);" onclick="deleteFile('%s')">🗑 删除</a>
                                </span>
                            </li>
                            """, encodedPath, name, encodedPath));
                } else {
                    // 判断是否是图片文件
                    String lowerName = name.toLowerCase();
                    boolean isImage = lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")
                            || lowerName.endsWith(".png") || lowerName.endsWith(".gif")
                            || lowerName.endsWith(".bmp") || lowerName.endsWith(".webp");

                    if (isImage) {
                        // 👍 图片文件展示缩略图
                        fileList.append(String.format("""
                                <li class="file-item">
                                    <span class="name">
                                        🖼️ <img src="/download?path=%s" alt="%s" style="max-height:60px; max-width:80px; vertical-align:middle; margin-right:8px;">
                                        %s
                                    </span>
                                    <span class="btns">
                                        <a href="/download?path=%s">📥 下载</a>
                                        <a href="javascript:void(0);" onclick="deleteFile('%s')">🗑 删除</a>
                                    </span>
                                </li>
                                """, encodedPath, name, name, encodedPath, encodedPath));
                    } else {
                        // 非图片仍按默认方式展示
                        fileList.append(String.format("""
                                <li class="file-item">
                                    <span class="name">📄 %s</span>
                                    <span class="btns">
                                        <a href="/download?path=%s">📥 下载</a>
                                        <a href="javascript:void(0);" onclick="deleteFile('%s')">🗑 删除</a>
                                    </span>
                                </li>
                                """, name, encodedPath, encodedPath));
                    }
                }
            }
        } else {
            fileList.append("""
                    <li class="file-item">
                        <span class="name">📭 当前目录为空</span>
                        <span class="btns"></span>
                    </li>
                    """);
        }

        return String.format("""
                <html>
                <head>
                    <meta charset="utf-8">
                    <title>LAN 文件共享</title>
                    <style>
                        ul.file-list { list-style: none; padding: 0; margin: 0; }
                        li.file-item {
                            font-size: 15px; padding: 6px 4px;
                            border-bottom: 1px solid #eee;
                            display: flex; align-items: center;
                            justify-content: space-between;
                            max-width: 800px; margin: 0 auto 4px auto;
                        }
                        .file-item .name {
                            flex-grow: 1; white-space: nowrap;
                            overflow: hidden; text-overflow: ellipsis;
                            margin-right: 24px;
                        }
                        .file-item .btns {
                            display: flex; gap: 12px;
                        }
                        .file-item .btns a {
                            text-decoration: none; color: #0366d6;
                            padding: 4px 8px; border: 1px solid #ccc;
                            border-radius: 4px; font-size: 14px;
                        }
                        .file-item .btns a:hover {
                            background-color: #f0f0f0;
                        }
                        #progressWrapper { margin-top: 10px; display: none; font-size: 14px; }
                    </style>
                    <script>
                        function createFolder() {
                            let name = prompt("📁 请输入文件夹名称");
                            if (!name || name.trim() === '') {
                                alert("❌ 文件夹名不能为空");
                                return;
                            }
                            const form = document.createElement("form");
                            form.method = "POST";
                            form.action = "/mkdir";
                            form.innerHTML = `
                                <input type="hidden" name="foldername" value="${name}"/>
                                <input type="hidden" name="path" value="%s"/>
                            `;
                            document.body.appendChild(form);
                            form.submit();
                        }
                
                        function deleteFile(path) {
                            if (!confirm("⚠️ 确定删除该文件/文件夹？")) return;
                            fetch("/delete?path=" + encodeURIComponent(path))
                                .then(r => r.text()).then(txt => {
                                    alert(txt); location.reload();
                                }).catch(e => alert("❌ 删除失败：" + e));
                        }
                
                        window.onload = function () {
                            const uploadForm = document.getElementById("uploadForm");
                            const fileInput = document.getElementById("fileInput");
                            const pathInput = document.getElementById("pathInput");
                            const progressWrapper = document.getElementById("progressWrapper");
                            const progressBar = document.getElementById("uploadProgress");
                            const progressText = document.getElementById("progressText");
                            const speedText = document.getElementById("speedText");
                
                            uploadForm.addEventListener("submit", function(e) {
                                e.preventDefault();
                
                                if (!fileInput.files.length) {
                                    alert("请选择文件");
                                    return;
                                }
                
                                const file = fileInput.files[0];
                                const formData = new FormData();
                                formData.append("file", file);
                                formData.append("path", pathInput.value);
                
                                const xhr = new XMLHttpRequest();
                                xhr.open("POST", "/upload", true);
                
                                let lastTime = Date.now();
                                let lastLoaded = 0;
                
                                progressWrapper.style.display = "block";
                
                                xhr.upload.onprogress = function(e) {
                                    if (e.lengthComputable) {
                                        const percent = Math.round((e.loaded / e.total) * 100);
                                        progressBar.value = percent;
                                        progressText.textContent = percent + "%%";
                
                                        const now = Date.now();
                                        const timeDiff = (now - lastTime) / 1000;
                                        const loadedDiff = e.loaded - lastLoaded;
                
                                        if (timeDiff > 0) {
                                            const speed = loadedDiff / timeDiff;
                                            speedText.textContent = "📶 速度: " + formatBytes(speed) + "/s";
                                            lastTime = now;
                                            lastLoaded = e.loaded;
                                        }
                                    }
                                };
                
                                xhr.onload = function () {
                                    if (xhr.status === 200) {
                                        alert("✅ 上传完成");
                                        location.reload();
                                    } else {
                                        alert("❌ 上传失败");
                                    }
                                };
                                xhr.onerror = function () {
                                    alert("❌ 上传出错");
                                };
                                xhr.send(formData);
                            });
                
                            function formatBytes(bytes) {
                                if (bytes >= 1024 * 1024) {
                                    return (bytes / (1024 * 1024)).toFixed(1) + " MB";
                                } else {
                                    return (bytes / 1024).toFixed(1) + " KB";
                                }
                            }
                        };
                    </script>
                </head>
                <body style="padding:24px; font-family: sans-serif;">
                    <h2>📱 LAN 文件共享</h2>
                    <p><strong>📂 当前目录：</strong> %s <strong>📦 空间：</strong> 共 %s，剩 %s</p>
                
                    <form id="uploadForm" style="margin-bottom:12px;">
                        📤 <input type="file" name="file" id="fileInput" required/>
                        <input type="hidden" id="pathInput" name="path" value="%s"/>
                        <button type="submit">上传文件</button>
                    </form>
                
                    <div id="progressWrapper">
                        ⏳ 上传进度：
                        <progress id="uploadProgress" value="0" max="100" style="width: 300px;"></progress>
                        <span id="progressText">0%%</span>
                        <span id="speedText" style="margin-left: 10px;">📶 速度: 0 KB/s</span>
                    </div>
                
                    <button onclick="createFolder()">📁 新建文件夹</button>
                    <hr/>
                    <ul class="file-list">%s</ul>
                </body>
                </html>
                """, path, currentDir.getAbsolutePath(), totalStr, freeStr, path, fileList);
    }

    private String formatSize(long size) {
        float kb = size / 1024f;
        float mb = kb / 1024f;
        float gb = mb / 1024f;
        if (gb >= 1) return String.format("%.1f GB", gb);
        else if (mb >= 1) return String.format("%.1f MB", mb);
        else return String.format("%.0f KB", kb);
    }
}