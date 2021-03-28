// Marcus Kok
public class CheckingAccount extends CashAccount{
    private double feesPaid;
    public CheckingAccount() {
        super();
    }

    public CheckingAccount(String customerName) {
        super(customerName);
    }

    public double getFeesPaid() {
        return feesPaid;
    }

    public void chargeFee(double feesPaid){
        if(feesPaid < 0) throw new IllegalArgumentException("Charged fee cannot be negative.");
        this.feesPaid += feesPaid;

        try{
            withdraw(feesPaid);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }

    }

    @Override
    public synchronized void deposit(double amount) throws IllegalArgumentException {
        if(amount > 1000) throw new IllegalArgumentException("calling CrimeWatch.gov!");
        super.deposit(amount);
    }

    @Override
    public String toString() {
        return "CheckingAccount{" +
                "feesPaid=" + feesPaid +
                "} " + super.toString();
    }
}
