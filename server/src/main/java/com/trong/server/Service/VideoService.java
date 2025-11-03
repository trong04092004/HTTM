package com.trong.server.Service;

import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import java.nio.file.Paths;

public class VideoService {

    private static final String ML_SERVER_URL = "http://127.0.0.1:5000/detect";
    private static final String STORAGE_DIRECTORY = "video_storage";

    public File saveUploadedFile(HttpExchange exchange) throws Exception {

        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        HttpExchangeRequestContext requestContext = new HttpExchangeRequestContext(exchange);
        List<FileItem> items = upload.parseRequest(requestContext);

        for (FileItem item : items) {
            if (!item.isFormField()) {

                File uploadDir = new File(STORAGE_DIRECTORY);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                String originalFileName = Paths.get(item.getName()).getFileName().toString();
                File savedFile = new File(uploadDir, originalFileName);
                item.write(savedFile);

                System.out.println("Đã lưu file hợp lệ tại: " + savedFile.getAbsolutePath());
                item.delete();
                return savedFile;
            }
        }

        throw new IOException("Không tìm thấy file video nào trong request.");
    }
    public String sendToMLServer(File videoFile) throws IOException {
        String boundary = "----Boundary" + System.currentTimeMillis();
        HttpURLConnection connection = (HttpURLConnection) new URL(ML_SERVER_URL).openConnection();

        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (DataOutputStream out = new DataOutputStream(connection.getOutputStream());
             FileInputStream fileIn = new FileInputStream(videoFile)) {

            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"video\"; filename=\"" + videoFile.getName() + "\"\r\n");
            out.writeBytes("Content-Type: video/mp4\r\n\r\n");

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            out.writeBytes("\r\n--" + boundary + "--\r\n");
            out.flush();
        }
        int responseCode = connection.getResponseCode();
        InputStream responseStream = (responseCode == 200)
                ? connection.getInputStream()
                : connection.getErrorStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        connection.disconnect();
        return response.toString();
    }
    class HttpExchangeRequestContext implements RequestContext {
        private final HttpExchange exchange;

        public HttpExchangeRequestContext(HttpExchange exchange) {
            this.exchange = exchange;
        }

        @Override
        public String getCharacterEncoding() {
            return "UTF-8";
        }

        @Override
        public String getContentType() {
            return exchange.getRequestHeaders().getFirst("Content-Type");
        }

        @Override
        public int getContentLength() {
            String contentLength = exchange.getRequestHeaders().getFirst("Content-Length");
            if (contentLength != null) {
                try {
                    return Integer.parseInt(contentLength);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
            return -1;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return exchange.getRequestBody();
        }
    }
}
