public class invitation {
    private String hostName;
    private String address;

    public invitation(String n, String a){
        hostName = n;
        address = a;
    }

    public invitation(String address){
        address = address;
        hostName = "host";
        System.out.println("new invitation made: " + address + " " + hostName);
    }

    public String getHostName(){
        return hostName;
    }

    public void setAddress(String a){
        address = a;
        System.out.println("new address is: " + address);
    }

    public String sendInvitation(String guest){
        return "Dear " + guest + " please attend my event at " + address + ". See " +
                "you then, " + hostName;
    }

    public static void main(String[] args) {
        invitation i = new invitation("marcus", "marcus house");
        System.out.println(i.getHostName()); // print hostname
        String newAddress = "New House";
        i.setAddress(newAddress);
        String newInvite = i.sendInvitation("Matthew");
        System.out.println(newInvite);

        invitation j = new invitation("5209 huntscroft");

    }
}

