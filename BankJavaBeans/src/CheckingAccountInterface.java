public interface CheckingAccountInterface extends CashAccountInterface
{
    double getFeesPaid();
    void   payFee(double amount) throws OverdraftException;
    String toString();
}