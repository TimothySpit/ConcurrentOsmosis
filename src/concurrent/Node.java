package concurrent;

/**
 * Class representing a node in our simulation. Each node has a specific value,
 * which is passed with specific rates to its neighbours.
 * 
 * @author Timo Speith (& Magnus Halbe)
 * @version 0.1
 */
public class Node
{
    //the value currently hold by this node
    double value;
    
    //y-coordinate of this Node in its specific column
    private int y;
    
    //This nodes pedecessing neighbour
    private Node previous;
    //This nodes successing neighbour
    private Node next;
    
    //Transmission rates of this node to its neighbours (0=west, 1=north, 2=east, 3=south).
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
        this.y = y;
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
        
    }
    
    /**
     * Passes a specific amount of this Nodes value to the Node lying north of it.
     * If there is currently no Node and the value to be passed is greater zero,
     * a new Node is created and returned. Otherwise nothing happens.
     * 
     * @param value the value of this Node (to get via @see{getValue}). 
     * @return the Node created by this Method, if any was created, null otherwise
     */
    public Node updatePrevious(double value)
    {
        double rate = rates[1];
        if(rate > 0)
        {
            
        }
        return this;
    }
    
    /**
     * Passes a specific amount of this Nodes value to the Node lying south of it.
     * If there is currently no Node and the value to be passed is greater zero,
     * a new Node is created and returned. Otherwise nothing happens.
     * 
     * @param value the value of this Node (to get via @see{getValue}). 
     * @return the Node created by this Method, if any was created, null otherwise
     */
    public Node updateNext(double value)
    {
        double rate = rates[3];
        return this;
    }
}