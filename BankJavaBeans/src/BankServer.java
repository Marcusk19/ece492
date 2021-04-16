// Marcus Kok
import java.io.*;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Locale;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;




public class BankServer extends UnicastRemoteObject implements BankServerClientInterface {
    // instance variables
    private static ConcurrentHashMap<Integer, CashAccount> accounts = new ConcurrentHashMap<Integer, CashAccount>();
    // constructor
    public BankServer() throws Exception {
        LocateRegistry.createRegistry(1099);
        Naming.rebind("BankServer", this);
        System.out.println("BankServer is up at " + InetAddress.getLocalHost());

        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("accounts.ser"));
            accounts = (ConcurrentHashMap<Integer, CashAccount>) ois.readObject();
            ois.close();
        }
        catch(Exception e) {
            System.out.println(e);
        }
        System.out.println(accounts);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Marcus Kok");
        new BankServer();
    }

    public String createNewAccount(String accountType, String customerName) throws Exception {
        CashAccount ca;
        if(accountType.equals("CHECKING"))
            ca = new CheckingAccount(customerName);
        else if(accountType.equals("SAVINGS"))
            ca = new SavingsAccount(customerName);
        else return "ERROR: account type " + accountType + " is not recognized by the server."
                    + "Call the IT department!";
        accounts.put(ca.getAccountNumber(), ca); // add account to map
        System.out.println(accounts);
        // serialize accounts collection
        try{saveAccounts();}
        catch(Exception e) {
            System.out.println("Error serializing accounts collection " + e);
        }

        return ca.toString();
    }

    public String showAccount(int accountNumber, String customerName) {
        if(accountNumber == 0){
            TreeSet<String> hitList = new TreeSet();
            CashAccount[] accountList = accounts.values().toArray(new CashAccount[0]);
            for(CashAccount cashAccount : accountList){
                String name = cashAccount.getCustomerName().toUpperCase();
                String shortName = customerName.toUpperCase();
                if(name.startsWith(shortName))
                    hitList.add(cashAccount.getCustomerName() + " " + cashAccount.toString());
            }
            if(hitList.isEmpty())
                return "No accounts starting with the entered name were found.";
            else{
                String newLine = System.lineSeparator();
                String hitString = "The following accounts were found starting with the name " + customerName;
                for(String s : hitList){
                    hitString = hitString + newLine + s;
                }
                return hitString;
            }
        }
        CashAccount ca = accounts.get(accountNumber);
        if(ca == null) return "Account " + accountNumber + " not found.";
        if(ca.getCustomerName().equals(customerName)){
            return ca.toString();
        }
        else return "Provided name " + customerName + " does not match stored name for account number.";
    }

    public String processAccount(String processType, int accountNumber, double amount, String customerName) {
        CashAccount ca = accounts.get(accountNumber);
        if(ca == null) return "Account " + accountNumber + " not found.";
        if(!(ca.getCustomerName().equals(customerName))){
            return "Provided name " + customerName + " does not match stored name for account number.";
        }
        if(processType.equals("DEPOSIT")){
            ca.deposit(amount);
            try {
                saveAccounts();
            } catch(Exception e){
                return e.getMessage();
            }
            return ca.toString();
        }
        if(processType.equals("WITHDRAW")){
            try{
                ca.withdraw(amount);
            } catch(Exception e){
                return e.getMessage();
            }
            try{
                saveAccounts();
            } catch(Exception e){
                return e.getMessage();
            }
            return ca.toString();
        }
        else return "ERROR: transaction type: " + processType + " is not recognized by the server.";
    }

    public String closeOutAccount(int accountNumber, String customerName) {
        CashAccount ca = accounts.get(accountNumber);
        if(ca == null) return "Account " + accountNumber + " not found.";
        if(!(ca.getCustomerName().equals(customerName))){
            return "Provided name " + customerName + " does not match stored name for account number.";
        }
        if(ca.getBalance() != 0){
            return "Balance must be $0.00 to close out.";
        }
        accounts.remove(accountNumber);
        try{
            saveAccounts();
        } catch(Exception e){
            return e.getMessage();
        }
        return "Termination of account " + accountNumber + " successful.";
    }

    private static  synchronized void saveAccounts() throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("accounts.ser"));
        oos.writeObject(accounts);
        oos.close();
    }
}

