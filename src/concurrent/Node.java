package concurrent;

/**
 * Class representing a node in our simulation. Each node has a specific value,
 * which is passed with specific rates to its neighbours.
 * 
 * @author Timo Speith (& Magnus Halbe)
 * @version 0.5
 */
public class Node
{
    //the value currently hold by this node
    double value;
    //the change which this node undergoes during one turn
    double change;
    
    //y-coordinate of this Node in its specific column
    private int y;
    
    //This nodes pedecessing neighbour
    private Node previous;
    //This nodes successing neighbour
    private Node next;
    
    //Transmission rates of this node to its neighbours according to the enum
    private final double[] rates = new double[4];
    
    
    /**
     * Creates a new instance of a Node. 
     * 
     * @param value the value this node holds
     * @param y the y-coordinate of this node
     */
    public Node(double value, int y)
    {
        this.value = value;
        this.change = 0.0;
        
        this.y = y;
        
        this.previous = null;
        this.next = null;
    }
    
    /**
     * Returns the value hold by this node.
     * @return the current value hold by this Node
     */
    public double getValue()
    {
        return value;
    }
    
    /**
     * Returns the y-coordinate of this node.
     * @return the y-coordinate of this Node
     */
    public int getY()
    {
        return y;
    }
    
    /**
     * Registers changes during a turn. All changes can be applied via @see{flush}
     * at the and of one turn.
     * 
     * @param value the value to be added to the changes
     */
    public void register(double value)
    {
        change += value;
    }
    
    /**
     * Sets the transition rates for each direction.
     * 
     * @param direction the neighbour to which this rate is the transition
     * @param rate the transition rate for this direction
     */
    public void setRate(Neighbour direction, double rate)
    {
        rates[direction.ordinal()] = rate;
    }
    
    /**
     * Passes a specific amount of this Nodes value to the Node lying north of it.
     * If there is currently no Node and the value to be passed is greater zero,
     * a new Node is created and returned. Otherwise nothing happens.
     * 
     * @return the Node created by this Method, if any was created, null otherwise
     */
    public Node updatePrevious()
    {
        double rate = rates[Neighbour.Top.ordinal()];
        double pass = rate * value;
        if(pass > 0)
        {
            register(-pass);
            if(previous == null)
            {
                previous = new Node(pass, y-1);
                return previous;
            }
            previous.register(pass);
        }
        return null;
    }
    
    /**
     * Passes a specific amount of this Nodes value to the Node lying south of it.
     * If there is currently no Node and the value to be passed is greater zero,
     * a new Node is created and returned. Otherwise nothing happens.
     * 
     * @return the Node created by this Method, if any was created, null otherwise
     */
    public Node updateNext()
    {
        double rate = rates[Neighbour.Bottom.ordinal()];
        double pass = rate * value;
        if(pass > 0)
        {
            register(-pass);
            if(next == null)
            {
                next = new Node(pass, y+1);
                return next;
            }
            next.register(pass);
        }
        return null;
    }
    
    /**
     * Returns a specific amount of this Nodes value that would be passed to the Node
     * lying east of it. Also registers this amount to be removed from this node.
     * 
     * @return the double that contains the amount that would be output
     */
    public double emitLeft()
    {
        double rate = rates[Neighbour.Left.ordinal()];
        double pass = rate * value;
        register(-pass);
        return pass;
    }
    
    /**
     * Returns a specific amount of this Nodes value that would be passed to the Node
     * lying west of it. Also registers this amount to be removed from this node.
     * 
     * @return the double that contains the amount that would be output
     */
    public double emitRight()
    {
        double rate = rates[Neighbour.Right.ordinal()];
        double pass = rate * value;
        register(-pass);
        return pass;
    }
    
    /**
     * Method to be invoked every turn. Calculates the changes made during the turn
     * to this nodes value. This method also detects convergence.
     * 
     * @return true if the changes made during this turn are less than epsilon, false otherwise.
     */
    public boolean flush()
    {
        boolean result = change <= Column.epsilon;
        value += change;
        change = 0.0;
        return result;
    }
    
    @Override
    public String toString()
    {
    	return "Y-Coordinate: " + y + "; Value: "+ value;
    }
}