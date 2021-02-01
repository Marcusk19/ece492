// Marcus Kok 200235945
import javax.sound.midi.Soundbank;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

public class ChatRoomClient implements ActionListener, Runnable {
    Socket s;
    ObjectOutputStream oos;
    ObjectInputStream ois;
    String  newLine        = System.lineSeparator();
    int     fontSize       = 20;
    int     maxFontSize    = 50;
    int     minFontSize    = 5;
    boolean loadedFromMain = false;

    // Chat Window
    JFrame chatWindow = new JFrame();
    JPanel topPanel = new JPanel();
    JPanel bottomPanel = new JPanel();
    JLabel whosInLabel = new JLabel("Who's in the chat room:");
    JButton sendPublicButton = new JButton("Send to ALL");
    JTextField whosInTextField = new JTextField(48);
    JTextField errMsgTextField = new JTextField("Error messages will show here.");
    JTextArea sendChatArea = new JTextArea();
    JTextArea receiveChatArea = new JTextArea();
    JScrollPane sendScrollPane = new JScrollPane(sendChatArea);
    JScrollPane receiveScrollPane = new JScrollPane(receiveChatArea);
    JSplitPane chatSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, sendScrollPane, receiveScrollPane);

    //Menus
    MenuBar menuBar = new MenuBar();
    Menu fontMenu = new Menu("Font");
    Menu screenMenu = new Menu("Screen Orientation");
    MenuItem biggerFontMenuItem = new MenuItem("Bigger");
    MenuItem smallerFontMenuItem = new MenuItem("Smaller");
    MenuItem horizontalOrientationMenuItem = new MenuItem("Horizontal");
    MenuItem verticalOrientationMenuItem = new MenuItem("Vertical");

    public ChatRoomClient(String serverAddress, String chatName, String password) throws  Exception{
        if(serverAddress.contains(" ") || chatName.contains(" ") || password.contains(" ")){
            throw new IllegalArgumentException("Parameters may not contain blanks.");
        }
        System.out.println("Connecting to the chat room server at " + serverAddress + " on port 2222");
        try {
            s = new Socket(serverAddress, 2222);
        }
        catch(ConnectException ce){
            System.out.println("Connection to TherapyServer at " + serverAddress + " on port 2222 has failed");
            System.out.println("Is server address correct? Has the server been started on port 2222?");
            return; // terminate so user can fix and restart
        }
        System.out.println("Connected to the chat server!");
        oos = new ObjectOutputStream(s.getOutputStream());
        oos.writeObject(chatName + " " + password); // send the "join message"
        ois = new ObjectInputStream(s.getInputStream());
        String reply = (String) ois.readObject(); // wait for server response

        if(reply.startsWith("Welcome"))
            System.out.println("Join was successful");
        else
            throw new IllegalArgumentException("Join of " + chatName + " with password " + password + " was not successful");

        topPanel.add(whosInLabel);
        topPanel.add(whosInTextField);

        bottomPanel.add(sendPublicButton);
        bottomPanel.add(errMsgTextField);

        chatWindow.getContentPane().add(topPanel, "North");
        chatWindow.getContentPane().add(chatSplitPane, "Center");
        chatWindow.getContentPane().add(bottomPanel, "South");

        chatWindow.setTitle(chatName + "'s CHAT ROOM (Close this window to leave the chat room.)");

        sendPublicButton.setBackground(Color.green);
        whosInLabel.setForeground(Color.blue);

        receiveChatArea.setLineWrap(true);
        receiveChatArea.setWrapStyleWord(true);

        receiveChatArea.setEditable(false);
        errMsgTextField.setEditable(false);
        whosInTextField.setEditable(false);

        chatSplitPane.setDividerLocation(0.5);

        sendChatArea.setText("ENTER a message to send HERE, then push the send button below.");
        receiveChatArea.setText("VIEW received chat messages HERE (including the ones you sent.)" +
                                newLine + "The bar separating the IN and OUT areas can be moved.");

        sendPublicButton.addActionListener(this); // so sendPublicButton can call us!


        chatWindow.setSize(1000, 400);
        chatWindow.setLocation(400, 100);
        chatWindow.setVisible(true);

        // Add menus to ChatWindow
        chatWindow.setMenuBar(menuBar);
        menuBar.add(fontMenu);
        menuBar.add(screenMenu);
        fontMenu.add(biggerFontMenuItem);
        biggerFontMenuItem.addActionListener(this);
        fontMenu.add(smallerFontMenuItem);
        smallerFontMenuItem.addActionListener(this);
        screenMenu.add(verticalOrientationMenuItem);
        verticalOrientationMenuItem.addActionListener(this);
        screenMenu.add(horizontalOrientationMenuItem);
        horizontalOrientationMenuItem.addActionListener(this);

        Thread t = new Thread(this); // make an app thread (to do the receive function)
        t.start();


    }

    public static void main(String[] args){
        System.out.println("ChatRoomClient: Marcus Kok");
        if(args.length != 3){
            System.out.println("Restart, enter THREE parameters: \n 1. server address \n 2. chat name \n 3. password");
            return;
        }
        try {
            ChatRoomClient crc = new ChatRoomClient(args[0], args[1], args[2]);
            crc.loadedFromMain = true; // used at termination
        }
        catch(Exception e){
            System.out.println(e); // print exception object as error message
            return; // can't continue if can't load program!
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        errMsgTextField.setText("");
        errMsgTextField.setBackground(Color.white);
        if(ae.getSource() == sendPublicButton){
            String chatMessageToSend = sendChatArea.getText().trim();
            if(chatMessageToSend.length() == 0){ // blank message!
                errMsgTextField.setText("No message entered to send.");
                errMsgTextField.setBackground(Color.pink); // highlight to get attention
                return;
            }
            //System.out.println("Your message '" + chatMessageToSend + "' is being sent to the server");
            try{
                oos.writeObject(chatMessageToSend);
                sendChatArea.setText(""); // clear the input field. (indication to the user that the message was sent.)
            }
            catch (Exception e){ // server is down
                errMsgTextField.setText("Connection to the chat server has failed.");
                errMsgTextField.setBackground(Color.pink);
                sendChatArea.setEditable(false); // keep user from entering more messages to send
                sendPublicButton.setEnabled(false); // disable sendButton
            }
            return;
        }

        if(ae.getSource() == biggerFontMenuItem){ // increases font size in the sendChatArea and receiveChatArea
            if(fontSize < maxFontSize){
                fontSize += 1;
                sendChatArea.setFont(new Font("default", Font.BOLD, fontSize));
                receiveChatArea.setFont(new Font("default", Font.BOLD, fontSize));
            }
            return;
        }

        if(ae.getSource() == smallerFontMenuItem){ // decrease font size in the sendChatARea and receiveChatArea
            if(fontSize > minFontSize){
                fontSize -= 1;
                sendChatArea.setFont(new Font("default", Font.BOLD, fontSize));
                receiveChatArea.setFont(new Font("default", Font.BOLD, fontSize));
            }
            return;
        }

        if(ae.getSource() == horizontalOrientationMenuItem){
            // changes the bar that separates send and receive chat area from vertical to horizontal
            chatSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            chatSplitPane.setDividerLocation((double)0.5); // half screen point
            return;
        }

        if(ae.getSource() == verticalOrientationMenuItem){
            // changes bar that separates send and receive chat area from horizontal to vertical
            chatSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            chatSplitPane.setDividerLocation((double) 0.5); // half screen point
            return;
        }
    }
    public void run(){ // receive
        try{ Thread.sleep(100); // pause app thread to let constructor finish
        }
        catch(InterruptedException ie){}

        if(loadedFromMain){
            chatWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // shuts down the JVM!
        }
        else{
            chatWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // garbage collects window
        }

        try{
            while(true){
                // receive message from server and display on GUI
                String incomingChatMessage = (String) ois.readObject();
                if(incomingChatMessage.startsWith("[")){
                    incomingChatMessage = incomingChatMessage.substring(1); // drop leading "]"
                    whosInTextField.setText(incomingChatMessage);
                    continue; // back to loop top to read next message
                }
                receiveChatArea.append(newLine + incomingChatMessage);
                // auto-scroll the JScrollPane to bottom line so the last message will be visible.
                receiveChatArea.setCaretPosition(receiveChatArea.getDocument().getLength());
            }
        }
        catch (Exception e){
            // show error message to user
            errMsgTextField.setBackground(Color.pink);
            errMsgTextField.setText("CONNECTION TO THE CHAT ROOM SERVER HAS FAILED!" +
                    newLine + "You must close this chat window and then restart the client to reconnect to the server to" +
                    "continue");
            // disable the GUI
            sendChatArea.setEditable(false);
            sendPublicButton.setEnabled(false);

        }
        // after catch, t thread returns to Thread object and is terminated.

    }
}
