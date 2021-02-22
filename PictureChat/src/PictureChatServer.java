// Marcus Kok 200235945
import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class PictureChatServer implements Runnable{

    ConcurrentHashMap<String, ObjectOutputStream> whosIn = new ConcurrentHashMap<String, ObjectOutputStream>();
    Vector<String> whosOut = new Vector<String>();
    ConcurrentHashMap<String, String> passwords = new ConcurrentHashMap<String, String>();
    ConcurrentHashMap<String, Vector<Object>> savedMessages = new ConcurrentHashMap<String, Vector<Object>>();

    private ServerSocket ss;

    public static void main(String[] args) throws Exception{
        System.out.println("PictureChatServer: by Marcus Kok");
        if(args.length > 0){
            System.out.println("Command line parameters have been provided, but are being ignored");
        }
        new PictureChatServer();

    }

    public PictureChatServer() throws Exception {
        ss = new ServerSocket(5555);

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

        try{
            FileInputStream fis = new FileInputStream("SavedChatMessages.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            savedMessages = (ConcurrentHashMap<String, Vector<Object>>) ois.readObject();
            System.out.println("Saved Messages:");
            for(String chatName : savedMessages.keySet()){
                System.out.println(chatName +":");
                Vector<Object> printArray = savedMessages.get(chatName);
                for(int i = 1; i<printArray.size(); i++) System.out.println(printArray.get(i));
            }
            ois.close();
        }
        catch(FileNotFoundException fnfe){
            System.out.println("SavedChatMessages.ser is not found, so an empty collection will be used");
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
            // processing for updating whos in and whos out
            sendToAllClients("Welcome to " + chatName + " who has just joined (or rejoined) the chat room!");
            String[] whosInSendArray = new String[whosInArray.length+1]; // identifies the whosIn list to the clients.
            whosInSendArray[0] = "whosin";
            int i = 1;
            for (String name : whosInArray){whosInSendArray[i++] = name;}
            sendToAllClients(whosInSendArray);
            System.out.println(chatName + " is joining");
            System.out.println("Currently in the chat room: " + Arrays.toString(whosInArray));

            String[] allNames = passwords.keySet().toArray(new String[0]);
            String[] whosOutArray = new String[passwords.size() - whosIn.size() + 1];
            whosOutArray[0] = "whosout";
            int j = 1;
            for(String name : allNames){
                if(!whosIn.containsKey(name)){
                    whosOutArray[j++] = name;
                }
            }
            sendToAllClients(whosOutArray);
            whosOutArray[0] = "";
            System.out.println("Currently NOT in the chat room: " + Arrays.toString(whosOutArray));

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



        // check for saved messages
        Vector<Object> savedMessageList = savedMessages.get(chatName);
        if(savedMessageList != null) {
            // look for ignore array at slot 0 of savedMessagelist
            if(savedMessageList.get(0) instanceof String[]){
                System.out.println("found ignore list for " + chatName);
                try{
                    oos.writeObject(savedMessageList.get(0));
                }
                catch(Exception e){}
            }
            while (savedMessageList.size() > 1) {
                String savedMessage = (String) savedMessageList.remove(1);
                try {
                    oos.writeObject(savedMessage);
                    saveMessages();
                } catch (Exception e) {
                    break;
                }
            }
        }

        try {
            // SEND/RECEIVE processing
            while (true) {
                Object somethingfromClient = ois.readObject();
                if(somethingfromClient instanceof String) {
                    String message = (String)somethingfromClient;
                    System.out.println("Received " + "'" + message + "'" + " from " + chatName);
                    sendToAllClients(chatName + " says: " + message);
                }
                else if(somethingfromClient instanceof Object[]){
                    Object[] privateOrSaveArray = (Object[])somethingfromClient;
                    System.out.println("Received Array: " + Arrays.toString(privateOrSaveArray) + " from " + chatName);
                    // check for ignore-list processing
                    if(privateOrSaveArray[0] instanceof  String) {
                        String[] arrayOfStrings = (String[]) privateOrSaveArray;
                        if (arrayOfStrings[0].equals("ignore")) {
                            System.out.println("Adding " + Arrays.toString(privateOrSaveArray) + " to " + chatName + "'s ignore list");
                            Vector<Object> clientSavedMessages = savedMessages.get(chatName);
                            clientSavedMessages.set(0, arrayOfStrings);
                            saveMessages();
                            continue;
                        }
                    }
                    sendToPrivateRecipients(chatName, privateOrSaveArray);
                }
                else if(somethingfromClient instanceof ImageIcon){
                    ImageIcon image = (ImageIcon) somethingfromClient;
                    System.out.println("Received Image: " + image.getDescription());
                    sendToAllClients(image);
                }
                else{
                    System.out.println("Object received from " + chatName + " was not type ImageIcon, String, or array-of-Strings");
                    System.out.println(somethingfromClient);
                }
            }
        }
        catch (Exception e){
            // LEAVE processing
            System.out.println(e);
            ObjectOutputStream currentOOS = whosIn.get(chatName);
            if(currentOOS == oos){
                // if = then this is the thread for the client that IS in the chat room now.
                // Do "normal" leave processing
                whosIn.remove(chatName);

                sendToAllClients("Goodbye to " + chatName + " who just left the chat room.");
                String[] whosInArray = whosIn.keySet().toArray(new String[0]);
                String[] whosInSendArray = new String[whosInArray.length + 1];
                whosInSendArray[0] = "whosin";
                int i = 1;
                for (String name : whosInArray) {
                    whosInSendArray[i++] = name;
                }
                System.out.println("whosInSend: " + Arrays.toString(whosInSendArray));
                sendToAllClients(whosInSendArray);
                System.out.println(chatName + " is leaving the chat room");


                String[] allNames = passwords.keySet().toArray(new String[0]);
                String[] whosOutArray = new String[passwords.size() - whosIn.size() + 1];
                whosOutArray[0] = "whosout";
                int j = 1;
                for(String name : allNames){
                    if(!whosIn.containsKey(name)){
                        whosOutArray[j++] = name;
                    }
                }
                sendToAllClients(whosOutArray);
            }
            else{
                // if NOT = then this was the thread for the client in a PREVIOUS session
                // Don't do anything! (this old session has already been REPLACED in the re-join processing
            }
        }
    }

    private synchronized void sendToAllClients(Object message){ // "synchronized" restricts client threads to enter one at a time
        ObjectOutputStream[] oosArray = whosIn.values().toArray(new ObjectOutputStream[0]);
        for (ObjectOutputStream clientOOS : whosIn.values()) {
            try{

                clientOOS.writeObject(message);
            }
            catch(IOException e){} // do nothing if send error
        }
    }

    private void sendToPrivateRecipients(String senderChatName, Object[] messageAndRecipients){
        // slot 0 of the received array may be a String message or a picture (ImageIcon)
        // If private, the message or picture description should already include "PRIVATE"
        String message = null;
        ImageIcon picture = null;
        boolean sendIsPrivate = false;

        if(messageAndRecipients[0] instanceof String){
            message = (String) messageAndRecipients[0]; // safe cast
            if(message.contains("PRIVATE")) sendIsPrivate = true;
        }
        if(messageAndRecipients[0] instanceof ImageIcon){
            picture = (ImageIcon) messageAndRecipients[0];
            if(picture.getDescription().contains("PRIVATE")) sendIsPrivate = true;
        }
        // know that all slots after - are string recipients
        Vector<String> allRecipients = new Vector<String>();
        Vector<String> sendRecipients = new Vector<String>();
        Vector<String> saveRecipients = new Vector<String>();

        // fill vectors
        for(int i = 1; i < messageAndRecipients.length; i++){
            String name = (String)messageAndRecipients[i];
            allRecipients.add(name);
            if(whosIn.containsKey(name)) sendRecipients.add(name);
            else saveRecipients.add(name);
        }

        // append list of ALL recipients to a private message or picture description.
        if((message != null) && sendIsPrivate){
            message += " to " + allRecipients;
        }
        if((picture != null) && sendIsPrivate){
            String description = picture.getDescription();
            description += " to " + allRecipients;
            picture.setDescription(description);
        }

        // send private message/picture to all private recipietns currently in the chatRoom
        for(String privateRecipient : sendRecipients){
            ObjectOutputStream privateRecipientOOS = whosIn.get(privateRecipient);
            try{
                if(message != null) privateRecipientOOS.writeObject(message);
                if(picture != null) privateRecipientOOS.writeObject(picture);
            }
            catch(Exception e){ // this person just left the chat room!
                saveRecipients.add(privateRecipient); // show it to them when they next rejoin
            }
        }

        // send confirmation to the sender
        ObjectOutputStream privateSenderOOS = whosIn.get(senderChatName);
        if(privateSenderOOS != null){
            // has the SENDER left the chatRoom? If so, can SKIP sending a confirmation
            try{
                if(message != null && sendIsPrivate) privateSenderOOS.writeObject("You sent " + message);
                if(picture != null && sendIsPrivate) privateSenderOOS.writeObject(picture);
            }
            catch (Exception e){} // skip if confirmation send fails
        }

        // save this message /picture for any saveResidents in the savedMessage file
        for(String saveRecipient : saveRecipients){
            // are there save-for recipients
            Vector recipientSavedMessageVector = savedMessages.get(saveRecipient);
            if(recipientSavedMessageVector == null){
                // no messages have ver been saved for this client
                System.out.println("Adding a new client entry in saveMessages for a SAVE operation");
                recipientSavedMessageVector = new Vector();
                String[] emptyIgnoreArray = {"ignore"};
                recipientSavedMessageVector.add(emptyIgnoreArray); // to slot [0];
                savedMessages.put(saveRecipient,recipientSavedMessageVector); // add chatName key and ignore list to savedMessages
            }
            // add the message to the recipient's Vector:
            if(message != null) recipientSavedMessageVector.add(message);
            if(picture != null) recipientSavedMessageVector.add(picture);
        }
        if(!saveRecipients.isEmpty()) saveMessages();
    }

    private void saveForNotInRecipients(String saverChatName, String[] messageAndRecipients){
        System.out.println("In saveForNotInRecipients()");
        String saveMessage = messageAndRecipients[0];
        messageAndRecipients[0] = "";
        String saveRecipients = Arrays.toString(messageAndRecipients);
        saveRecipients = "[" + saveRecipients.substring(3); // drop leading comma
        System.out.println("Received save message '" + saveMessage + "' from " + saverChatName);
        String totalSaveMessage;
        if(saveMessage.startsWith("PRIVATE"))
            totalSaveMessage = saverChatName + " saved this message for " + saveRecipients + " on " + new Date() + " : " + saveMessage;
        else
            totalSaveMessage = saverChatName + " saved this message for " + saveRecipients + " on " + new Date() + " : " + saveMessage;
        System.out.println(totalSaveMessage);

        for(int i = 1; i < messageAndRecipients.length; i++){
            String saveRecipient = messageAndRecipients[i];
            // get the pointer to the recipient's Vector of saved messages in the savedMessages collection.
            Vector<Object> recipientSavedMessagesVector = savedMessages.get(saveRecipient);
            if(recipientSavedMessagesVector == null){
                recipientSavedMessagesVector = new Vector<Object>();
                recipientSavedMessagesVector.add("ignore list"); // slot 0 is reserved for the ignore list
                savedMessages.put(saveRecipient, recipientSavedMessagesVector); // add chatName and Vector to savedMessages collection
            }
            // Add the message to the bottom of the recipient's vector
            recipientSavedMessagesVector.add(totalSaveMessage);
        }
        saveMessages();
        ObjectOutputStream oos = whosIn.get(saverChatName);
        try{oos.writeObject(totalSaveMessage);}
        catch(Exception e){}
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

    private synchronized void saveMessages(){
        try{
            FileOutputStream fos = new FileOutputStream("SavedChatMessages.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(savedMessages);
            oos.close();
        }
        catch(Exception e){
            System.out.println("messages collection cannot be saved on disk: " + e);
        }
    }

}
