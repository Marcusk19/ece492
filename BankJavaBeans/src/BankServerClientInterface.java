//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BankServerClientInterface extends Remote{
    String createNewAccount(String var1, String var2) throws Exception;

    String showAccount(int var1, String var2) throws RemoteException;

    String processAccount(String var1, int var2, double var3, String var5) throws RemoteException;

    String closeOutAccount(int var1, String var2) throws RemoteException;

}
