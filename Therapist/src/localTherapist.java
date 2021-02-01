import java.io.BufferedReader;
import java.io.InputStreamReader;

public class localTherapist {
    public static void main(String[] args) throws Exception{
        // TODO Auto-generated method stub
        System.out.println("Welcome to the Local Therapy System!");
        System.out.println("To begin ask me a yes or no question. \n Ex. ask 'will I feel better' instead of 'when will I feel better'");
        InputStreamReader isr = new InputStreamReader(System.in); // low-level I/O class
        BufferedReader keyboard = new BufferedReader(isr); // high-level I/O class

        String[] answers = {"Absolutely", "Certainly Not", "Forget It!", "Ask Your Mother", "I dont' think so...", "Are you kidding?",
        "Not Today", "In your dreams!", "It's OK with Me!", "Sounds Good", "Yes", "It's Only a Matter of Time", "No"};
        while(true) { // call the readLine(0 method in the buffered Reader program, which will wait
            String question = keyboard.readLine().trim(); //trim() removes any leading/trailing blanks
            if(question.length() == 0) continue; // continue returns to loop top
            if(question.equalsIgnoreCase("end")
            ||  question.equalsIgnoreCase("stop")
            ||  question.equalsIgnoreCase("exit")
            ||  question.equalsIgnoreCase("quit")){
                System.out.println("Hope you enjoyed the THERAPIST today!");
                break;
            }

            int index = (int) (Math.random() * answers.length); // generate random index to array
            System.out.println(answers[index]);

        }
    }
}

