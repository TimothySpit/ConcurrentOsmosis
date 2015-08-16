package concurrent;

import gnu.trove.list.array.TDoubleArrayList;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.Exchanger;

public class Column implements Runnable
{
    private final int x;
    private final GraphInfo ginfo;

    private boolean convergenceDetected;
    
    private final TDoubleArrayList lastValues;

    private int stepsTotal;
    private int stepsDone;

    private final Exchanger<ValueBundle> leftExchanger;
    private final Exchanger<ValueBundle> rightExchanger;
    private final LinkedList<Node> nodeList;

    public Column(int xCoord, int stepsTotal, GraphInfo ginfo, Exchanger<ValueBundle> left, Exchanger<ValueBundle> right)
    {
        x = xCoord;
        this.ginfo = ginfo;
        this.stepsTotal = stepsTotal;
        stepsDone = 0;
        leftExchanger = left;
        rightExchanger = right;
        nodeList = new LinkedList<>();
        lastValues = new TDoubleArrayList(ConcOsmosis.getHeight());
        lastValues.fill(0, ConcOsmosis.getHeight(), 0.0);
    }

    @Override
    public void run()
    {
        while (!Thread.interrupted())
        {
            performSteps();
            exchange();
            stepsDone = 0;
        }
        System.out.println("Column " + x + " Terminated.");
    }

    /**
     * Iterates through the column.
     */
    public void performSteps()
    {
        while (stepsDone < stepsTotal)
        {
            ListIterator<Node> iterator = nodeList.listIterator();
            while (iterator.hasNext())
            {
                Node currentNode = iterator.next();
                Node previous = currentNode.updatePrevious();
                Node next = currentNode.updateNext();
                
                if (!isLeftmost())  {currentNode.emitIntoLeftAccu();}
                if (!isRightmost()) {currentNode.emitIntoRightAccu();}
                
                // If a previous Node was created due to exchanges
                if (previous != null)
                {
                    Node previousPreviousCandidate = iterator.previous();
                    iterator.add(previous);
                    iterator.next();
                    if (previousPreviousCandidate.getY() == previous.getY() - 1)
                    {
                        // Here the newly inserted node's previous neighbour gets informed
                        previousPreviousCandidate.setNext(previous);
                        previous.setPrevious(previousPreviousCandidate);
                    }
                    initializeNode(previous);
                }
                
                // If a next Node was creted due to exchanges
                if (next != null)
                {
                    iterator.add(next);
                    initializeNode(next);
                    if (iterator.hasNext())
                    {
                        Node nextNextCandidate = iterator.next();
                        if (nextNextCandidate.getY() == next.getY() + 1)
                        {
                            // Here the newly inserted node's potential next neighbour gets informed
                            nextNextCandidate.setPrevious(next);
                            next.setNext(nextNextCandidate);
                        }
                        iterator.previous();
                    }
                }
            }
            
            // Writes the changes into the Nodes
            nodeList.stream().forEach((currentNode) -> {currentNode.flush();});
            
            stepsDone++;
        }
    }

    /**
     * Exchanges values with the left exchanger, then the right exchanger.
     */
    public void exchange()
    {
        TDoubleArrayList leftValues = null;
        TDoubleArrayList rightValues = null;

        // Creates acuumulator list for right neighbours if existent
        if (!isLeftmost())
        {
            leftValues = new TDoubleArrayList(ConcOsmosis.getHeight());
            leftValues.fill(0, ConcOsmosis.getHeight(), 0.0);
        }

        // Creates acuumulator list for right neighbours if existent
        if (!isRightmost())
        {
            rightValues = new TDoubleArrayList(ConcOsmosis.getHeight(), 0.0);
            rightValues.fill(0, ConcOsmosis.getHeight(), 0.0);
        }

        // Creates accumulator lists
        for(Node currentNode: nodeList)
        {
            int y = currentNode.getY();
            if (leftValues != null)  {leftValues.set(y, currentNode.flushLeftAccu());}
            if (rightValues != null) {rightValues.set(y, currentNode.flushRightAccu());}
        }
        
        ValueBundle receivedFromLeft = null;
        ValueBundle receivedFromRight = null;
        
        int convergencesUntilHere = 0;
        
        TDoubleArrayList valuesToExchange = null;
        int currentSteps = 1;
        
        // Receive values from left neighbour
        try
        {
            receivedFromLeft = leftExchanger.exchange(new ValueBundle(leftValues));
            convergencesUntilHere = receivedFromLeft.getConvergents();
            valuesToExchange = receivedFromLeft.getValues();
            currentSteps = receivedFromLeft.getCurrentSteps();
        }
        catch (InterruptedException e) {}

        boolean inflowIsOutflowLeft = false;
        if (!isLeftmost())
        {
            double newVal = receivedFromLeft.getPass().sum();
            double oldVal = leftValues.sum();
            inflowIsOutflowLeft = Math.abs(newVal-oldVal) < ConcOsmosis.getEpsilon();
        }
        // Convergence calculating
        convergenceDetected = inflowIsOutflowLeft;
        if (convergenceDetected) {convergencesUntilHere++;}
        if (currentSteps <= 4)   {valuesToExchange.addAll(lastValues);}
        
        // Receive values from right neighbour
        try
        {
            ValueBundle pass = new ValueBundle(valuesToExchange, rightValues,
                    convergencesUntilHere, currentSteps);
            receivedFromRight = rightExchanger.exchange(pass);
        }
        catch (InterruptedException e) {}

        // Termination is signaled by 0 steps
        if (currentSteps == 0) { Thread.currentThread().interrupt();} //TODO: Andere Terminierung

        // Calculate accumulators from neighbour columns in
        if (!isLeftmost())  {receiveHorizontal(receivedFromLeft.getPass());}
        if (!isRightmost()) {receiveHorizontal(receivedFromRight.getPass());}
        

        // Writes all changes
        lastValues.fill(0, ConcOsmosis.getHeight(), 0.0);
        for(Node currentNode: nodeList)
        {
        	currentNode.flush(); 
         	lastValues.set(currentNode.getY(), currentNode.getValue());
        }
        
        stepsTotal = currentSteps;
    }
        
     /**
     * Updates all nodes (and creates new ones if needed) with values from
     * another column
     *
     * @param received TDoubleArrayList the double values that were received
     */
    private void receiveHorizontal(TDoubleArrayList received)
    {
        ListIterator<Node> iterator = nodeList.listIterator();
        int previousIndex = -1;
        //Updates all existing nodes, and creates new ones, if necessary
        while (iterator.hasNext())
        {
            Node currentNode = iterator.next();
            int y = currentNode.getY();
            // Checks for new nodes between existing ones
            littleLoop(previousIndex + 1, y, received, iterator);
            previousIndex = y;
            currentNode.register(received.get(y));
        }
        // The end could have new nodes, too
        littleLoop(previousIndex + 1, ConcOsmosis.getHeight(), received, iterator);
    }
   
    /**
     * Searches a specific number of entries in a list consisting of values and
     * creates Nodes, if the value is greater 0.0. The node is inserted into
     * the original node list.
     * 
     * @param begin the index to begin with (inclusive)
     * @param end the index to end with (exclusive)
     * @param received the values to be worked on
     * @param iterator the iterator which works on the nodes
     */
    private void littleLoop(int begin, int end, TDoubleArrayList received, ListIterator iterator)
    {
        for (int index = begin; index < end; index++)
        {
            double value = received.get(index);
            if (value > 0.0)
            {
                Node newNode = new Node(value, index);
                insertNode(iterator, newNode);
            }
        }
    }

    /**
     * Gives an instance of node all four rates.
     *
     * @param node the node which gets rates
     */
    private void initializeNode(Node node)
    {
        System.out.println("neue Node" + x + " | " + node.getY()); //TODO entfernen

        int y = node.getY();

        double rate = ginfo.getRateForTarget(x, y, Neighbour.Left);
        node.setRate(Neighbour.Left, rate);

        rate = ginfo.getRateForTarget(x, y, Neighbour.Top);
        node.setRate(Neighbour.Top, rate);

        rate = ginfo.getRateForTarget(x, y, Neighbour.Right);
        node.setRate(Neighbour.Right, rate);

        rate = ginfo.getRateForTarget(x, y, Neighbour.Bottom);
        node.setRate(Neighbour.Bottom, rate);
    }

    /**
     * Inserts a node in the list. The nodes remain sorted by y-coordinates.
     * The neighbour nodes get new "previous" and "next"
     *
     * @param node the node which gets inserted
     */
    public synchronized void insertNode(Node node)
    {
        ListIterator iter = nodeList.listIterator();
        int goalY = node.getY();
        ListIterator<Node> iterator = nodeList.listIterator();
        
        // Move iterator to right place
        while (iterator.hasNext()) 
        {
            if (iterator.next().getY() >= goalY) {break;}
        }
        insertNode(iter, node);
    }

    /**
     * Inserts a node in the list. The place is determined by the given iterator.
     * It is important, that the iterator is at the right place, so that the
     * nodes remain sorted by y-coordinates.The neighbour nodes get new "previous" and "next"
     *
     * @param iter the ListIterator to be used. It has to be at the right location.
     * @param node the node which gets inserted
     */
    private synchronized void insertNode(ListIterator<Node> iterator, Node node)
    {
        iterator.add(node);
        int index = nodeList.indexOf(node);
        Node candidate;
        
        // If this node is not the first, it could have a predecessor
        if (index > 0)
        {
            candidate = nodeList.get(index - 1);
            if (candidate.getY() == node.getY() - 1)
            {
                candidate.setNext(node);
                node.setPrevious(candidate);
            }
        }
        
        // If this node is not the last it could have a successor
        if (index < nodeList.size() - 1)
        {
            candidate = nodeList.get(index + 1);
            if (candidate.getY() == node.getY() + 1)
            {
                node.setNext(candidate);
                candidate.setPrevious(node);
            }
        }
        
        // Assign exchange rates
        initializeNode(node);
    }
    
    /**
     * Finds out whether this column is the leftmost column of the System
     * 
     * @return true if this column is the leftmost, false otherwise
     */
    public  boolean isLeftmost()
    {
        return (x == 0);
    }

    /**
     * Finds out whether this column is the rightmost column of the System
     * 
     * @return true if this column is the rightmost, false otherwise
     */
    public boolean isRightmost()
    {
        return (x == ginfo.width - 1);
    }
}