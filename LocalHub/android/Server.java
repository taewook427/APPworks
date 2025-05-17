package com.example.lhub;

import android.content.Context;

import fi.iki.elonen.NanoHTTPD; // added from file - project structure

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Server extends NanoHTTPD {
    private MainActivity logger;
    private String textdata = "";
    private String[] filedata = new String[0];

    public Server(MainActivity m) {
        super(5000);
        logger = m;

        // init local
        Path tdir = Paths.get("/data/data/com.example.lhub/temp"); // only app internal storage accessible (/data/data/com.example.lhub)
        try {
            if (Files.exists(tdir)) {
                Files.walk(tdir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
            Files.createDirectories(tdir);
        } catch (Exception e) {
            logger.addLog(e.toString());
        }
        logger.addLog("server initialized");

        // start http server
        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (Exception e) {
            logger.addLog(e.toString());
        }
        logger.addLog("server starting...");

        // get local ip of server
        printIP();
    }

    // get local IPs
    private void printIP() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                List<InetAddress> addresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (!address.isLoopbackAddress() && address.getHostAddress().contains(".")) {
                        logger.addLog("IP " + address.getHostAddress() + ":5000");
                    }
                }
            }
        } catch (Exception e) {
            logger.addLog(e.toString());
        }
    }

    // get text from assets
    private String getAsset(Context context, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;

        try {
            inputStream = context.getAssets().open(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            return stringBuilder.toString();

        } catch (Exception e) {
            logger.addLog(e.toString());
            return null;

        } finally { // close streams in a finally block
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    logger.addLog(e.toString());
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    logger.addLog(e.toString());
                }
            }
        }
    }

    @Override // serve html file
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        if ("/".equals(uri) || "/index.html".equals(uri)) {
            String content = getAsset(logger, "index.html");
            if (content == null) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Cannot find index file");
            } else {
                return newFixedLengthResponse(Response.Status.OK, "text/html", content);
            }
        }

        // API endpoint
        if (uri.equals("/api/text")) {
            return handleText(session);
        } else if (uri.equals("/api/files/list")) {
            return handleFileList(session);
        } else if (uri.equals("/api/files/upload")) {
            return handleFileUpload(session);
        } else if (uri.startsWith("/api/files/download")) {
            return handleFileDownload(session);
        } else if (uri.startsWith("/api/files/delete")) {
            return handleFileDelete(session);
        } else {
            logger.addLog("URI fail=" + uri);
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found");
        }
    }

    // text get & set
    private Response handleText(IHTTPSession session) {
        if (Method.GET.equals(session.getMethod())) {
            return newFixedLengthResponse(Response.Status.OK, "text/plain", textdata);

        } else if (Method.POST.equals(session.getMethod())) {
            try {
                Map<String, String> files = new HashMap<>();
                session.parseBody(files);
                textdata = files.values().iterator().next();
                return newFixedLengthResponse(Response.Status.OK, "text/plain", "Text saved");
            } catch (Exception e) {
                logger.addLog(e.toString());
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Server error");
            }

        }
        return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Invalid method");
    }

    // file list sync
    private Response handleFileList(IHTTPSession session) {
        if (!Method.GET.equals(session.getMethod())) {
            return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Invalid method");
        }

        StringBuilder res = new StringBuilder();
        res.append("[");
        for (int i = 0; i < filedata.length; i++) {
            res.append("\"");
            res.append(filedata[i]);
            res.append("\"");
            if (i < filedata.length - 1) {
                res.append(",");
            }
        }
        res.append("]");
        return newFixedLengthResponse(Response.Status.OK, "application/json", res.toString());
    }

    // file upload
    private Response handleFileUpload(IHTTPSession session) {
        if (!Method.POST.equals(session.getMethod())) {
            return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Invalid method");
        }

        try {
            // get file names
            Map<String, String> files = new HashMap<>();
            session.parseBody(files);
            String tempname = files.get("file");
            Map<String, List<String>> parameters = session.getParameters();
            String filename = parameters.get("filename").get(0);
            if (tempname == null || filename == null) {
                logger.addLog("temp=" + tempname + ", file=" + filename);
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Error reading file");
            }

            // get files ready
            File tempFile = new File(tempname);
            File savedFile = new File("/data/data/com.example.lhub/temp/" + filename);
            boolean exists = savedFile.exists();
            if (exists) {
                savedFile.delete();
            }

            // file copy
            try (InputStream in = new FileInputStream(tempFile);
                 OutputStream out = new FileOutputStream(savedFile)) {
                byte[] buffer = new byte[1048576]; // 1MB chunks
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // file list update
            if (!exists) {
                String[] temp = new String[filedata.length + 1];
                System.arraycopy(filedata, 0, temp, 0, filedata.length);
                temp[temp.length - 1] = filename;
                filedata = temp;
            }
            return newFixedLengthResponse(Response.Status.OK, "text/plain", "File uploaded");

        } catch (Exception e) {
            logger.addLog(e.toString());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error saving file");
        }
    }

    // file download
    private Response handleFileDownload(IHTTPSession session) {
        if (!Method.GET.equals(session.getMethod())) {
            return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Invalid method");
        }

        // get file name & open file
        String uri = session.getUri();
        String filename = uri.substring(uri.lastIndexOf("/") + 1);
        File file = new File("/data/data/com.example.lhub/temp/" + filename);
        if (!file.exists()) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found");
        }

        // serve file as stream
        try {
            FileInputStream fis = new FileInputStream(file);
            return newChunkedResponse(Response.Status.OK, "application/octet-stream", fis);
        } catch (Exception e) {
            logger.addLog(e.toString());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error reading file");
        }
    }

    // file delete
    private Response handleFileDelete(IHTTPSession session) {
        if (!Method.DELETE.equals(session.getMethod())) {
            return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Invalid method");
        }

        // get file name & delete
        String uri = session.getUri();
        String filename = uri.substring(uri.lastIndexOf("/") + 1);
        File file = new File("/data/data/com.example.lhub/temp/" + filename);
        if (file.exists()) {
            file.delete();
        }

        // update file list
        String[] temp = new String[filedata.length];
        int index = 0;
        for (int i = 0; i < filedata.length; i++) {
            if (!filedata[i].equals(filename)) {
                temp[index++] = filedata[i];
            }
        }
        filedata = Arrays.copyOfRange(temp, 0, index);
        return newFixedLengthResponse(Response.Status.OK, "text/plain", "File deleted");
    }
}