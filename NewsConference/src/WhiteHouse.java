import java.io.IOException;

public class WhiteHouse {
    String presidentsStatement;

    public synchronized void makeAStatement(String presidentsStatement){
        this.presidentsStatement = presidentsStatement;
        notifyAll();
    }

    public synchronized String attendTheNewsConference(String topicOfInterest){
        System.out.println("Reporter with topic: " + topicOfInterest + " has entered the news conference.");
        while(true){
            try{wait();}
            catch(InterruptedException ie){}
            if (presidentsStatement.contains(topicOfInterest)) return presidentsStatement; // leave the news conference.
            if (presidentsStatement.contains("God bless America")) return presidentsStatement;
        }
    }
}
