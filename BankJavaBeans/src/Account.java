import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public abstract class Account implements Serializable, AccountInterface
{
// Things to do when writing a bean:
// 1. Declare the data fields (private)
// 2. provide a getter & setter for each field.
// 3. provide a toString() method to "introduce yourself"
// 4. provide constructors which take parameters to initialize fields.
// 5. implement the Serializable interface to give permission to externalize

    // Declare STATIC field
    private static int lastAccountNumber; // 0 if not yet initialized from data base

    // STATIC method
    private static synchronized int getNextAccountNumber() throws Exception
    {
        if (lastAccountNumber == 0)
        {
            // initialize lastAccountNumber from DB
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("LastAccountNumber.ser"));
            lastAccountNumber = (int) ois.readObject();
            ois.close();
        }
        lastAccountNumber++; // bump to next number for new account
        // write the updated lastAccountNumber to the DB
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("LastAccountNumber.ser"));
        oos.writeObject(lastAccountNumber);
        oos.close();
        return lastAccountNumber;
    }

    // Instance variables (in our program object) // no initial values!
    private String customerName;
    private int    accountNumber;

    public Account() throws Exception // DEFAULT constructor
    {
        accountNumber = getNextAccountNumber(); // call synchronized static method.
    }

    public Account(String customerName) throws Exception
    {
        this(); // Call my other constructor that has no parameters.
        setCustomerName(customerName);
    }

    protected Account(String customerName, int accountNumber) // added for DB
    {
        setCustomerName(customerName);
        this.accountNumber = accountNumber;
    }

    public int getAccountNumber()
    {
        return accountNumber;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public String toString() // the "introduce yourself" method!
    {
        return getClass().getName() + " #" + getAccountNumber() + " for " + getCustomerName();
    }
}