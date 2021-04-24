public class Reporter implements Runnable{
    String topicOfInterest;
    WhiteHouse whiteHouse;
    public Reporter(WhiteHouse whiteHouse, String topicOfInterest) {
        this.topicOfInterest = topicOfInterest;
        this.whiteHouse = whiteHouse;
        new Thread(this).start();
    }

    public void run() {
        String presidentsStatement = whiteHouse.attendTheNewsConference(topicOfInterest);
        if(presidentsStatement.equals("God bless America")){
            System.out.println("A Reporter has returned from the news conference. Reporter's topic-of-interest was: "
                    + topicOfInterest + ". President did not address the topic.");
        }
        else {
            System.out.println("A Reporter has returned from the news conference. Reporter's topic-of-interest was: "
                    + topicOfInterest + ". President's statement was: " + presidentsStatement);
        }
    }
}
