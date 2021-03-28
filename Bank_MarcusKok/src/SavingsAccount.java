// Marcus Kok
public class SavingsAccount extends CashAccount{
    private double interestReceived;

    public SavingsAccount() {
    }

    public SavingsAccount(String customerName) {
        super(customerName);
    }

    public double getInterestReceived() {
        return interestReceived;
    }


    public void addInterest(double interestReceived) throws IllegalArgumentException{
        if(interestReceived < 0) throw new IllegalArgumentException("Added interest cannot be negative.");
        this.interestReceived += interestReceived;
        try{
            deposit(interestReceived);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "SavingsAccount{" +
                "interestReceived=" + interestReceived +
                "} " + super.toString();
    }
}
