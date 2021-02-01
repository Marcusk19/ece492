import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;

public class lecture2Client {
    public static void main(String[] args) throws Exception{
        if(args.length != 1) { // requires single command line  parameter
            System.out.println("Restart. Provide the message to send as a single command line parameter. (enclose " +
                    "multiple words in quotes)");
            return; // terminate so user can start over
        }
        System.out.println("Message to send is: " + args[0]); // show the message the user entered.

        try {
            Socket s = new Socket("localhost", 1234);
            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); // load high-level output class
            dos.writeUTF(args[0]); // send entered command line parameter (in slot 0 of the args command line parms array)

            DataInputStream dis = new DataInputStream(s.getInputStream()); // same socket
            String reply = dis.readUTF(); // wait  for reply from server

            System.out.println("Reply from server is: '" + reply + "'");
        }
        catch(ConnectException ce){
            System.out.println("We are unable to connect to the server. Perhaps the server app is not up yet?");
        }
    }
}
