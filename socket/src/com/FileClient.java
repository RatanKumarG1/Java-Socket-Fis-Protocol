package com;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * @author Ratan
 *
 * Client class - listen to the dedicated port and execute the predefined
 * commands (like DIR, CD, INFO, PWD, QUIT) on the server files and directories
 * by sending the appropriate request to the server, which is listening to the
 * connected dedicated port taking command line user input from the user along
 * with the required parameters
 */
public class FileClient {

    private Socket socket;
    private OutputStream outputStream = null;
    Scanner sc = new Scanner(System.in);
    private InputStream inputStream = null;

    private static final String QUIT = "quit";
    private static final String GOODBYE = "200 Goodbye";    
    private static final String HOST = "localhost";
    private static final int PORT = 6655;
    private static final String CONNECTION_ESTABLISHED = "200 Welcome to File Information Service";
    
    /**
     * Connect the client to the dedicated port from local machine and execute
     * the appropriate commands, requesting to client
     *
     * @param host
     * @param port
     * @throws IOException
     */
    public FileClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        System.out.println(CONNECTION_ESTABLISHED);
        while (true) {
            // Get input from user
            String data = sc.nextLine();
            if (data.equalsIgnoreCase(QUIT)) {// Exit, if user enters QUIT from cmd line
                System.out.println(GOODBYE);
                break;
            }
            // Write the request to the server
            outputStream.write(data.getBytes());
            byte[] readBuffer = new byte[1024];
            int numBytes = inputStream.read(readBuffer);
            byte[] tempBuffer = new byte[numBytes];
            System.arraycopy(readBuffer, 0, tempBuffer, 0, numBytes);
            String message = new String(tempBuffer, "UTF-8");            
            // check whether the response is string or array and display accordingly
            if(message.startsWith("[") && message.endsWith("]")){
                List<String> rcvdMsgs = Arrays.asList(message.substring(1, message.length() - 1).split(","));
                // display the received message from server
                rcvdMsgs.stream().forEach((String msg) -> {
                    System.out.println(msg.trim());
                });
            } else {
                System.out.println(message);
            }
            
                        
        }
    }

    /**
     * Main method to execute the client using host and port
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Client");
        FileClient fc = new FileClient(HOST, PORT);
    }
}
