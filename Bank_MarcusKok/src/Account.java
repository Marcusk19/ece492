// Marcus Kok
import java.io.*;

public abstract class Account implements Serializable {
    // Java Bean to hold bank account data for a specific customer
    private String customerName; // Note that INITIAL values are NOT provided!
    private int accountNumber; // This is the TEMPLATE to create a new EMPTY Account Bean
    private  static  int lastAccountNumber; // memory "cache" of the next account # to be assigned to an Account

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName){
        this.customerName = customerName;
    }

    public String toString() {
        return getClass().getName() + "  #" + getAccountNumber() + " for " + getCustomerName();
    }

    public Account(){
        // default 'no-parms' constructor
        System.out.println("Executing code in the Account default constructor to assign an account number");
        if(lastAccountNumber == 0)
            // initialize lastAccountNumber in STATIC from lastAccountNumber on data base (only done once)
            try {
                accountNumber = getNextAccountNumber(); // call static method
            }
            catch(Exception e){
                System.out.println(e);
            }
        // assign an account number
    }

    public Account(String customerName){ // another "overloaded" constructor
        this(); // call the default constructor to get an accountNumber assigned to this account
        setCustomerName(customerName); // call own setter method
        try{
            accountNumber = getNextAccountNumber();
        }
        catch(Exception e){}
    }

    public int getAccountNumber(){ return accountNumber;}

    private static synchronized int getNextAccountNumber() throws  Exception{
        if(lastAccountNumber == 0){
            // initialize lastAccountNumber from "DB"
           ObjectInputStream ois = new ObjectInputStream(new FileInputStream("LastAccountNumber.ser"));
            lastAccountNumber = (int) ois.readObject();
            ois.close();
        }
        lastAccountNumber++;  // bump last number to create next number
        // write the updated lastAccountNumber to "DB"
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("LastAccountNumber.ser"));
        oos.writeObject(lastAccountNumber);
        oos.close();
        return lastAccountNumber;
    }

}
