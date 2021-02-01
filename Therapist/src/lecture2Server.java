import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class lecture2Server {
    public static void main(String[] args) throws Exception{
        ServerSocket ss = new ServerSocket(1234); // this app must reserve this port # on it's host computer
        System.out.println("Server is up at" + InetAddress.getLocalHost().getHostAddress() + " waiting for a client to connect on port "
        + ss.getLocalPort());

        while(true){
            Socket s = ss.accept(); // WAIT here for next client to CONNECT

            DataInputStream dis = new DataInputStream(s.getInputStream()); // load high level input I/O class for this Socket
            String message = dis.readUTF(); // then WAIT again here for the just-connected client to SEND

            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); // load high-level output I/O class for this Socket
            dos.writeUTF("Got your message: '" + message + "'"); // send reply to client request.

            dos.close(); // or s.close();
            System.out.println("received: '" + message + "' from " + s.getInetAddress().getHostAddress()); // debug trace on server console
        }
    }
}
