public class StartTheNewsConference {
    public static void main(String[] args) {
        if(args.length == 0){
            System.out.println("Enter topics of interest.");
            return;
        }
        WhiteHouse whiteHouse = new WhiteHouse();
        for(String topicOfInterest : args){
            new Reporter(whiteHouse, topicOfInterest);
        }
        new President(whiteHouse);
    }
}
