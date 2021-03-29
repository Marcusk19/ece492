// Marcus Kok
import java.math.BigDecimal;
import java.math.MathContext;

public  abstract class CashAccount extends Account implements CashAccountInterface{
    // instance variables
    private double balance;
    public CashAccount(){
        super();
    }

    public CashAccount(String customerName) {
        super(customerName);
    }

    public double getBalance() {
        return balance;
    }

    public synchronized void deposit(double amount) throws IllegalArgumentException{
        if(amount < 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        balance += amount;
    }

    public synchronized void withdraw(double amount) throws IllegalArgumentException, OverdraftException{
        if(amount < 0) throw new IllegalArgumentException("Withdraw amount must be positive.");
        if(amount > balance) throw new OverdraftException("Withdraw of $" + amount + " denied in account #"
                                                                + getAccountNumber() + " insufficient funds.");
        balance -= amount;
    }

    @Override
    public String toString() {
        BigDecimal balanceBD = new BigDecimal(balance, MathContext.DECIMAL32);//set PRECISION to 7 digits
        balanceBD = balanceBD.setScale(2,BigDecimal.ROUND_DOWN); //SCALE is # of digits after the period
        return "#" + getAccountNumber() + " for " + getCustomerName() + " $" + balanceBD.toPlainString(); // NO-exponent notation
    }
}
