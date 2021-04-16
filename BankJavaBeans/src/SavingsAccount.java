import java.math.BigDecimal;
import java.math.MathContext;


public class SavingsAccount extends CashAccount implements CashAccountInterface
{
    private double interestReceived;

    public SavingsAccount() throws Exception
    {
        super();
    }

    public SavingsAccount(String customerName) throws Exception
    {
        super(customerName);
    }

    private SavingsAccount(String customerName, int accountNumber, double balance) // added for DB
    {
        super(customerName, accountNumber, balance);
    }

    public static SavingsAccount restoreFromDataBase(String customerName, int accountNumber, double balance) // added for DB
    {
        return new SavingsAccount(customerName, accountNumber, balance);
    }

    public double getInterestReceived()
    {
        return interestReceived;
    }

    public void addInterest(double interest)
    {
        if (interest < 0) throw new IllegalArgumentException("Interest amount must be positive.");
        deposit(interest);
        interestReceived += interest; // count it only if deposit was successful.
    }

    @Override
    public String toString()
    {
        BigDecimal interestBD = new BigDecimal( getInterestReceived(), MathContext.DECIMAL32);//set PRECISION to 7 digits
        interestBD = interestBD.setScale(2,BigDecimal.ROUND_DOWN); //SCALE is # of digits after the period
        String interestString = " interest accrued to date is $" + interestBD.toPlainString(); // no-exponent notationgetI
        return super.toString() + interestString;
    }
}