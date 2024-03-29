import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class EchoTellerServer extends UnicastRemoteObject implements tellerServerClientInterface {

    public EchoTellerServer() throws Exception {
        super(); // call constructor of UnicastRemoteObject to load _Stub and _Skel!
        LocateRegistry.createRegistry(1099); // find/start rmiregistry on this computer
        Naming.rebind("EchoTellerserver", this); // register "this" server's STUB with the rmiregistry
        System.out.println("EchoTellerServer is up at " + InetAddress.getLocalHost().getHostAddress());
        
    }
}
