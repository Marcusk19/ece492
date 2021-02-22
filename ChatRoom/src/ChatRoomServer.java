// Marcus Kok 200235945
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoomServer implements Runnable{

    ConcurrentHashMap<String, ObjectOutputStream> whosIn = new ConcurrentHashMap<String, ObjectOutputStream>();
    ConcurrentHashMap<String, String> passwords = new ConcurrentHashMap<String, String>();
    private ServerSocket ss;

    public static void main(String[] args) throws Exception{
        System.out.println("Marcus Kok");
        if(args.length > 0){
            System.out.println("Command line parameters have been provided, but are being ignored");
        }
        new ChatRoomServer();

    }

    public ChatRoomServer() throws Exception {
        ss = new ServerSocket(2222);

        System.out.println("ChatRoomServer is up at "
                            + InetAddress.getLocalHost().getHostAddress() +
                            " on port " + ss.getLocalPort());
        try{
            FileInputStream fis = new FileInputStream("passwords.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            passwords = (ConcurrentHashMap<String, String>) ois.readObject();
            for(String chatName : passwords.keySet()){ // print chat names from passwords.ser file to console
                System.out.println("Chat Name: " + chatName + ", Password: " + passwords.get(chatName));
            }
            ois.close();
        }
        catch (FileNotFoundException fnfe){
            System.out.println("passwords.ser is not found, so an empty collection will be used");
        }

        new Thread(this).start(); // this thread branches into the run() method.

    }

    @Override
    public void run() { //client threads enter here
        Socket s = null;
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        ObjectOutputStream previousOOS = null;
        String joinMessage = null;
        String chatName = null;
        String providedPassword = null;
        String storedPassword = null;
        String clientAddress = null;

        try {
            s = ss.accept();
            clientAddress = s.getInetAddress().getHostAddress();
            System.out.println("New client connecting from " + clientAddress);
            ois = new ObjectInputStream(s.getInputStream());
            joinMessage = ((String) ois.readObject()).trim();
            oos = new ObjectOutputStream(s.getOutputStream());
        } catch (Exception e) { // connecting client may not be using oos or firstMessage was not a String
            System.out.println("Client " + clientAddress + " join protocol not OOS or 1st message not String. " + e);
            if (s.isConnected()) {
                try {
                    s.close();
                } // hang up
                catch (IOException ioe) {
                } // already hung up!
            }
            return; // return tot he Thread object to terminate this client thread.
        } finally {
            new Thread(this).start();
        }
        // if still running here then s, ois, oos are all good and the join message was a String!
        // if not, we have dumped this caller and are waiting again (with a new thread) in accept() above for the next client to connect
        // JOIN processing
        try{
            int blankOffset = joinMessage.indexOf(" ");
            if (blankOffset < 0) {
                try {
                    System.out.println("No blank in join message: " + joinMessage);
                    oos.writeObject("Invalid format in 1st message."); // not specific in case of hacker caller
                    oos.close(); // kill connection
                } catch (Exception e) {
                    return; // kill client session thread
                }
            }
            chatName = joinMessage.substring(0, blankOffset).toUpperCase();
            providedPassword = joinMessage.substring(blankOffset).trim();

            if (passwords.containsKey(chatName)) { // is this chatName a KEY in the passwords collection?
                storedPassword = passwords.get(chatName); // if YES,  retrieve the stored pw for this chatName
                if (providedPassword.equals(storedPassword)) {
                    if (whosIn.containsKey(chatName)) {
                        previousOOS = whosIn.get(chatName);
                        whosIn.replace(chatName, oos);
                        previousOOS.writeObject("Session terminated due to rejoin from another location");
                        previousOOS.close(); // shut down previous connection. (this prompts leave processing)
                        System.out.println(chatName + " is rejoining");

                    }
                }
                // password was retrieved from passwords collection but entered pw is not = to it
                else {
                    oos.writeObject("Your entered password " + providedPassword + " is not the same as the password stored for chat name " + chatName);
                    oos.close(); // hang up
                    System.out.println("Invalid password: " + providedPassword + " instead of " + storedPassword + " for " + chatName);
                    return; // kill this client thread
                }
            } // end of pw was found in passwords collection
            else { // if chatName is NOT in the password collection then this chatName has NEVER joined before
                passwords.put(chatName, providedPassword);
                savePasswords();
                System.out.println(chatName + " is a new client in the chat room.");
            }

            oos.writeObject("Welcome to the chat room " + chatName + "!"); // send "join is successful"
            whosIn.put(chatName, oos); // add new-join client
            String[] whosInArray = whosIn.keySet().toArray(new String[0]);
            Arrays.sort(whosInArray);

            sendToAllClients("Welcome to " + chatName + " who has just joined (or rejoined) the chat room!");
            String whosInString = "["; // identifies the whosIn list to the clients.
            for (String name : whosInArray) {
                whosInString += name + ", ";
            }
            sendToAllClients(whosInString);
            System.out.println(chatName + " is joining");
            System.out.println("Currently in the chat room: " + whosInString.substring(1));
        } // bottom of try for join processing
        catch(Exception e){
            System.out.println("Connection failure during join processing from " + chatName + " at " +
                    clientAddress + " " + e);
            if(s.isConnected()){
                try{s.close();}
                catch(IOException ioe){} // already hung up!
            }
            return; // kill this client's thread
        }

        try {
            // SEND/RECEIVE processing
            while (true) {
                String message = (String) ois.readObject();
                System.out.println("Received " + message + " from " + chatName);
                sendToAllClients(chatName + " says: " + message);
            }
        }
        catch (Exception e){
            // LEAVE processing
            ObjectOutputStream currentOOS = whosIn.get(chatName);
            if(currentOOS == oos){
                // if = then this is the thread for the client that IS in the chat room now.
                // Do "normal" leave processing
                whosIn.remove(chatName);
                sendToAllClients("Goodbye to " + chatName + " who just left the chat room.");
                String[] whosInArray = whosIn.keySet().toArray(new String[0]);
                String whosInString = "[";
                for (String name : whosInArray) {
                    whosInString += name + ", ";
                }
                sendToAllClients(whosInString);

                System.out.println(chatName + " is leaving the chat room");

            }
            else{
                // if NOT = then this was the thread for the client in a PREVIOUS session
                // Don't do anything! (this old session has already been REPLACED in the re-join processing
            }
        }
    }

    private synchronized void sendToAllClients(Object message){ // "synchronized" restricts client threads to enter one at a time
        ObjectOutputStream[] oosArray = whosIn.values().toArray(new ObjectOutputStream[0]);
        for (ObjectOutputStream clientOOS : oosArray) {
            try{clientOOS.writeObject(message);}
            catch(IOException e){} // do nothing if send error
        }
    }

    private synchronized void savePasswords(){
        try{
            FileOutputStream fos = new FileOutputStream("passwords.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(passwords);
            oos.close();
        }
        catch(Exception e){
            System.out.println("passwords collection cannot be saved on disk: " + e);
        }
    }

}
