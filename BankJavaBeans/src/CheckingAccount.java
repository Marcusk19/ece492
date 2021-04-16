import java.math.BigDecimal;
import java.math.MathContext;


public class CheckingAccount extends CashAccount implements CheckingAccountInterface
{
    private double feesPaid;

    public CheckingAccount() throws Exception
    {
        super();
    }

    public CheckingAccount(String customerName) throws Exception
    {
        super(customerName);
    }

    private CheckingAccount(String customerName, int accountNumber, double balance) // added for DB
    {
        super(customerName, accountNumber, balance);
    }

    public static CheckingAccount restoreFromDataBase(String customerName, int accountNumber, double balance) // added for DB
    {
        return new CheckingAccount(customerName, accountNumber, balance);
    }

    @Override
    public void deposit(double amount)
    {
        if (amount >= 1000) System.out.println("Calling CrimeWatch.gov $" + amount + " deposit on account #" + getAccountNumber());
        super.deposit(amount);
    }

    public double getFeesPaid()
    {
        return feesPaid;
    }

    public void payFee(double fee) throws OverdraftException
    {
        if (fee < 0) throw new IllegalArgumentException("fee amount must be positive.");
        withdraw(fee);
        feesPaid += fee; // count it only if withdraw was successful!
    }

    @Override
    public String toString()
    {
        BigDecimal feesBD = new BigDecimal( getFeesPaid(), MathContext.DECIMAL32);//set PRECISION to 7 digits
        feesBD = feesBD.setScale(2,BigDecimal.ROUND_DOWN); //SCALE is # of digits after the period
        String feesString = " fees paid to date is $" + feesBD.toPlainString(); // no-exponent notationgetI
        return "Checking" + super.toString() + feesString;
    }
}