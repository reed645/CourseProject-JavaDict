/**
 * Author: <Ludi Han>
 * Student Id: <1581026>
 * Email: <ludih@student.unimelb.edu.au>
 */
package client;

import java.io.*;
import java.net.Socket;


public class DictionaryClient {

    public DictionaryClient() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Wrong format. Goodbye.");
            return;
        }

        //connect to the server
        Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
        OutputStream outputStream = socket.getOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));


        //create a thread to listen to the reply
        new ClientReader(socket).start();
        //use the GUI to interact
        new ClientGUI(bufferedWriter);

    }


}
