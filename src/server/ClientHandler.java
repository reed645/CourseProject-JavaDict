/**
 * Author: <Ludi Han>
 * Student Id: <1581026>
 * Email: <ludih@student.unimelb.edu.au>
 */
package server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable{
    private Socket socket;
    private final Map<String, List<String>> dictionary;

    public ClientHandler(Socket socket, Map<String, List<String>> dictionary) {
        this.socket = socket;
        this.dictionary = dictionary;
    }

    @Override
    public void run() {
        BufferedReader bufferedReader = null;
        try {
            //read the message
            InputStream inputStream = socket.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String data;
            while((data = bufferedReader.readLine()) != null){
                System.out.println("Receive:" + data);
                //parse the json data and handle user's command
                handleData(data);

            }
        } catch (IOException e) {
            System.out.println("Client disconnected.");
        }finally {
            try {
                if(bufferedReader != null){
                    bufferedReader.close();
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void handleData(String data) {
        try{
            //turn the string into json object
            JsonObject request = JsonParser.parseString(data).getAsJsonObject();
            //get the property of the request
            String action = request.get("action").getAsString();
            String word = request.get("word").getAsString();

            switch (action) {
                case "add":
                    addWord(request);
                    break;
                case "search":
                    searchWord(request);
                    break;
                case "update":
                    updateMeaning(request);
                    break;
                case "add meaning":
                    addMeaning(request);
                    break;
                case "delete":
                    deleteWord(request);
                    break;
                default:
                    String response = "Unknown action: " + action;
                    sendResponse(response);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }



    }


    private void addWord(JsonObject request) {
        String word = request.get("word").getAsString();
        String meaning = request.get("meaning").getAsString();
        if (!dictionary.containsKey(word)) {
            List<String> meanings = parseMeaning(meaning);
            dictionary.put(word, meanings);
            sendResponse("\"" + word + "\"" + " is added successfully!");
        }else{
            sendResponse("Failed! \"" + word + "\" already exists.");
        }

    }


    private void searchWord(JsonObject request) {
        String word = request.get("word").getAsString();
        if (dictionary.containsKey(word)) {
            List<String> meanings = dictionary.get(word);
            sendResponse(word + ": " + String.join("; ", meanings));
        }else{
            sendResponse("Failed! \"" + word + "\" not found.");
        }
    }

    private void updateMeaning(JsonObject request) {
        String word = request.get("word").getAsString();
        String oldMeaning = request.get("oldMeaning").getAsString().trim();
        String newMeaning = request.get("newMeaning").getAsString().trim();

        if (dictionary.containsKey(word)) {
            List<String> meanings = dictionary.get(word);
            int index = meanings.indexOf(oldMeaning);
            //see if the old meaning exists in the dictionary
            if (index != -1) {
                meanings.set(index, newMeaning);
                sendResponse("Update success！ \"" + "The meaning of " + word + "\" is updated.");
            } else {
                sendResponse("Failed! Existing meaning not found.");
            }
        }else{
            sendResponse("Failed! Word not found.");
        }
    }

    private void addMeaning(JsonObject request) {
        String word = request.get("word").getAsString();
        String rawMeanings = request.get("newMeaning").getAsString().trim();
        if (!dictionary.containsKey(word)) {
            sendResponse("Failed! Word not found.");
            return;
        }

        List<String> meanings = dictionary.get(word);
        List<String> newMeanings = parseMeaning(rawMeanings);

        //iterate the list to see if the meaning exists
        int added = 0;
        for (String meaning : newMeanings) {
            if (!meanings.contains(meaning)) {
                meanings.add(meaning);
                added++;
            }
        }


        if (added > 0) {
            sendResponse("Add meaning success! " + added + " new meaning(s) added.");
        } else {
            sendResponse("Failed! Meaning already exists.");
        }

    }

    private void deleteWord(JsonObject request) {
        String word = request.get("word").getAsString();
        if (dictionary.containsKey(word)) {
            dictionary.remove(word);
            sendResponse("Delete success!");
        }else{
            sendResponse("Failed! \"" + word + "\" not found.");
        }

    }

    private void sendResponse(String response) {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> parseMeaning(String meaning) {
        String[] meanings = meaning.split(";");
        List<String> meaningList = new ArrayList<>();
        for (String rawMeaning: meanings){
            //in case there's invalid input in the data
            String trimmed = rawMeaning.trim();
            if (!trimmed.isEmpty()) {
                meaningList.add(trimmed);
            }
        }
        return meaningList;

    }



}
