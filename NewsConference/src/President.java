import java.io.BufferedReader;
import java.io.InputStreamReader;

public class President implements Runnable{
    WhiteHouse whiteHouse;

    public President(WhiteHouse whiteHouse) {
        this.whiteHouse = whiteHouse;
        new Thread(this).start();
    }

    public void run() {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader keyboard = new BufferedReader(isr);
        System.out.println("Enter a statement. The phrase 'God bless America' ends the news conference.");
        try {
            while (true) {

                String statement = keyboard.readLine();
                whiteHouse.makeAStatement(statement);
                if (statement.equals("God bless America")) {
                    break;
                }

            }
            System.out.println("The News Conference is over.");
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
