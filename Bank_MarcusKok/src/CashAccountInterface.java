public interface CashAccountInterface extends AccountInterface
{
    double getBalance();
    void   deposit(double amount);
    void   withdraw(double amount) throws OverdraftException;
    String toString();
}
