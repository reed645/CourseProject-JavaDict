/**
 * Author: <Ludi Han>
 * Student Id: <1581026>
 * Email: <ludih@student.unimelb.edu.au>
 */

package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientReader extends Thread {
    private Socket socket;

    public ClientReader(Socket socket) {
        this.socket = socket;
    }

    public void run(){
        BufferedReader bufferedReader = null;
        try {
            InputStream socketInputStream = socket.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(socketInputStream));
            while(true){
                String reply = bufferedReader.readLine();
                if (reply != null) {
                    ClientGUI.showResponse(reply);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socket.close();
                bufferedReader.close();
            } catch (IOException e) {
                System.out.println("Disconnected from server");
            }
        }

    }
}
