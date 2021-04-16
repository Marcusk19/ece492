import java.math.BigDecimal;
import java.math.MathContext;


public abstract class CashAccount extends Account implements CashAccountInterface
{
    private double balance;

    public CashAccount() throws Exception
    {
        super();
    }

    public CashAccount(String customerName) throws Exception
    {
        super(customerName);
    }

    protected CashAccount(String customerName, int accountNumber, double balance) // added for DB
    {
        super(customerName, accountNumber);
        this.balance = balance;
    }

    public synchronized void deposit(double amount) throws IllegalArgumentException
    {
        if (amount < 0)
            throw new IllegalArgumentException("Deposit amount must be positive.");
        balance += amount; // add to the balance
    }

    public synchronized void withdraw(double amount)  throws IllegalArgumentException, OverdraftException
    {
        if (amount < 0)
            throw new IllegalArgumentException("Withdraw amount must be positive.");
        if (amount > balance)
            throw new OverdraftException("Withdraw of $" + amount
                    + " denied in account #" + getAccountNumber()
                    + " insufficient funds.");
        balance -= amount; // subtract from the balance
    }

    public double getBalance()
    {
        return balance;
    }

    public String toString()
    {
        BigDecimal balanceBD = new BigDecimal(balance, MathContext.DECIMAL32);//set PRECISION to 7 digits
        balanceBD = balanceBD.setScale(2,BigDecimal.ROUND_DOWN); //SCALE is # of digits after the period
        String balanceString = " balance is $" + balanceBD.toPlainString(); // no-exponent notation
        return super.toString() + balanceString; // tack my field values onto parent field values!
    }

}