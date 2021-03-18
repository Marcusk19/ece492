// Marcus Kok
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
// 3-4-21 This program enhances SimpleExpressionCalculator by allowing
// expressions with multiple operators (but not yet x or parentheses).
// AND during an initial scan to find missing operands and validate
// operands are numeric, we will also spot negative unary operators and
// replace them with 'n' (making them an operand attribute rather than
// an operator).

public class GraphingCalculator implements CalculatorInterface
{

    public static void main(String[] args)
    {
        System.out.println("Marcus Kok");
        System.out.println("Enter an expression (e.g. 1 + 2 + 3) or END to terminate.");
        System.out.println("Operators are + - * / ^ r  e.g. the expression 25r2 has value 5 (And ' 25 r 2 ' is OK.)");
        System.out.println("A 'unary' operator ('-' not followed by a space) is also allowed before an operand. e.g. -2 + -3 has value -5 (-2+-3 is OK)");
        System.out.println("An operand can be a number or the symbol x, pi or e" +
                            " If the expression contains x, follow it with the keyword 'whenXis' followed by an x value.");
        System.out.println("Graph the equation by adding keyword 'by' after 'whenXis' to specify the increments of x you want to graph by");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        CalculatorInterface sec = new GraphingCalculator();
        String expression      = null;
        String expressionValue = null;
        while(true)
        {
            try {expression = br.readLine().trim();}
            catch(IOException ioe)	{} // ignore keyboard errors
            if (expression.length() == 0) continue; // skip blank entry
            if (expression.equalsIgnoreCase("END")) break; // operator termination
            try {
                expression = expression.toLowerCase();
                if(expression.contains("whenxis")){
                    double in = 0; // declaring increment variable in case we need to graph
                    boolean graph = false;
                    if(expression.contains("by")){ // check for graph
                        graph = true;
                        System.out.println("In graph mode");
                        in = Double.parseDouble(expression.substring(expression.indexOf('y') + 1));
                        System.out.println("Increment = " + in);
                        expression = expression.substring(0, expression.indexOf("by")); // cut off the by portion of string
                    }
                    String xString = expression.substring(expression.indexOf("whenxis"));
                    double x;
                    // check for a value of x input as "pi" or "e"
                    if(xString.substring(7).trim().equalsIgnoreCase("pi")) x = Math.PI;
                    else if(xString.substring(7).trim().equalsIgnoreCase("e")) x = Math.E;
                    else x = Double.parseDouble(xString.substring(7));
                    expression = expression.substring(0, expression.indexOf("whenxis"));
                    // System.out.println("value of x is: " + x);
                    if(graph){
                        System.out.println("graphing");
                        expressionValue = sec.calculate(expression, x, in);
                        System.out.println(expressionValue);
                    }
                    else {
                        expressionValue = sec.calculate(expression, x).trim();
                        System.out.println(expression.trim() + " = " + expressionValue + " whenXis " + x);
                    }
                }
                else {
                    expressionValue = sec.calculate(expression);
                    System.out.println(expression + " = " + expressionValue);
                }
            }
            catch(Exception e)
            {
                System.out.println(e); // show error message
            }
        } // bottom of main loop
    } // return of main thread terminates program.

    // INSTANCE VARIABLES (in each program object) **************************
    public boolean debugMode = false;


    // METHODS IN THE PROGRAM OBJECT *****************************************
    public String calculate(String expression)
    {
        if(debugMode) System.out.println("Entering calculate");
        if(expression.contains("=")) throw new IllegalArgumentException("Expression should not contain an = sign.");
        expression = expression.trim().toLowerCase();
        if(expression.contains("(") || expression.contains(")")) expression = replaceInnerExpression(expression); // eliminate parenthesized expressions
        return evaluateInnerExpression(expression); // evaluate the resulting complex expression
    }

    public String calculate(String expression, double x) {
        if(debugMode) System.out.println("Entering calculate with x value");
        String xValueAsString = String.valueOf(x);
        if(xValueAsString.startsWith("+")) throw new IllegalArgumentException("Positive unary not allowed in x value");
        if(x < 0) xValueAsString = 'n' + xValueAsString.substring(1);
        expression = expression.replace("x", xValueAsString);
        return calculate(expression);
    }

    public String calculate(String expression, double x, double increment) {
        double[] xValues = new double[11]; // by default graph 11 points on the graph
        double[] yValues = new double[xValues.length];

        System.out.println("X | Y");
        for(int i = 0; i < xValues.length; i++) {
            xValues[i] = x;
            yValues[i] = Double.parseDouble(calculate(expression, x));
            System.out.println(xValues[i] + " | " + yValues[i]);
            x += increment;
        }
        // load the RefreshingGraphPanel passing arrays as constructor parameters
        RefreshingGraphPanel rgp = new RefreshingGraphPanel(expression, xValues, yValues, this);
        return calculate(expression, yValues[0]);
    }

    public void setDebugModeOn() {
        debugMode = true;
    }

    public void setDebugModeOff() {
        debugMode = false;
    }

    public String evaluateInnerExpression(String expression){
        if(debugMode) System.out.println("Evaluating inner expression");
        expression = expression.trim().toLowerCase();
        String[] operators = {"r", "^", "*", "/", "+", "-"};

        String currentOperator;

        // do operand substitution for pi and e
        expression = expression.replace("pi", String.valueOf(Math.PI));
        expression = expression.replace("e", String.valueOf(Math.E));
        expression = scanInnerExpression(expression);
        for(int i = 0; i < operators.length; i+=2){
            String currentOperator1 = operators[i];
            String currentOperator2 = operators[i+1];
            if(debugMode) System.out.println("Currently on pass for " + currentOperator1 + ", " + currentOperator2);
            while(expression.contains(currentOperator1) || expression.contains(currentOperator2)){
                // find what operator we will take in expression
                if(!expression.contains(currentOperator1)) currentOperator = currentOperator2; // if one is not in expression it has to be the other and vice versa
                else if(!expression.contains(currentOperator2)) currentOperator = currentOperator1;
                else {
                    // if both are in the expression, look at indices and determine which happens first (left to right)
                    int index1 = expression.indexOf(currentOperator1);
                    int index2 = expression.indexOf(currentOperator2);
                    if (index1 < index2) currentOperator = currentOperator1;
                    else currentOperator = currentOperator2;
                }
                int index = expression.indexOf(currentOperator);

                if(index == expression.length() - 1) throw new IllegalArgumentException("The expression should not end with an operator");
                if(debugMode) System.out.println("Found index " + index + " of Operator " + currentOperator);
                int prevOp = findPreviousOperatorOffset(expression, index);
                int followingOp = findFollowingOperatorOffset(expression, index);
                String leftOperand = expression.substring(prevOp+1, index);
                String rightOperand = expression.substring(index+1, followingOp);

                if(leftOperand.startsWith("+") || rightOperand.startsWith("+")) throw new IllegalArgumentException("Use of a positive unary operator is not allowed");
                if(leftOperand.startsWith("n")) leftOperand = leftOperand.replace('n', '-');
                if(rightOperand.startsWith("n"))  rightOperand = rightOperand.replace('n', '-');

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
                if (debugMode) System.out.println("Left operand is " + leftNumber + " Right operand is " + rightNumber);

                if ((currentOperator.equals("r")) && (rightNumber > 5))
                    throw new IllegalArgumentException("Root values greater than 5 are not supported.");

                double result = 0;
                switch (currentOperator)
                {
                    case "+" : result = leftNumber + rightNumber; 	         break;
                    case "-" : result = leftNumber - rightNumber; 	         break;
                    case "*" : result = leftNumber * rightNumber; 	         break;
                    case "/" : result = leftNumber / rightNumber;    	     break;
                    case "^" : result = Math.pow(leftNumber,rightNumber);   break;
                    case "r" : result = Math.pow(leftNumber, 1/rightNumber);break;
                }
                if(debugMode) System.out.println(leftNumber + currentOperator + rightNumber + " = " + result);
                expression = expression.replace(expression.substring(prevOp+1, followingOp), String.valueOf(result));
                if(debugMode) System.out.println("Reduced expression to " + expression);
                expression = scanInnerExpression(expression);
            }// end of while loop looking for operator

        } // end of for loop
        if(expression.startsWith("n")) expression = expression.replace("n", "-"); // check for leading negative
        return expression;
    }

    private String replaceInnerExpression(String expression){
        if(debugMode) System.out.println("Replacing inner expressions");
        while(expression.contains("(")){
            // keep looping until all parentheses are gone
            int leftParenOffset = expression.indexOf("("); // we know there is one of these
            int rightParenOffset = expression.indexOf(")"); // but we're not sure about these
            if(rightParenOffset < 0) throw new IllegalArgumentException("Unmatched parentheses: missing ')'.");
            if(rightParenOffset < leftParenOffset) throw new IllegalArgumentException("Invalid parentheses order: ')' before '('.");

            int i = leftParenOffset; // scan to right from where the first left parenthesis
            for(; i< rightParenOffset; i++){
                if(expression.charAt(i) == '(') leftParenOffset = i; // the beginning of an "inner"
            }
            String innerExpression = expression.substring(leftParenOffset+1, rightParenOffset);
            if(innerExpression.length() == 0) throw new IllegalArgumentException("inner expression () is empty");
            if(debugMode) System.out.println("Inner expression is: " + innerExpression);

            String innerExpressionValueString = evaluateInnerExpression(innerExpression);
            if(innerExpressionValueString.startsWith("-")) innerExpressionValueString = "n" + innerExpressionValueString.substring(1);
            if(debugMode) System.out.println("innerExpression value is " + innerExpressionValueString);

            expression = expression.substring(0, leftParenOffset)
                            + innerExpressionValueString
                            + expression.substring(rightParenOffset+1); // drop left and right parentheses characters during the replace process
            if(debugMode) System.out.println("After replace of an inner expression by it's value, expression is '" + expression + "'");
            expression = expression.replace("nn","");
            expression = expression.replace("-n", "+");
            expression = expression.replace("+n", "-");
            if(debugMode) System.out.println("After removal of double negative cause by replace, expression is " + expression);
        } // bottom of while("(")
        if(expression.contains(")")) throw new IllegalArgumentException("Unbalanced parentheses: extra')'");
        return expression;
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

    private int findPreviousOperatorOffset(String expression, int currentOperatorOffset)
    {
        // look for the preceding operator (as the delimiter of the preceding operand)
        int i;
        for (i = currentOperatorOffset -1; i > -1; i--)
        {
            //if (debugMode) System.out.println("char at " + i + " is " + expression.charAt(i));
            if ((expression.charAt(i) == '+')
                    || (expression.charAt(i) == '-')
                    || (expression.charAt(i) == '*')
                    || (expression.charAt(i) == '/')
                    || (expression.charAt(i) == '^')
                    || (expression.charAt(i) == 'r'))
            {
                break;
            }
        }// end of for
        if(i < -1) i = -1;
        if(debugMode) System.out.println("previous operator index returned: " + i);
        return i;

    }// end of method
    //-------------------------------------------------------------------------------------
    private int findFollowingOperatorOffset(String expression, int currentOperatorOffset)
    {
        // look for the following operator (as the delimiter of the preceding operand)
        int i;
        for (i = currentOperatorOffset +1; i < expression.length(); i++)
        {
            // if (debug) System.out.println("char at " + i + " is " + expression.charAt(i));
            if ((expression.charAt(i) == '+')
                    || (expression.charAt(i) == '-')
                    || (expression.charAt(i) == '*')
                    || (expression.charAt(i) == '/')
                    || (expression.charAt(i) == '^')
                    || (expression.charAt(i) == 'r'))
            {
                break;
            }
        }// end of for
        if(debugMode) System.out.println("following operator index returned: " + i);
        return i;
    }// end of method
    //-------------------------------------------------------------------------------------

}// end of class