package com;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Ratan
 *
 * Server class - listen to the dedicated port same as client Receives the
 * request from the client and executes the operations on files and directory
 * present returns the appropriate response to the client
 */
public class FileServer extends Thread {

    private ServerSocket serverSocket;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private static final int PORT = 6655;
    private static final String MISSING_PARAMS = "500 Invalid command. Missing parameters";
    private static final String OK = "200 OK";
    private static final String UNKNOWN_COMMAND = "503 Unknown command";
    private static final String DIR = "DIR";
    private static final String INFO = "INFO";
    private static final String PWD = "PWD";
    private static final String CD = "CD";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");


    /**
     * Initiate the server socket with the dedicated port to listen and receive
     * the response
     *
     * @param port
     */
    public FileServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Respond to the client after prosessing the receiving the request
     */
    public void run() {
        while (true) {
            try {
                Socket clientSock = serverSocket.accept();
                outputStream = clientSock.getOutputStream();
                inputStream = clientSock.getInputStream();
                while (clientSock.isConnected()) {
                    byte[] readBuffer = new byte[1024];
                    int numBytes = inputStream.read(readBuffer);
                    byte[] tempBuffer = new byte[numBytes];
                    System.arraycopy(readBuffer, 0, tempBuffer, 0, numBytes);
                    String message = new String(tempBuffer, "UTF-8");
                    String reply = navMsg(message);
                    byte[] replyBytes = reply.getBytes();
                    outputStream.write(replyBytes);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Route and process the request based on the recieved command
     *
     * @param message
     * @return
     * @throws IOException
     */
    public String navMsg(String message) throws IOException {
        List<String> list = Arrays.asList(message.trim().split(" ", 2));
        if (list.size() == 1) {
            switch (list.get(0).toUpperCase().trim()) {
                case DIR:
                    return getDirContents().toString();
                case PWD:
                    return System.getProperty("user.dir");
                case CD:
                case INFO:
                    return MISSING_PARAMS;
            }
        } else {
            switch (list.get(0).toUpperCase().trim()) {
                case CD:
                    return changeDirectory(list.get(1).trim());
                case INFO:
                    return getFileDetails(list.get(1).trim()).toString();
            }
        }
        return UNKNOWN_COMMAND;
    }

    /**
     * Change the current working directory to the requested directory
     *
     * @param dirName
     * @return
     */
    public String changeDirectory(String dirName) {
        String filePath = null;
        if(dirName.equalsIgnoreCase("..")){
            File parentFile = new File(System.getProperty("user.dir")).getParentFile();
            dirName = parentFile.getName();
            filePath = parentFile.getAbsolutePath();
        } else {
            filePath = System.getProperty("user.dir") + File.separator + dirName;
        }
        File file = new File(filePath);
        if (file.exists()) {
            System.setProperty("user.dir", filePath);
            return "200 OK. Current directory is " + new File(System.getProperty("user.dir")).getName();
        }
        return "501 Directory " + dirName + " not found";
    }

    /**
     * Get the file/directory metadata details (type, size, created date)
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    public List<String> getFileDetails(String fileName) throws IOException {
        List<String> listAttrs = new ArrayList<>();
        File file = new File(System.getProperty("user.dir") + File.separator + fileName);
        if (file.exists()) {
            Path path = Paths.get(file.getAbsolutePath());
            BasicFileAttributes attrs = Files.getFileAttributeView(path, BasicFileAttributeView.class).readAttributes();

            if (file.isFile()) {
                listAttrs.add("Type: File");
                listAttrs.add("Size: " + attrs.size());
            } else if (file.isDirectory()) {
                listAttrs.add("Type: Directory");
            }
            String date = simpleDateFormat.format(new Date(attrs.creationTime().toMillis()));
            listAttrs.add("Created: " + date);            
            listAttrs.add(OK);
            return listAttrs;
        }
        listAttrs.add("501 File " + fileName + " not found");
        return listAttrs;
    }

    /**
     * Get the current working directory contents
     * all files and sub-directories
     * @return
     */
    public List<String> getDirContents() {
        List<String> listFiles = new ArrayList<>();
        File curDir = new File(System.getProperty("user.dir"));
        File[] filesList = curDir.listFiles();
        for (File f : filesList) {
            if (f.isDirectory()) {
                listFiles.add(f.getName());
            }
            if (f.isFile()) {
                listFiles.add(f.getName());
            }
        }
        listFiles.add(OK);
        return listFiles;
    }

    /**
     * Main method to execute the client using the dedicated port
     * @param args 
     */
    public static void main(String[] args) {
        System.out.println("Server");
        FileServer fs = new FileServer(PORT);
        fs.start();
    }

}
