public class AccountTester {
    public static void main(String[] args) throws Exception {
        CheckingAccount checkingAccount = new CheckingAccount();
        checkingAccount.setCustomerName("Smith, Bubba");
        System.out.println(checkingAccount.getClass().getName() + " " + checkingAccount.getAccountNumber() + " " + checkingAccount.getCustomerName() + " balance " + checkingAccount.getBalance());
        checkingAccount.deposit(500);
        try{ checkingAccount.withdraw(1000);}
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        checkingAccount.payFee(10);
        try{checkingAccount.deposit(10000);}
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        System.out.println(checkingAccount.toString());
        // now create second account, but this time use a constructor to set customer name field and toString()
        // to get it
        SavingsAccount savingsAccount = new SavingsAccount("Boop, Betty");
        savingsAccount.deposit(500);
        try{savingsAccount.withdraw(200);}
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println(savingsAccount.toString());
        savingsAccount.addInterest(100);
        System.out.println(savingsAccount.toString());
    }
}
