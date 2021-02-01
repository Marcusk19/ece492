// TherapyServer Marcus Kok

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TherapyServer {
    public static void main(String[] args) throws Exception{
        // TODO Auto-generated method stub
        System.out.println("Marcus Kok");
        if(args.length > 0){
            System.out.println("Server does not accept any command line parameters and is ignoring" +
                    " given parameter");
        }

        ServerSocket ss;
        try{
            ss = new ServerSocket(1111);
        }
        catch(Exception e){
            System.out.println("Port 1111 is not available.");
            System.out.println("An already-running version of the server may need to be terminated");
            return;
        }

        System.out.println("Server is up at " + InetAddress.getLocalHost().getHostAddress() + " waiting for a client to connect on port "
                + ss.getLocalPort());

        String[] answers = {"Absolutely", "Certainly Not", "Forget It!", "Ask Your Mother", "I dont' think so...", "Are you kidding?",
        "Not Today", "In your dreams!", "It's OK with Me!", "Sounds Good", "Yes", "It's Only a Matter of Time", "No"};

        while(true) { // call the readLine() method in the buffered Reader program, which will wait
            Socket s = ss.accept(); // wait for a client to connect

            DataInputStream dis = new DataInputStream(s.getInputStream()); // high-level input I/O class
            String question = dis.readUTF(); // low-level input I/O class

            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); // high-level output I/O class

            int index = (int) (Math.random() * answers.length); // generate random index to array

            dos.writeUTF(answers[index]); // send random answer to client

            dos.close(); // hang up connection

        }
    }
}

