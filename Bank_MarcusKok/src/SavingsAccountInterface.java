public interface SavingsAccountInterface extends CashAccountInterface
{
    double getInterestReceived();
    void   addInterest(double amount);
    String toString();
}
