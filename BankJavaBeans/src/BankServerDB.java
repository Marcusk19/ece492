// Marcus Kok
import java.io.*;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;


public class BankServerDB extends UnicastRemoteObject implements BankServerClientInterface {
    // instance variables
    private static ConcurrentHashMap<Integer, CashAccount> accounts = new ConcurrentHashMap<Integer, CashAccount>();
    private Connection connection;
    private PreparedStatement insertStatement;
    private PreparedStatement updateStatement;
    private PreparedStatement deleteStatement;
    private Statement selectAllStatement;



    // constructor
    public BankServerDB() throws Exception {
        LocateRegistry.createRegistry(1099);
        Naming.rebind("BankServer", this);
        System.out.println("BankServer is up at " + InetAddress.getLocalHost());
        // restore accounts collection from database
        Class.forName("com.ibm.db2j.jdbc.DB2jDriver");
        System.out.println("DB driver loaded!");
        connection = DriverManager.getConnection("jdbc:db2j:C:\\DataBase\\QuoteDB");
        System.out.println("DB opened!");
        // ask Connection object to make PreparedStatement object
        insertStatement = connection.prepareStatement(
                "INSERT INTO BANK_ACCOUNTS "
                        + "(ACCOUNT_NUMBER, ACCOUNT_TYPE, CUSTOMER_NAME, BALANCE) "
                        + "VALUES (?,?,?,?)");

        updateStatement = connection.prepareStatement(
                "UPDATE BANK_ACCOUNTS "
                        + "SET BALANCE = ? "
                        + "WHERE ACCOUNT_NUMBER = ?");

        deleteStatement = connection.prepareStatement(
                "DELETE FROM BANK_ACCOUNTS "
                        + "WHERE ACCOUNT_NUMBER = ?");

        selectAllStatement = connection.createStatement();
        ResultSet rs = selectAllStatement.executeQuery("SELECT * FROM BANK_ACCOUNTS");

        while (rs.next()) // read the next row of the ResultSet
        {
            // get the column values for this row
            int    accountNumber = rs.getInt   ("ACCOUNT_NUMBER");
            String accountType   = rs.getString("ACCOUNT_TYPE");
            String customerName  = rs.getString("CUSTOMER_NAME");
            double balance       = rs.getDouble("BALANCE");

            System.out.println(" acct#="    + accountNumber
                    + " acctType=" + accountType
                    + " custName=" + customerName
                    + " balance="  + balance);
            CashAccount ca;
            if (accountType.equals("CHECKING"))
                ca = CheckingAccount.restoreFromDataBase(customerName, accountNumber, balance);
            else if (accountType.equals("SAVINGS"))
                ca = SavingsAccount.restoreFromDataBase(customerName, accountNumber, balance);
            else    {
                System.out.println("SYSTEM ERROR: account type " + accountType + " is not recognized when reading DB table BANK_ACCOUNTS in server constructor.");
                continue; // skip unrecognized account
            }
            accounts.put(accountNumber, ca);
        }
        System.out.println(accounts);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Marcus Kok");
        new BankServerDB();
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
        // Add a new row to the DB table for this new account
        try {
            insertStatement.setInt   (1, ca.getAccountNumber());
            insertStatement.setString(2, accountType);
            insertStatement.setString(3, customerName);
            insertStatement.setDouble(4, 0); // initial balance for a new account
            insertStatement.executeUpdate();
        }
        catch(SQLException sqle)
        {
            return "ERROR: Unable to add new account to the data base."
                    + sqle.toString();
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
                updateStatement.setDouble (1, ca.getBalance());
                updateStatement.setInt    (2, ca.getAccountNumber());
                updateStatement.executeUpdate();
            }
            catch(SQLException sqle)
            {
                return "ERROR: Server is unable to update account in the data base."
                        + sqle.toString();
            }
            return ca.toString();
        }
        if(processType.equals("WITHDRAW")){
            try{
                ca.withdraw(amount);
            } catch(Exception e){
                return e.getMessage();
            }

            try {
                updateStatement.setDouble (1, ca.getBalance());
                updateStatement.setInt    (2, ca.getAccountNumber());
                updateStatement.executeUpdate();
            }
            catch(SQLException sqle)
            {
                return "ERROR: Server is unable to update account in the data base."
                        + sqle.toString();
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
        try {
            deleteStatement.setInt(1, accountNumber);
            deleteStatement.executeUpdate();
        }
        catch(SQLException sqle)
        {
            return "ERROR: Server is unable to delete account from the data base."
                    + sqle.toString();
        }
        return "Termination of account " + accountNumber + " successful.";
    }

}

