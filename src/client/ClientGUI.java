/**
 * Author: <Ludi Han>
 * Student Id: <1581026>
 * Email: <ludih@student.unimelb.edu.au>
 */
package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.IOException;


public class ClientGUI extends JFrame {
    private JButton searchButton;
    private JButton deleteButton;
    private JButton addButton;
    private JButton updateButton;
    private JTextField meaningField;
    private JTextField wordField;
    private JTextArea responseArea;
    private JPanel MainField;
    private JTextField existingMeaning;

    private BufferedWriter writer;
    private static JTextArea staticResponseArea;
    private static final Gson gson = new Gson();

    public ClientGUI(BufferedWriter bufferedWriter) {
        this.writer = bufferedWriter;
        staticResponseArea = responseArea;

        setContentPane(MainField);
        setTitle("Dictionary Client");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);


        //bind the button with the action
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //in case there's no input in the filed
                String word = wordField.getText().trim().toLowerCase();
                String meaning = meaningField.getText().trim();
                if (word.isEmpty() || meaning.isEmpty()) {
                    showResponse("Word or meaning cannot be empty.");
                    return;
                }
                JsonObject request = new JsonObject();
                request.addProperty("action", "add");
                request.addProperty("word", word);
                request.addProperty("meaning", meaning);
                sendRequest(request);
            }
        });


        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String word = wordField.getText().trim().toLowerCase();
                if (word.isEmpty()) {
                    showResponse("Word cannot be empty.");
                    return;
                }
                //create json data
                JsonObject request = new JsonObject();
                request.addProperty("action", "search");
                request.addProperty("word", word);
                sendRequest(request);
                //reset the text field
                wordField.setText("");
            }
        });


        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String word = wordField.getText().trim().toLowerCase();
                String oldMeaning = existingMeaning.getText().trim();
                String newMeaning = meaningField.getText().trim();

                if (word.isEmpty() || newMeaning.isEmpty() ) {
                    showResponse("Word or new meaning cannot be empty.");
                    return;
                }
                //create the json data and distinguish "add new meaning" from "update meaning"
                JsonObject request = new JsonObject();
                request.addProperty("word", word);
                request.addProperty("newMeaning", newMeaning);
                if (oldMeaning.isEmpty()){
                    request.addProperty("action", "add meaning");
                }else{
                    request.addProperty("action", "update");
                    request.addProperty("oldMeaning", oldMeaning);
                }
                sendRequest(request);
                //reset the text field
                wordField.setText("");
                existingMeaning.setText("");
                meaningField.setText("");
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String word = wordField.getText().trim().toLowerCase();
                if (word.isEmpty()) {
                    showResponse("Word cannot be empty.");
                    return;
                }
                JsonObject request = new JsonObject();
                request.addProperty("action", "delete");
                request.addProperty("word", word);
                sendRequest(request);
                //reset the text field
                wordField.setText("");
            }
        });
    }

    public static void showResponse(String message) {
        if (staticResponseArea != null) {
            staticResponseArea.append(message + "\n");
        }
    }

    private void sendRequest(JsonObject request) {
        String jsonString = gson.toJson(request);
        int maxRetries = 5;
        //sleep 1 second if attempt failed
        int sleepTime = 1000;
        boolean success = false;

        for (int attempt = 1; attempt <= maxRetries && !success; attempt++) {
            try {
                writer.write(jsonString + "\n" );
                writer.flush();
                success = true;
            } catch (IOException e) {
                showResponse("Error sending message: " + e.getMessage());
                if (attempt == maxRetries) {
                    showResponse("Error! Failed to send request after multiple attempts.");
                }else if(attempt < maxRetries){
                    showResponse("Retrying to send request... (Attempt " + (attempt + 1) + ")");
                } else {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

    }
}
