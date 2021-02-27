import java.rmi.Naming;

public class EchoTellerClient{
    public static void main(String[] args) throws Exception{
        TellerServerClientInterface server = (TellerServerClientInterface) Naming.lookup("rmi://localhost/EchoTellerServer");

        System.out.println(server.createNewAccount("savings", "Marcus"));
        System.out.println(server.closeOutAccount(1234, "Marcus"));
        System.out.println(server.showAccount(1234, "Bob"));
        System.out.println(server.processAccount("add", 1256, 12.5,"Steve"));
    }
}
