// Marcus Kok
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.Collections;

public class RefreshingGraphPanel extends JPanel implements MouseListener
{
    // instance variables

    private JFrame graphWindow = new JFrame();
    private double[] xValues, yValues;
    private CalculatorInterface graphingCalculator;
    private String expression;
    private String[] xScaleValues; // String versions of the provided xvalues
    private int[] xClickOffsets; // where the x values are printed on the X axis
    private double smallestY;
    private double biggestY;

    private JFrame xyWindow = new JFrame();
    private JPanel xyPanel = new JPanel();
    private JTextField xTextField = new JTextField();
    private JTextField yTextField = new JTextField();

    public RefreshingGraphPanel(String expression, double[] xValues, double[] yValues, CalculatorInterface graphingCalculator){
        if(xValues.length != yValues.length) throw new IllegalArgumentException("x and y values are not the same length");
        System.out.print("X: | ");
        for(double x : xValues) System.out.print(x + " | ");
        System.out.println("");
        System.out.print("Y: | ");
        for(double y : yValues) System.out.print(y + " | ");

        this.expression = expression;
        this.xValues = xValues;
        this.yValues = yValues;
        this.graphingCalculator = graphingCalculator;
        smallestY = yValues[0];
        biggestY = yValues[yValues.length-1];
        xScaleValues = new String[xValues.length];
        for(int i = 0; i < xValues.length; i++) xScaleValues[i] = String.valueOf(xValues[i]);
        xClickOffsets = new int[xScaleValues.length];

        graphWindow.setTitle(expression);
        graphWindow.getContentPane().add(this, "Center"); // WE are the Jpanel being added!
        graphWindow.setSize(500, 500);
        graphWindow.setVisible(true);
        graphWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        xTextField.setHorizontalAlignment(SwingConstants.LEFT);
        yTextField.setHorizontalAlignment(SwingConstants.LEFT);
        xyPanel.setLayout(new GridLayout(2,1)); // rows. columns
        xyPanel.add(xTextField);
        xyPanel.add(yTextField);
        xyWindow.getContentPane().add(xyPanel,"Center");
        xyWindow.setSize(100,75);
        // setLocation() and setVisible() will be done in the mousePressed() method.
        addMouseListener(this);


    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent me) {
        int xPixels = me.getX();
        int yPixels = me.getY();
        System.out.println("Mouse pressed in " + expression + " at x = " + xPixels + " y = " + yPixels);
        int graphWindowXlocation = (int) graphWindow.getLocation().getX();
        int graphWindowYlocation = (int) graphWindow.getLocation().getY();
        System.out.println("GraphWindow is at screen location " + graphWindowXlocation + ", " + graphWindowYlocation);
        int xScreenPixelLocation = graphWindowXlocation + xPixels;
        int yScreenPixelLocation = graphWindowYlocation + yPixels;
        System.out.println("Net screen x,y pixel location of mouse click is " + xScreenPixelLocation + ", " + yScreenPixelLocation);

        // show the mini x,y display window
        xyWindow.setLocation(xScreenPixelLocation, yScreenPixelLocation);
        xyWindow.setVisible(true);
        // set the xy values to be displayed in the xy window
        double xPixelRange = xClickOffsets[xClickOffsets.length-1] - xClickOffsets[0];
        double xPixelValue = me.getX() - xClickOffsets[0];
        double xPercent = 100 * xPixelValue/xPixelRange;
        double xExpressionRange = xValues[xValues.length-1] - xValues[0];
        double xValue = xValues[0] + (xPercent * xExpressionRange)/100;
        System.out.println(" xPixelRange="               + xPixelRange
                + " xPixelValue="               + xPixelValue
                + " xPixelValueOfxPixelRange=" + xPercent
                + " xExpressionRange= "         + xExpressionRange
                + " xValue="                    + xValue);
        String xValueString = String.valueOf(xValue);
        String yValueString = graphingCalculator.calculate(expression, xValue);

        // Restrict the decimal precision of displayed x & y values to 4 decimal places
        int xDecimalOffset = xValueString.indexOf(".");
        if (xDecimalOffset >= 0) // found a decimal point
        {
            String xWholePart   = xValueString.substring(0,xDecimalOffset);
            String xDecimalPart = xValueString.substring(xDecimalOffset+1);
            if (xDecimalPart.length() > 4)
                xDecimalPart = xDecimalPart.substring(0,4);//drop after 4th digit
            xValueString = xWholePart + "." + xDecimalPart;
        }
        int yDecimalOffset = yValueString.indexOf(".");
        if (yDecimalOffset >= 0) // found a decimal point
        {
            String yWholePart   = yValueString.substring(0,yDecimalOffset);
            String yDecimalPart = yValueString.substring(yDecimalOffset+1);
            if (yDecimalPart.length() > 4)
                yDecimalPart = yDecimalPart.substring(0,4);//drop after 4th digit
            yValueString = yWholePart + "." + yDecimalPart;
        }
        xTextField.setText("X = " + xValueString);
        yTextField.setText("Y = " + yValueString);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //System.out.println("Mouse released");
        xyWindow.setVisible(false);
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void paint(Graphics g) {
        // Draw two sample dots and connect them with a line
        // x is in-from-the-left, but y is down-from-the-top!
        //System.out.println("Drawing graph...");
        /*int windowWidth  = getSize().width; // the getSize() method
        int windowHeight = getSize().height;// is in the JPanel class.
        int upperLeftDotX   = windowWidth/4;    // adjust the locations of
        int upperLeftDotY   = windowHeight/4;   // the upper-left "dot" and
        int lowerRightDotX  = windowWidth-100;  // the lower-right "dot" depending
        int lowerRightDotY  = windowHeight-100; // on the current window height & width.
        g.drawOval(upperLeftDotX, upperLeftDotY,  4,4); // tiny 4x4 circle at upper left  (x,y)
        g.drawOval(lowerRightDotX,lowerRightDotY, 4,4); // tiny 4x4 circle at lower right (x,y)
        // Connect the dots! Draw lines between the points
        g.drawLine(upperLeftDotX, upperLeftDotY, lowerRightDotX, lowerRightDotY); // (x,y to x,y) EASY!*/

        double yValueRange;
        double yRangeToZero;
        int     windowWidth    = getSize().width; // the getSize() method
        int     windowHeight   = getSize().height;// is in the JPanel class.
        int     margin         = 50;
        int     yAxisYstart    = windowHeight - margin;
        int     yAxisYstop     = margin;
        int     yAxisXlocation = margin;
        int     yAxisLength    = windowHeight - (2*margin);
        int     yAxisZeroPoint = yAxisYstart;
        int     yClickOffset   = yAxisYstart;

        if((smallestY <= 0) && (biggestY > 0)){
            double percentToYzeroPoint = Math.abs(smallestY)/(Math.abs(smallestY) + Math.abs(biggestY));
            int pixelsToSubtract = (int) (percentToYzeroPoint*yAxisLength);
            yAxisZeroPoint -= pixelsToSubtract;
            System.out.println("Window height is " + windowHeight + " yAxisZeroPoint is at y = " + yAxisZeroPoint);
        }

        int xAxisZeroPoint = 0;
        int xAxisXstart = margin;
        int xAxisXstop = windowWidth - margin;
        int xAxisYlocation = yAxisZeroPoint;
        int xAxisLength = windowWidth - (2 * margin);
        int xClickBump = xAxisLength/(xValues.length-1);
        int xClickOffset = xAxisXstart;

        g.setColor(Color.BLACK);
        g.setFont(new Font("Times Roman", Font.BOLD, 20)); // medium
        g.drawLine(xAxisXstart, xAxisYlocation, xAxisXstop, xAxisYlocation);
        g.drawString("X", xAxisXstop+10, xAxisYlocation); // String x, y

        for(int i = 0; i < xScaleValues.length; i++){
            xClickOffsets[i] = xClickOffset; // save x offsets for later graphing of x, y values!
            g.drawString("|", xClickOffset, xAxisYlocation+5); // down a little
            g.drawString(xScaleValues[i], xClickOffset-5, xAxisYlocation+25); // down a little
            xClickOffset += xClickBump;
        }

        xClickOffset = xAxisXstart; // reset to default left margin
        if((xValues[0]) < 0 && (xValues[10] >= 0)){
            // find the x offset where the xValue changes from - to +
            int i;
            for(i = 0; i < xValues.length-1; i++){
                if((xValues[i] <= 0) && (xValues[i+1] > 0)) break;
                xClickOffset += xClickBump;
            } // end of for (i = 0; i < xValues.length-1; i++)
            xAxisZeroPoint = xClickOffset;
             //System.out.println("Window width is " + windowWidth + " xAxisZeroPoint is at x = " + xAxisZeroPoint);
        } // end of if (xValues[0]) < 0 && (xValues[10] >= 0)

        double smallestY = Double.MAX_VALUE;
        double biggestY = -Double.MAX_VALUE;
        for(double y: yValues){
            if(y < smallestY) smallestY = y;
            if(y > biggestY) biggestY = y;
        }

        String yTopScale = "Y = " + String.valueOf(biggestY);
        String yBottomScale = "Y = " + String.valueOf(smallestY);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Times Roman", Font.BOLD, 20));
        if (xAxisZeroPoint == 0) // draw Y axis at left margin
        {
            g.drawLine(yAxisXlocation, yAxisYstart, // from lower (x,y)
                    yAxisXlocation, yAxisYstop); // to upper (x,y)
            g.drawString(yTopScale,    xAxisXstart+10, yAxisYstop + 10); // String
            g.drawString(yBottomScale, xAxisXstart+10, windowHeight);    // at (x,y)
        }
        else // x axis has a 0 point, so draw Y axis at the x=0 point
        {
            g.drawLine(xAxisZeroPoint, yAxisYstart,
                    xAxisZeroPoint, yAxisYstop);
            g.drawString(yTopScale,    xAxisZeroPoint, yAxisYstop+10);
            g.drawString(yBottomScale, xAxisZeroPoint, yAxisYstart);
        }

        // Convert yValues to y print coordinates (pixels)
        int[] yPixelOffsets = new int[yValues.length];
        double valueRange = biggestY - smallestY;
        int pixelRange = yAxisYstart - yAxisYstop;

        for(int i = 0; i < yValues.length; i++){
            double valuePercent = (yValues[i]-smallestY)/valueRange;
            int pixelOffset = (int)(pixelRange * valuePercent);
            yPixelOffsets[i] = yAxisYstart-pixelOffset; // y is down-from-the-top, and Ystart is bottom (max)
            /*System.out.println("xValue is "       + xScaleValues[i]
                    + " xPixelOffset is " + xClickOffsets[i]);
            System.out.println("yValue is "       + yValues[i]
                    + " yPixelOffset is " + yPixelOffsets[i]);*/
        }

        g.setColor(Color.red);
        for(int i = 0; i < yPixelOffsets.length; i++){
            g.drawOval(xClickOffsets[i]-2, yPixelOffsets[i]-2, 4, 4); // tiny 4x4 circle
            // connect the dots! draw lines between the points
            if(i > 0) g.drawLine(xClickOffsets[i-1], yPixelOffsets[i-1], xClickOffsets[i], yPixelOffsets[i]);
        }

    } // end of paint
}
