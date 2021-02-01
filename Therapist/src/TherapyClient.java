// TherapyClient Marcus Kok
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.Buffer;
import java.util.Date;

public class TherapyClient {
    public static void main(String[] args) throws Exception{
        // TODO Auto-generated method stub
        System.out.println("Marcus Kok");
        if(args.length != 1) { // requires single command line  parameter
            System.out.println("Restart. Command line parameter should be network address of Therapy server.");
            return; // terminate so user can start over
        }

        System.out.println("Opening log file.");
        FileWriter logFile = new FileWriter("TherapyLog.txt", true); // open in append mode
        BufferedWriter log = new BufferedWriter(logFile);


        System.out.println("Welcome to the remote therapy system!");
        InputStreamReader isr = new InputStreamReader(System.in); // low-level I/O class
        BufferedReader keyboard = new BufferedReader(isr); // high-level I/O class

        // write header to log file
        log.newLine();
        log.write("New therapy session on " + new Date());
        log.newLine(); // put newline char at end of this line

        while(true) { // call the readLine(0 method in the buffered Reader program, which will wait
            Socket s;
            try{
                s = new Socket(args[0], 1111);
            }
            catch(ConnectException ce){
                System.out.println("Connection to TherapyServer at " + args[0] + " on port 1111 has failed");
                System.out.println("Is server address correct? Has the server been started on port 1111?");
                return; // terminate so user can fix and restart
            }
            String question = keyboard.readLine();
            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); // high-level I/O class
            dos.writeUTF(question);

            if(question.length() == 0) continue; // continue returns to loop top
            if(question.equalsIgnoreCase("end")
                    ||  question.equalsIgnoreCase("stop")
                    ||  question.equalsIgnoreCase("exit")
                    ||  question.equalsIgnoreCase("quit")) {
                System.out.println("Hope you enjoyed the THERAPIST today!");
                break;
            }

            DataInputStream dis = new DataInputStream(s.getInputStream());
            String answer = dis.readUTF(); // receive answer from server
            System.out.println(answer); // print answer to console

            log.write("Question was: '" + question + "' Therapist answer was: " + answer);
            log.newLine();

        }
        System.out.println("Closing log file.");
        log.close(); // closes output file and writes it to the disk


    }
}

