/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

/**
 * Downloader for the Maven Wrapper JAR, used when it is missing.
 * This is a minimal copy of the reference implementation.
 */
public class MavenWrapperDownloader {
    private static final String WRAPPER_VERSION = "3.3.2";
    private static final String DEFAULT_DOWNLOAD_URL =
            "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/" + WRAPPER_VERSION
                    + "/maven-wrapper-" + WRAPPER_VERSION + ".jar";

    public static void main(String[] args) {
        System.out.println("- Downloading Maven Wrapper...");

        File baseDirectory = new File(args.length > 0 ? args[0] : ".");
        File wrapperJar = new File(baseDirectory, ".mvn/wrapper/maven-wrapper.jar");
        if (wrapperJar.exists()) {
            System.out.println("- Maven Wrapper JAR already exists.");
            return;
        }
        wrapperJar.getParentFile().mkdirs();

        String downloadUrl = getWrapperUrl(baseDirectory);
        System.out.println("- Downloading from: " + downloadUrl);

        try {
            downloadFileFromURL(downloadUrl, wrapperJar);
            System.out.println("- Downloaded Maven Wrapper JAR to " + wrapperJar.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("- Error downloading Maven Wrapper JAR: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String getWrapperUrl(File baseDirectory) {
        File propsFile = new File(baseDirectory, ".mvn/wrapper/maven-wrapper.properties");
        if (!propsFile.isFile()) return DEFAULT_DOWNLOAD_URL;
        Properties p = new Properties();
        try (InputStream in = java.nio.file.Files.newInputStream(propsFile.toPath())) {
            p.load(in);
        } catch (IOException ignored) {
            return DEFAULT_DOWNLOAD_URL;
        }
        String url = p.getProperty("wrapperUrl");
        return (url == null || url.isBlank()) ? DEFAULT_DOWNLOAD_URL : url.trim();
    }

    private static void downloadFileFromURL(String urlString, File destination) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "maven-wrapper-downloader");

        int status = connection.getResponseCode();
        if (status >= 300 && status < 400) {
            String location = connection.getHeaderField("Location");
            if (location != null) {
                connection.disconnect();
                downloadFileFromURL(location, destination);
                return;
            }
        }
        if (status != 200) {
            throw new IOException("HTTP " + status + " from " + urlString);
        }

        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } finally {
            connection.disconnect();
        }
    }
}
