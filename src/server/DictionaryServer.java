/**
 * Author: <Ludi Han>
 * Student Id: <1581026>
 * Email: <ludih@student.unimelb.edu.au>
 */
package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class DictionaryServer {
    static String filePath = "";
    private static final Map<String, List<String>> dictionary = new ConcurrentHashMap<>();

    public static Map<String, List<String>> getDictionary() {
        return dictionary;
    }

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Wrong format. Please try again.");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        filePath = args[1];
        //load the dictionary file
        loadDictionary(filePath);

        MyThreadPool pool = new MyThreadPool();

        //save the file when the server is shut down
        Runtime.getRuntime().addShutdownHook(
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        saveDictionary(filePath);
                    }
                })
        );

        try {
            //listen to the connection
            ServerSocket serverSocket = new ServerSocket(port);
            boolean connection = true;

            //set up a new thread to handle receive user's request
            while(connection){
                Socket socket = serverSocket.accept();
                pool.execute(new ClientHandler(socket, getDictionary()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }




    }

    private static void loadDictionary(String filePath) {
        File file = new File(filePath);
        //get the file; if file not exists, create a new file
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("File created successfully.");
                } else {
                    System.out.println("Failed to create file.");
                }
            } catch (IOException e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }

        //read the file
        BufferedReader reader = null;
        FileReader fileReader = null;
        try{
            reader = new BufferedReader(new FileReader(file));
            String line;
            while((line = reader.readLine())!= null){
                //ignore the empty line
                if (line.trim().isEmpty()) continue;
                // skip invalid line
                if (!line.contains(":")) continue;
                //parse the line
                String[] parts = line.split(":", 2);
                String word = parts[0].trim();
                String rawMeanings = parts[1].trim();
                String[] rawMeaningsList = rawMeanings.split(";");
                //the meanings to be stored in the dictionary
                List<String> meanings = new ArrayList<>();
                for(String meaning: rawMeaningsList){
                    String trimmed = meaning.trim();
                    if (!trimmed.isEmpty()) {
                        meanings.add(trimmed);
                    }
                }
                dictionary.put(word, meanings);
            }
            System.out.println("File loading success.");
        }catch(FileNotFoundException e){
            System.out.println("File not found.");
        } catch (IOException e) {
            System.out.println("Failed to read file: " + e.getMessage());
        }finally{
            try {
                if(reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                System.out.println("Invalid or missing file.");
            }
        }

    }

    private static void saveDictionary(String filePath) {
        BufferedWriter writer = null;
        try{
            writer = new BufferedWriter(new FileWriter(filePath));
            for (Map.Entry<String, List<String>> entry : dictionary.entrySet()) {
                String word = entry.getKey();
                List<String> meanings = entry.getValue();
                String line = word + ":" + String.join(";", meanings);
                writer.write(line);
                writer.newLine();
            }
            System.out.println("File saved!");
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.out.println("Failed to close writer: " + e.getMessage());
                }
            }
        }
    }
}
