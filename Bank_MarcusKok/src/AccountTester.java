public class AccountTester {
    public static void main(String[] args) {
        Account a = new Account();
        a.setCustomerName("Smith, Bubba");
        System.out.println(a.getClass().getName() + " " + a.getAccountNumber() + " " + a.getCustomerName());

        // now create second account, but this time use a constructor to set customer name field and toString()
        // to get it
        Account a2 = new Account("Boop, Betty");
        System.out.println(a2.toString());
    }
}
