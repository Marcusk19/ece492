import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class EchoTellerServer extends UnicastRemoteObject implements TellerServerClientInterface {
    public EchoTellerServer() throws Exception{
        super(); // call constructor of UnicastRemoteObject to load _Stub and _Skel!
        LocateRegistry.createRegistry(1099); // find/start the rmiregistry on this computer
        Naming.rebind("EchoTellerServer", this); // register "this" server's STUB with the local rmiregistry
        System.out.println("EchoTellerServer is up at " + InetAddress.getLocalHost()); // no need to show RMI port
    }

    public static void main(String[] args) throws  Exception{
        EchoTellerServer es = new EchoTellerServer();
    }

    public String createNewAccount(String accountType, String customerName){
        return "A new " + accountType + " will be created for " + customerName;
    }

    public String showAccount(int accountNumber, String customerName){
        return " Account number: " + accountNumber + " belongs to " + customerName;
    }

    public String processAccount(String processType, int accountNumber, double amount, String customerName){
        return " Process " + processType + " for " + accountNumber + " with amount " + amount + " by " + customerName;
    }

    public String closeOutAccount(int accountNumber, String customerName){
        return " Closed account " + accountNumber + " by " + customerName;
    }
}
