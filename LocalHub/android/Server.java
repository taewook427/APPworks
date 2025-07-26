package com.example.kutil6_localhub;

import android.content.Context;

import fi.iki.elonen.NanoHTTPD; // added from file - project structure

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Server extends NanoHTTPD {
    public LogMethod logger;
    private int port;
    private String textdata = "";
    private String[] filedata = new String[0];
    private Context context;

    public interface LogMethod {
        void addLog(String message);
        void printIP(String message);
    }
    public Server(Context c, int p, LogMethod l) {
        super(p);
        context = c;
        port = p;
        logger = l;
    }

    public void startServer() {
        // init local, only app internal storage accessible (/data/data/com.example.kutil6_lhub)
        File tdir = new File(context.getFilesDir(), "temp");
        try {
            if (tdir.exists()) {
                for (File child : tdir.listFiles()) {
                    if (!child.delete()) { logger.addLog("fail=delete child file"); }
                }
                if (!tdir.delete()) { logger.addLog("fail=delete temp dir"); }
            }
            if (!tdir.mkdir()) { logger.addLog("fail=make temp dir"); }
        } catch (Exception e) {
            logger.addLog(e.toString());
        }
        logger.addLog("server initialized");

        // get local ip of server
        getIP();

        // start server
        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (Exception e) {
            logger.addLog(e.toString());
        }
        logger.addLog("server starting...");
    }

    public void stopServer() {
        logger.addLog("server stopping...");
        try {
            stop();
        } catch (Exception e) {
            logger.addLog(e.toString());
        }
    }

    // get local IPs
    private void getIP() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                List<InetAddress> addresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (!address.isLoopbackAddress() && address.getHostAddress().contains(".")) {
                        logger.printIP("http://" + address.getHostAddress() + ":" + port);
                    }
                }
            }
        } catch (Exception e) {
            logger.addLog(e.toString());
        }
    }

    // get asset binary file
    private byte[] getAsset(String fileName) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        InputStream inputStream = null;

        try {
            inputStream = context.getAssets().open(fileName);
            byte[] data = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            return buffer.toByteArray();

        } catch (Exception e) {
            logger.addLog(e.toString());
            return null;

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    logger.addLog(e.toString());
                }
            }
            try {
                buffer.close();
            } catch (Exception e) {
                logger.addLog(e.toString());
            }
        }
    }

    @Override // serve html file
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        if ("/".equals(uri) || "/index.html".equals(uri)) {
            logger.addLog("API call=index.html");
            String content = new String(getAsset("index.html"), StandardCharsets.UTF_8);
            if (content.isEmpty()) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Cannot find index file");
            } else {
                return newFixedLengthResponse(Response.Status.OK, "text/html", content);
            }
        } else if ("/favicon.ico".equals(uri)) {
            logger.addLog("API call=favicon.ico");
            byte[] iconData = getAsset("favicon.ico");
            if (iconData == null) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Cannot find favicon");
            } else {
                return newFixedLengthResponse(Response.Status.OK, "image/x-icon", new ByteArrayInputStream(iconData), iconData.length);
            }
        }

        // API endpoint
        if (uri.equals("/api/text")) {
            logger.addLog("API call=text handle");
            return handleText(session);
        } else if (uri.equals("/api/files/list")) {
            logger.addLog("API call=file list get");
            return handleFileList(session);
        } else if (uri.equals("/api/files/upload")) {
            logger.addLog("API call=file upload");
            return handleFileUpload(session);
        } else if (uri.startsWith("/api/files/download")) {
            logger.addLog("API call=file download");
            return handleFileDownload(session);
        } else if (uri.startsWith("/api/files/delete")) {
            logger.addLog("API call=file delete");
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
            File savedFile = new File(context.getFilesDir(), "temp/" + filename);
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
        File file = new File(context.getFilesDir(), "temp/" + filename);
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
        File file = new File(context.getFilesDir(), "temp/" + filename);
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