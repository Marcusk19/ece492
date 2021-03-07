import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
// 3-4-21 This program enhances SimpleExpressionCalculator by allowing
// expressions with multiple operators (but not yet x or parentheses).
// AND during an initial scan to find missing operands and validate
// operands are numeric, we will also spot negative unary operators and
// replace them with 'n' (making them an operand attribute rather than
// an operator).

public class StartingComplexExpressionCalculator
{

    public static void main(String[] args)
    {
        System.out.println("Enter an expression (e.g. 1 + 2 + 3) or END to terminate.");
        System.out.println("Operators are + - * / ^ r  e.g. the expression 25r2 has value 5 (And ' 25 r 2 ' is OK.)");
        System.out.println("A 'unary' operator ('-' not followed by a space) is also allowed before an operand. e.g. -2 + -3 has value -5 (-2+-3 is OK)");
        System.out.println("An operand can be a number or the symbol x, pi or e");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StartingComplexExpressionCalculator sec = new StartingComplexExpressionCalculator();
        String expression      = null;
        String expressionValue = null;
        while(true)
        {
            try {expression = br.readLine().trim();}
            catch(IOException ioe)	{} // ignore keyboard errors
            if (expression.length() == 0) continue; // skip blank entry
            if (expression.equalsIgnoreCase("END")) break; // operator termination
            try {
                expressionValue = sec.calculate(expression);
                System.out.println(expression + " = " + expressionValue);
            }
            catch(Exception e)
            {
                System.out.println(e); // show error message
            }
        } // bottom of main loop
    } // return of main thread terminates program.

    // INSTANCE VARIABLES (in each program object) **************************
    public boolean debugMode = true;


    // METHODS IN THE PROGRAM OBJECT *****************************************
    public String calculate(String expression)
    {
        expression = expression.trim().toLowerCase();
        expression = scanInnerExpression(expression);

        // do operand substitution for pi and e
        expression = expression.replace("pi", String.valueOf(Math.PI));
        expression = expression.replace("e", String.valueOf(Math.E));

        char operator = ' ';
        int  i;
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
            throw new IllegalArgumentException("An operator was not found between the operands in the expression "+ expression);
        if (debugMode) System.out.println("In expression " + expression + " the operator " + operator + " was found at offset " + i);
        String leftOperand  = expression.substring(0,i).trim(); // beginning to operator
        String rightOperand = expression.substring(i+1).trim(); // everything after the operator
        // Test operands to ensure they are numeric.
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
        if (debugMode) System.out.println("Left operand is " + leftOperand + " Right operand is " + rightOperand);

        // watch for restriction on root value
        if ((operator == 'r') && (rightNumber > 5))
            throw new IllegalArgumentException("Root values greater than 5 are not suported.");

        double result = 0;
        switch (operator)
        {
            case '+' : result = leftNumber + rightNumber; 	         break;
            case '-' : result = leftNumber - rightNumber; 	         break;
            case '*' : result = leftNumber * rightNumber; 	         break;
            case '/' : result = leftNumber / rightNumber;    	     break;
            case '^' : result = Math.pow(leftNumber,rightNumber);   break;
            case 'r' : result = Math.pow(leftNumber, 1/rightNumber);break;
        }
        return String.valueOf(result);
    }

    //*************************************************************************
    private String scanInnerExpression(String expression)
    {
        // This method finds missing operands and validates operands are numeric.
        // Negative unary operators are spotted and replaced with 'n' (making
        // them an operand attribute rather than an operator!)
        // find the next operator
        if(debugMode)System.out.println("Entering scanInnerExpression() with expression: " + expression);

        int lastOperatorAt   = -1;
        int currentOperatorAt= 0;
        char currentOperator = ' ';
        char lastOperator    = '0';
        String operand       = "";
        int i=0;
        for (; i<expression.length(); i++)
        {
            //look for next operator
            if ((expression.charAt(i) == '+')
                    || (expression.charAt(i) == '-')
                    || (expression.charAt(i) == '*')
                    || (expression.charAt(i) == '/')
                    || (expression.charAt(i) == '^')
                    || (expression.charAt(i) == 'r'))
            {
                currentOperator = expression.charAt(i);
                currentOperatorAt = i;
                if ((currentOperatorAt == 0) && (currentOperator == '-'))
                {
                    expression = "n" + expression.substring(1);
                    continue;
                }
//        if (debugMode) System.out.println("Found operator " + currentOperator + " at offset " + currentOperatorAt);
                operand = expression.substring(lastOperatorAt+1, currentOperatorAt).trim();
                if (operand.length() == 0) // *** MISSING OPERAND ***
                {
                    // Before throwing exception for missing operand, check to
                    // see if right operator might be a negative unary.
                    if (expression.charAt(i+1) == ' ') // this is NOT a unary if followed by a blank.
                        throw new IllegalArgumentException("Missing operand between operators " + lastOperator + " and " + currentOperator);
                    if (expression.charAt(i) == '-')   // this ***IS*** a negative unary!
                    {
                        if(debugMode)System.out.println("Replacing - with n at offset " + i);
                        expression = expression.substring(0,i) + "n" + expression.substring(i+1);
                        continue;
                    }
                }
                try {
                    String unalteredOperand = operand;
                    if (operand.startsWith("n")) unalteredOperand = "-" + operand.substring(1);
                    double operandValue = Double.parseDouble(unalteredOperand);
                }
                catch(NumberFormatException nfe) {throw new IllegalArgumentException("Operand " + operand + " is not numeric.");}
                if (debugMode) System.out.println("Found operand " + operand + " at offset " + (lastOperatorAt+1) + " followed by operator " + currentOperator + " at offset " + currentOperatorAt);
                lastOperatorAt = currentOperatorAt;
                lastOperator   = currentOperator;
            }// end of if
            // find the next operator
        }// end of for
        if (i == expression.length())
        {
            operand = expression.substring(lastOperatorAt+1).trim();
            try {
                String unalteredOperand = operand;
                if (operand.startsWith("n")) unalteredOperand = "-" + operand.substring(1);
                double operandValue = Double.parseDouble(unalteredOperand);
            }
            catch(NumberFormatException nfe) {throw new IllegalArgumentException("Operand " + operand + " is not numeric.");}
            if (debugMode) System.out.println("last operand is " + operand);
        }
        // At this point operands and operators look good and unary operators
        // have been replaced by 'n'. We can now REMOVE BLANKS to make subsequent
        // parsing easier.
        expression = expression.replace(" ", "");
        if(debugMode)System.out.println("Exiting scanInnerExpression() returning modified expression: " + expression);
        return expression;
    }// end of scanInnerExpression
}// end of class