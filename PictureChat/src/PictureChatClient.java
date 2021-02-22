// Marcus Kok 200235945

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Vector;

public class PictureChatClient implements ActionListener, Runnable, ListSelectionListener {
    Socket s;
    ObjectOutputStream oos;
    ObjectInputStream ois;
    String  newLine        = System.lineSeparator();
    int     fontSize       = 20;
    int     maxFontSize    = 50;
    int     minFontSize    = 5;
    boolean loadedFromMain = false;
    Vector<String> blackList = new Vector<String>();
    String localChatName;
    // instructions String for showInstructionsWindow
    String instructions = "SENDING & RECEIVING CHAT MESSAGES"                                                        + newLine
            + "The bar that separates the SEND area and the RECEIVE area in the main chat window is moveable."       + newLine
            + "Also you can set the split to horitontal or vertical using the ScreenOrientation pull-down menu."     + newLine
            + "All chat messages you receive (including ones you send) will be shown in the right / bottom area."    + newLine
            + "ENTER chat messages to send in the left / top area and then push the green or yellow SEND button "    + newLine
            + "at the bottom of the screen."                                                                         + newLine
            + newLine
            + "WHO'S IN THE CHAT ROOM?"                                                                              + newLine
            + "Pushing the Who'sIn button will bring up a screen that shows everyone else currently in the chat"     + newLine
            + "room and also everyone that has previously been in. This screen also lets you indicate people you"    + newLine
            + "would like to IGNORE."                                                                                + newLine
            + newLine
            + "MAKING SELECTIONS FROM THE LISTS"                                                                     + newLine
            + "PRIVATE & SAVE-FOR selections will be cleared after the send."                                        + newLine
            + "IGNORE selections must be manually saved by pressing the SAVE button at the bottom of the list." + newLine
            + "To select multiple recipients from a list (or deselect someone) hold down the Ctrl key while selecting."   + newLine
            + newLine
            + "LEAVING A MESSAGE FOR SOMEONE NOT CURRENTLY IN THE CHAT ROOM"                                         + newLine
            + "You can select one or more people in the WHO'S NOT IN list and then use the yellow PRIVATE send key." + newLine
            + "The message will be saved on disk and shown to them the next time they join."                         + newLine
            + "You can add save-for recipients to either a PUBLIC or PRIVATE message to IN recipients."              + newLine
            + newLine
            + "SENDING A PRIVATE MESSAGE"                                                                            + newLine
            + "You can select recipients of a PRIVATE message from the who's-in list and then use the yellow PRIVATE"+ newLine
            + "send key. PRIVATE & SAVE-FOR (NOT-IN) recipients are automatically cleared after send."               + newLine
            + "TIP: type your message BEFORE selecting the recipients in case the whosIn list is restored..."        + newLine
            + newLine
            + "SELECTING PEOPLE TO IGNORE"                                                                           + newLine
            + "Incoming messages from people you 'ignore' will not be shown to you. But PUBLIC messages you send"    + newLine
            + "will be shown to them. Your ignore list will be remembered when you next join. After you SELECT"      + newLine
            + "people to ignore you must SAVE the list by pressing the SAVE button at the top of the list."          + newLine
            + newLine
            + "SENDING PICTURES"                                                                                     + newLine
            + "This feature is not implemented yet. When it is, you will be able to inspect pictures in the local"   + newLine
            + "directory and select one to send and attach a description to it."
            ;
    // non-GUI pointers for pictureChat
    ImageIcon selectedPicture;
    Image selectedImage;
    String selectedDescription;
    File localDirectory = new File(System.getProperty("user.dir"));

    // Chat Window
    JButton loadMyPicturesButton = new JButton("Load my pictures from disk.");
    JList<ImageIcon> picturesList = new JList<ImageIcon>();
    JScrollPane picturesScrollPane = new JScrollPane(picturesList);
    JPanel splitPanePanel = new JPanel();
    JFrame chatWindow = new JFrame();
    JPanel topPanel = new JPanel();
    JPanel bottomPanel = new JPanel();
    JButton sendPublicButton = new JButton("Send to ALL");
    JButton sendPrivateButton = new JButton("Send Private");
    JButton whosInButton = new JButton("who's in the chat room");
    JButton showInstructionsButton = new JButton("show instructions");
    JTextField whosInTextField = new JTextField(48);
    JTextField errMsgTextField = new JTextField("Error messages will show here.");
    JTextArea sendChatArea = new JTextArea();
    JTextArea receiveChatArea = new JTextArea();
    JScrollPane sendScrollPane = new JScrollPane(sendChatArea);
    JScrollPane receiveScrollPane = new JScrollPane(receiveChatArea);
    JSplitPane chatSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, splitPanePanel, receiveScrollPane);

    // Whos in Window
    JFrame whosInWindow = new JFrame();
    JLabel whosInLabel = new JLabel("Who else is *IN* the chat room."); // north
    JLabel whosNotInLabel = new JLabel("Who is NOT in right now.");     // north
    JButton savedIgnoreButton = new JButton("SAVE the IGNORED list");  // north
    JLabel selectPrivateLabel = new JLabel("SELECT PRIVATE recipients.");  // south
    JLabel selectSaveForLabel = new JLabel("SELECT SAVE-FOR recipients."); // south
    JButton selectIgnoreButton = new JButton("SELECT people to be IGNORED"); // south
    JPanel whosInTopPanel = new JPanel();
    JPanel whosInBotPanel = new JPanel();
    JPanel whosInCenterPanel = new JPanel();
    JList<String> whosInList = new JList<>();
    JList<String> whosNotInList = new JList<>();
    JList<String> ignoreList = new JList<>();
    JScrollPane whosInScrollPane = new JScrollPane(whosInList);
    JScrollPane whosNotInScrollPane = new JScrollPane(whosNotInList);
    JScrollPane ignoreScrollPane = new JScrollPane(ignoreList);

    // Instructions
    JFrame showInstructionsWindow = new JFrame();
    JTextArea showInstructionsTextArea = new JTextArea();
    JScrollPane showInstructionsScrollPane = new JScrollPane(showInstructionsTextArea);

    //Menus
    MenuBar menuBar = new MenuBar();
    Menu fontMenu = new Menu("Font");
    Menu screenMenu = new Menu("Screen Orientation");
    MenuItem biggerFontMenuItem = new MenuItem("Bigger");
    MenuItem smallerFontMenuItem = new MenuItem("Smaller");
    MenuItem horizontalOrientationMenuItem = new MenuItem("Horizontal");
    MenuItem verticalOrientationMenuItem = new MenuItem("Vertical");

//=============================== Constructor ==========================================================================

    public PictureChatClient(String serverAddress, String chatName, String password) throws  Exception{
        localChatName = chatName;
        if(serverAddress.contains(" ") || chatName.contains(" ") || password.contains(" ")){
            throw new IllegalArgumentException("Parameters may not contain blanks.");
        }
        System.out.println("Connecting to the chat room server at " + serverAddress + " on port 5555");
        try {
            s = new Socket(serverAddress, 5555);
        }
        catch(ConnectException ce){
            System.out.println("Connection to PictureChatServer at " + serverAddress + " on port 5555 has failed");
            System.out.println("Is server address correct? Has the server been started on port 5555?");
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

        topPanel.add(whosInButton);
        topPanel.add(loadMyPicturesButton);
        topPanel.add(showInstructionsButton);
        //topPanel.add(errMsgTextField);
        topPanel.setLayout(new GridLayout(1, 3));

        splitPanePanel.add(sendChatArea);
        splitPanePanel.add(picturesScrollPane);
        splitPanePanel.setLayout(new GridLayout(1,2));

        bottomPanel.add(sendPublicButton);
        bottomPanel.add(sendPrivateButton);
        bottomPanel.add(errMsgTextField);
        bottomPanel.setLayout(new GridLayout(1, 3));

        chatWindow.getContentPane().add(topPanel, "North");
        chatWindow.getContentPane().add(chatSplitPane, "Center");
        chatWindow.getContentPane().add(bottomPanel, "South");

        chatWindow.setTitle(chatName + "'s CHAT ROOM. Local directory is " + localDirectory + " *** Close this window to leave the chat room ***");

        sendPublicButton.setBackground(Color.green);
        sendPrivateButton.setBackground(Color.yellow);

        sendChatArea.setLineWrap(true);
        sendChatArea.setWrapStyleWord(true);

        receiveChatArea.setLineWrap(true);
        receiveChatArea.setWrapStyleWord(true);

        receiveChatArea.setEditable(false);
        //errMsgTextField.setEditable(false); //removed to allow user to scroll through long err message
        whosInTextField.setEditable(false);

        whosInButton.setBackground(Color.cyan);
        loadMyPicturesButton.setBackground(Color.blue);
        loadMyPicturesButton.setForeground(Color.white); // white print on dark background

        picturesList.setSelectionMode(0); // SINGLE select mode

        receiveChatArea.setText("VIEW received chat messages HERE (including the ones you sent.)" +
                                newLine + "The bar separating the IN and OUT areas can be moved.");

        sendPublicButton.addActionListener(this); // so sendPublicButton can call us!
        sendPrivateButton.addActionListener(this);
        showInstructionsButton.addActionListener(this);
        selectIgnoreButton.addActionListener(this);
        savedIgnoreButton.addActionListener(this);
        loadMyPicturesButton.addActionListener(this);
        picturesList.addListSelectionListener(this);

        chatWindow.setSize(1000, 400);
        chatWindow.setLocation(400, 100);
        chatSplitPane.setDividerLocation(460);
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

        // set up whos in Window
        whosInTopPanel.setLayout(new GridLayout(1, 3));
        whosInCenterPanel.setLayout(new GridLayout(1, 3));
        whosInBotPanel.setLayout(new GridLayout(1, 3));

        whosInTopPanel.add(whosInLabel);
        whosInTopPanel.add(whosNotInLabel);
        whosInTopPanel.add(savedIgnoreButton);
        whosInBotPanel.add(selectPrivateLabel);
        whosInBotPanel.add(selectSaveForLabel);
        whosInBotPanel.add(selectIgnoreButton);

        whosInCenterPanel.add(whosInScrollPane);
        whosInCenterPanel.add(whosNotInScrollPane);
        whosInCenterPanel.add(ignoreScrollPane);

        whosInWindow.getContentPane().add(whosInTopPanel, "North");
        whosInWindow.getContentPane().add(whosInCenterPanel, "Center");
        whosInWindow.getContentPane().add(whosInBotPanel, "South");

        whosInWindow.setSize(1000, 400);
        whosInWindow.setLocation(400, 500);

        whosInButton.addActionListener(this);

        whosInList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        whosNotInList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        ignoreList.setEnabled(false);
        // set up showInstructions window
        showInstructionsWindow.getContentPane().add(showInstructionsScrollPane, "Center");
        showInstructionsTextArea.setText(instructions);
        showInstructionsTextArea.setEditable(false);
        showInstructionsWindow.setTitle("Instructions");
        showInstructionsWindow.setSize(500, 600);

        Thread t = new Thread(this); // make an app thread (to do the receive function)
        t.start();


    }
//=============================== Main =================================================================================

    public static void main(String[] args){
        System.out.println("PictureChatClient: by Marcus Kok");
        if(args.length != 3){
            System.out.println("Restart, enter THREE parameters: \n 1. server address \n 2. chat name \n 3. password");
            return;
        }
        try {
            PictureChatClient crc = new PictureChatClient(args[0], args[1], args[2]);
            crc.loadedFromMain = true; // used at termination
        }
        catch(Exception e){
            System.out.println(e); // print exception object as error message
            return; // can't continue if can't load program!
        }
    }
//====================== Action Performed ==============================================================================
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
            /*
            if((!whosInList.isSelectionEmpty()) || (!whosNotInList.isSelectionEmpty())){
                errMsgTextField.setText("PUBLIC send button was pushed but PRIVATE recipients are selected.");
                errMsgTextField.setBackground(Color.pink);
                return;
            }*/
            if(!whosNotInList.isSelectionEmpty()){
                java.util.List<String> saveForRecipientsList = whosInList.getSelectedValuesList();
                Vector<Object> selectedRecipientsVector = new Vector<Object>(saveForRecipientsList);
                if(selectedPicture == null) // a picture was not selected
                    selectedRecipientsVector.add(0, chatMessageToSend); // ad text message at top of list
                else
                    selectedRecipientsVector.add(0, selectedPicture); // add picture at top of list
                Object[] saveForRecipientsArray = selectedRecipientsVector.toArray(new Object[0]);
                send(saveForRecipientsArray);
                whosNotInList.clearSelection();

            }
            if(selectedPicture == null) { // a picture has NOT been selected to send
                System.out.println("Your message '" + chatMessageToSend + "' is being sent to the server");
                send(chatMessageToSend);
            } else { // a picture has been selected
                // see if user wants to replace the description in the ImageIcon
                selectedDescription = selectedPicture.getDescription(); // default
                System.out.println("selectedDescription: " + selectedDescription);
                String alteredDescription = sendChatArea.getText().trim();
                System.out.println("alteredDescription: " + alteredDescription);
                if (alteredDescription.startsWith("The selected picture will be sent with the default description")){
                    selectedPicture.setDescription(localChatName + " sends " + selectedDescription);
                    System.out.println("Description: " + selectedPicture.getDescription());
                } else {
                    selectedPicture.setDescription(localChatName + " sends " + alteredDescription);
                    System.out.println("Description: " + selectedPicture.getDescription());
                }
                send(selectedPicture);
                System.out.println("Picture: " + selectedPicture + " sent to server.");
                selectedPicture.setDescription(selectedDescription); // restore original description in JList ImageIcon
                picturesList.clearSelection();
                selectedPicture = null; // reset selected picture indicator
            }

            sendChatArea.setText(""); // clear the input field. (indication to the user that the message was sent.)
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

        if(ae.getSource() == sendPrivateButton){
            java.util.List<String> privateRecipientsList = whosInList.getSelectedValuesList();
            java.util.List<String> saveRecipientsList = whosNotInList.getSelectedValuesList();

            System.out.println(privateRecipientsList.size() + " PRIVATE message recipients are: " + privateRecipientsList);
            System.out.println(saveRecipientsList.size() + " SAVE-FOR message recipients are: " + saveRecipientsList);
            String privateOrSaveMessage = sendChatArea.getText().trim();
            if(privateOrSaveMessage.length() == 0){
                errMsgTextField.setText("zero-length PRIVATE/SAVE message entered.");
                errMsgTextField.setBackground(Color.pink);
                return;
            }
            if((whosInList.isSelectionEmpty()) && (whosNotInList.isSelectionEmpty())){
                // no selections in either list
                errMsgTextField.setText("The sendPrivateMessage button was pushed but no recipients are selected.");
                errMsgTextField.setBackground(Color.pink);
                return;
            }
            System.out.println("PRIVATE/SAVE message entered: " + privateOrSaveMessage);
            sendChatArea.setText("");
            whosInList.clearSelection();
            whosNotInList.clearSelection();

            String[] privateAndSaveMessageArray = new String[privateRecipientsList.size() + saveRecipientsList.size() + 1];
            if(privateRecipientsList.size() > 0){
                privateAndSaveMessageArray[0] = "PRIVATE " + privateOrSaveMessage;
            }
            else {
                privateAndSaveMessageArray[0] = privateOrSaveMessage;
            }
            int i = 1;
            for(String recipient : privateRecipientsList) privateAndSaveMessageArray[i++] = recipient;
            for(String recipient : saveRecipientsList) privateAndSaveMessageArray[i++] = recipient;
            try{
                oos.writeObject(privateAndSaveMessageArray);
            }
            catch(Exception e){}
            return;
        }
        if(ae.getSource() == showInstructionsButton){
            //System.out.println("showInstructionsButton pushed");
            showInstructionsWindow.setVisible(true);
            return;
        }
        if(ae.getSource() == whosInButton){
            whosInWindow.setVisible(true);
            return;
        }
        if(ae.getSource() == selectIgnoreButton){

            ignoreList.setEnabled(true);
            Vector<String> everyone = new Vector<String>();
            for(int i = 0; i < whosInList.getModel().getSize(); i++){
                everyone.add(whosInList.getModel().getElementAt(i));
            }
            for(int i = 0; i < whosNotInList.getModel().getSize(); i++) {
                everyone.add(whosNotInList.getModel().getElementAt(i));
            }
            ignoreList.setListData(everyone);
            return;
        }
        if(ae.getSource() == savedIgnoreButton){

            java.util.List<String> selectedIgnoreNames = ignoreList.getSelectedValuesList();
            String[] whosIgnoredArray = new String[selectedIgnoreNames.size() + 1];
            whosIgnoredArray[0] = "ignore";
            int i = 1;
            for(String name : selectedIgnoreNames){
                whosIgnoredArray[i++] = name;
            }
            try{oos.writeObject(whosIgnoredArray);
                System.out.println("sent " + Arrays.toString(whosIgnoredArray) + " to server");}
            catch(Exception e){}
            String[] updateArray = new String[selectedIgnoreNames.size()];
            blackList.clear();
            // update blacklist and ignore panel
            for(int j = 0; j < selectedIgnoreNames.size(); j++){
                updateArray[j] = selectedIgnoreNames.get(j);
                blackList.add(selectedIgnoreNames.get(j));
            }
            ignoreList.setListData(updateArray);
            ignoreList.setEnabled(false);
            return;
        }
        if(ae.getSource() == loadMyPicturesButton){
            String[] listOfFiles = localDirectory.list();
            Vector<ImageIcon> pictures = new Vector<ImageIcon>();
            for(String fileName : listOfFiles){pictures.add(new ImageIcon(fileName, fileName));}

            if(pictures.isEmpty()){ // are there no pictures?
                errMsgTextField.setText("No .jpg .gif or .png picture files were found in " + localDirectory);
                errMsgTextField.setBackground(Color.pink);
                return;
            }
            picturesList.setListData(pictures); // show Image Icons in Jlist on GUI

        }
    }
//==================================== Run =============================================================================
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
                Object somethingFromServer = ois.readObject();
                if(somethingFromServer instanceof ImageIcon){
                    ImageIcon picture = (ImageIcon) somethingFromServer;
                    System.out.println("received: " + picture);
                    showIncomingPicture(picture);
                }
                if(somethingFromServer instanceof String) {
                    boolean notBlocked = true;
                    String chatMessage = (String) somethingFromServer;
                    // code goes here to handle chat messages using chatMessage pointer
                    for(String name : blackList){
                        if(chatMessage.startsWith(name)) {
                            System.out.println("blocked message from " + name);
                            notBlocked = false;
                        };
                    }
                    if(notBlocked) {
                        receiveChatArea.append(newLine + chatMessage);
                        // auto-scroll the JScrollPane to bottom line so the last message will be visible.
                        receiveChatArea.setCaretPosition(receiveChatArea.getDocument().getLength());
                    }
                }
                if(somethingFromServer instanceof  String[]){
                    String[] whosInOrOutArray = (String []) somethingFromServer;
                    System.out.println("Received array-of-strings: " + Arrays.toString(whosInOrOutArray));
                    // code goes here to handle received arrays using the whosInOrOutArray pointer
                    String[] updateArray = new String[whosInOrOutArray.length];
                    if(whosInOrOutArray.length > 1 || whosInOrOutArray[0].equals("ignore")) {
                        for (int i = 1; i < whosInOrOutArray.length; i++) {
                            String chatName = whosInOrOutArray[i];
                            if (chatName.equals(localChatName.toUpperCase())) continue;
                            else {
                                updateArray[i-1] = chatName;
                            }
                            if(i >= updateArray.length) break;
                        }
                    }
                        // if length == 2 and is not an ignore array, no one else is in chat room. update with empty array
                        if (whosInOrOutArray[0].equals("whosin")) whosInList.setListData(updateArray);
                        else if (whosInOrOutArray[0].equals("whosout")) whosNotInList.setListData(updateArray);
                        else if (whosInOrOutArray[0].equals("ignore")){
                            ignoreList.setListData(updateArray);
                            blackList.clear();
                            for(String name : updateArray){
                                if(name != null) {
                                    blackList.add(name);
                                }
                            }
                            System.out.println("blacklist for " + localChatName + blackList);
                        }
                }

            }
        }
        catch (Exception e){
            // show error message to user
            System.out.println("Error: " + e);
            errMsgTextField.setBackground(Color.pink);
            errMsgTextField.setText("CONNECTION TO THE CHAT ROOM SERVER HAS FAILED!" +
                    newLine + "You must close this chat window and then restart the client to reconnect to the server to" +
                    "continue");
            // disable the GUI
            sendChatArea.setEditable(false);
            sendPublicButton.setEnabled(false);
            sendPrivateButton.setEnabled(false);

        }
        // after catch, t thread returns to Thread object and is terminated.

    }
//============================== Methods ===============================================================================
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if(picturesList.getValueIsAdjusting()) return; // user is still selecting!
        selectedPicture = picturesList.getSelectedValue();
        if(selectedPicture == null) return;

        selectedDescription = selectedPicture.getDescription();
        System.out.println(selectedDescription + " was selected."); // show description
        sendChatArea.setText("The selected picture will be sent with the default description: '"
                                + selectedDescription + "' when you push a send button." + newLine
                                + "If you want to replace this description, ERASE this space "
                                + " and enter another description here. Then push a send button.");



    }

    private void showIncomingPicture(ImageIcon receivedPicture){
        JFrame receivedPictureWindow = new JFrame();
        receivedPictureWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        receivedPictureWindow.setTitle(receivedPicture.getDescription());

        Image picture = receivedPicture.getImage(); // get access to Image that ImageIcon contains
        int pictureWidth = picture.getWidth(receivedPictureWindow);
        int pictureHeight = picture.getHeight(receivedPictureWindow);
        receivedPictureWindow.setSize(pictureWidth, pictureHeight); // set window size to picture size
        JPanel picturePanel = new RefreshingPicturePanel(picture);
        receivedPictureWindow.getContentPane().add(picturePanel, "Center");
        receivedPictureWindow.setLocation(chatWindow.getLocation().x, chatWindow.getLocation().y);
        receivedPictureWindow.setVisible(true);
    }

    private void send(Object message){
        try {
            oos.reset();
            oos.writeObject(message);
        } catch (Exception e) { // server is down
            errMsgTextField.setText(e + ": Connection to the chat server has failed.");
            errMsgTextField.setBackground(Color.pink);
            sendChatArea.setEditable(false); // keep user from entering more messages to send
            sendPublicButton.setEnabled(false); // disable sendButton
        }
    }
}
