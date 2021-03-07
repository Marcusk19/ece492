import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

public class SimpleExpressionCalculator {
    boolean debugMode = true;
    public static void main(String[] args) {
        System.out.println("Enter a simple expression (a SINGLE operator with a numeric operand on each side) or END to terminate.\n" +
                "Operators are + - * / ^ r  e.g. the expression 25r2 has value 5 (And \" 25 r 2 \" is OK.)\n" +
                "A 'unary' operator ('-' not followed by a space) is also allowed before an operand. e.g. -2 + -3 has value -5 (-2+-3 is OK)\n" +
                "An operand can be a number or the symbol pi or e ");
        SimpleExpressionCalculator sec = new SimpleExpressionCalculator();

        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader keyboard = new BufferedReader(isr);

        while(true){
            try {
                String expression = keyboard.readLine();
                if(expression.toLowerCase().equals("end")) break;
                if(expression.equals("")) continue;
                String result = sec.calculate(expression);
                System.out.println(expression + " = " + result);
            }
            catch(Exception e){
                System.out.println(e);
            }

        }

    }

    public String calculate(String expression){
        char operator = ' ';
        int  i;
        expression = expression.toLowerCase();
        // do operand substitution for pi and e
        expression = expression.replace("pi", String.valueOf(Math.PI));
        expression = expression.replace("e", String.valueOf(Math.E));

        for (i = 1; i < expression.length(); i++)
            if((expression.charAt(i) == '+')
                    ||(expression.charAt(i) == '-')
                    ||(expression.charAt(i) == '*')
                    ||(expression.charAt(i) == '/')
                    ||(expression.charAt(i) == '^')
                    ||(expression.charAt(i) == 'r'))
            {
                operator = expression.charAt(i);
                break;
            }
        if (i == expression.length())
            throw new IllegalArgumentException("An operator was not found between the operands in "+ expression);
        if(debugMode) System.out.println("In expression " + expression + " the operator " + operator + " was found at offset " + i);
        String leftOperand  = expression.substring(0,i).trim(); // beginning to operator
        String rightOperand = expression.substring(i+1).trim(); // everything after the operator

        if(debugMode) System.out.println("Left operand is '" + leftOperand + "' Right operand is '" + rightOperand + "'");

        double leftNumber;
        double rightNumber;
        try {
            leftNumber = Double.parseDouble(leftOperand);
        }
        catch(NumberFormatException nfe)
        {
            throw new IllegalArgumentException("Left operand " + leftOperand + " is not numeric.");
        }
        try {
            rightNumber = Double.parseDouble(rightOperand);
        }
        catch(NumberFormatException nfe)
        {
            throw new IllegalArgumentException("Right operand " + rightOperand + " is not numeric.");
        }

        // watch for restriction on root value
        if((operator == 'r') && (rightNumber > 5))
            throw new IllegalArgumentException("Root values greater than 5 are not supported");
        double result = 0;
        switch(operator){
            case '+' : result = leftNumber + rightNumber;           break;
            case '-' : result = leftNumber - rightNumber;           break;
            case '*' : result = leftNumber * rightNumber;           break;
            case '/' : result = leftNumber / rightNumber;           break;
            case '^' : result = Math.pow(leftNumber, rightNumber);  break;
            case 'r' : result = Math.pow(leftNumber, 1/rightNumber);break;
        }

        return String.valueOf(result);
    }
}
