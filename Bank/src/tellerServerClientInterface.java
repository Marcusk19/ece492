import java.rmi.Remote;
import java.rmi.RemoteException;


public interface tellerServerClientInterface extends Remote
{
String createNewAccount(String  accountType, // CHECKING or SAVINGS
                        String  customerName) throws RemoteException;

String showAccount(int    accountNumber,     // if 0, show all accounts
		           String customerName)     throws RemoteException;

String processAccount(String processType,    //  DEPOSIT or WITHDRAW
		              int    accountNumber,
                      double amount,
                      String customerName)  throws RemoteException;

String closeOutAccount(int    accountNumber, // balance must be 0
                       String customerName) throws RemoteException;
}