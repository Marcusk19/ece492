public interface CalculatorInterface
{
    public String calculate(String expression);           // no x
    public String calculate(String expression, double x); // x but no graph
    public String calculate(String expression, double x, double increment);
    //Graph will be for 11 values of x created by bumping x by increment.
    public void   setDebugModeOn();
    public void   setDebugModeOff();
}
