package concurrent;

import gnu.trove.list.array.TDoubleArrayList;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.Exchanger;

public class Column implements Runnable
{
    private final int x;
    public static int height;
    public static int epsilon;
    private GraphInfo ginfo;
    private boolean verticalConvergenceDetected;
    private boolean columnConvergenceDetected;
	
	private int stepsTotal;
	private int stepsDone;

	private Exchanger<ValueBundle> leftExchanger;
	private Exchanger<ValueBundle> rightExchanger;
	private LinkedList<Node> nodeList;
	
	public Column(int xCoord, int stepsTotal, GraphInfo ginfo, Exchanger<ValueBundle> left, Exchanger<ValueBundle> right)
        {
		x = xCoord;
		this.ginfo = ginfo;
		height = ginfo.height;
		this.stepsTotal = stepsTotal;
		stepsDone = 0;
		leftExchanger = left;
		rightExchanger = right;
		nodeList = new LinkedList<>();
	}

	@Override
	public void run()
    {
		int happenedExchanges = 0;
        while(!Thread.interrupted())
        {
        	performSteps();
        	exchange();
        	happenedExchanges++;
        	//System.out.println("Exchanges here at " + x + " : " + happenedExchanges);
        	//if(!nodeList.isEmpty())
        	//System.out.println("X-Coordinate: "+x + "; "+ nodeList.getFirst().toString());
        	stepsDone = 0;
        }
        System.out.println("Column "+x + " Terminated.");
     }
	
    /**
    * Iterates through the column
    * 
    */
	public void performSteps()
	{
		verticalConvergenceDetected = false;
		while(stepsDone < stepsTotal)
		{
			ListIterator<Node> iterator = nodeList.listIterator();
			while(iterator.hasNext())
			{
				Node currentNode = iterator.next();
				
				Node previous = currentNode.updatePrevious();
				Node next = currentNode.updateNext();
				if (previous != null)
				{
					Node previousPreviousCandidate = iterator.previous();
					iterator.add(previous);
					iterator.next();
					if (previousPreviousCandidate.getY() == previous.getY()-1)
					{
						//here the newly inserted node's previous neighbour gets informed
						previousPreviousCandidate.setNext(previous);
						previous.setPrevious(previousPreviousCandidate);
					}
					initializeNode(previous);
				}
				if (next != null)
				{
					iterator.add(next);
					initializeNode(next);
					if (iterator.hasNext())
					{
						Node nextNextCandidate = iterator.next();
						if (nextNextCandidate.getY() == next.getY()+1)
						{
							//here the newly inserted node's potential next neighbour gets informed
							nextNextCandidate.setPrevious(next);
							next.setNext(nextNextCandidate);
						}
						iterator.previous();
					}
				}
			}
			
			iterator = nodeList.listIterator();
			double newEuclideanNorm=0;
			while(iterator.hasNext())
			{
				Node currentNode = iterator.next();
				double oldValue = currentNode.getValue();
				currentNode.flush();
				double newValue = currentNode.getValue();
				newEuclideanNorm += Math.pow(oldValue - newValue, 2.0);
			}
			newEuclideanNorm = Math.sqrt(newEuclideanNorm);
			if (!nodeList.isEmpty() && newEuclideanNorm < epsilon)
			{
				verticalConvergenceDetected = true;
				//TODO: All future steps until stepsDone could be skipped
			}
			stepsDone ++;
		}
	}
	
	/**
     * Exchanges values with the left exchanger, then the right exchanger
     */
	public void exchange()
	{
		TDoubleArrayList leftValues = null;
		TDoubleArrayList rightValues = null;
		
		if (!isLeftmost())
		{
			leftValues = new TDoubleArrayList(height);
			leftValues.fill(0, height,0.0);
		}
			
		if (!isRightmost())
		{
			rightValues = new TDoubleArrayList(height, 0.0);
			rightValues.fill(0, height,0.0);
		}
			
		ListIterator<Node> iterator = nodeList.listIterator();
		while(iterator.hasNext())
		{
			Node currentNode = iterator.next();
			if(!isLeftmost())
			{
				leftValues.set(currentNode.getY(), currentNode.emitLeft());
			}
			if (!isRightmost())
			{
				rightValues.set(currentNode.getY(), currentNode.emitRight());
			}
		}
		ValueBundle receivedFromLeft = null;
		ValueBundle receivedFromRight = null;
		int convergencesUntilHere = 0;
		int currentSteps = 1;
		
		//if(!isLeftmost()) //Is irrelevant. Exchanges with Column or PseudoColumn
			try {
				receivedFromLeft = leftExchanger.exchange(new ValueBundle(leftValues, 0, 0));
				convergencesUntilHere = receivedFromLeft.getConvergents();
				currentSteps = receivedFromLeft.getCurrentSteps();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		if (columnConvergenceDetected)
		{
			convergencesUntilHere++;
		}
			
		
		//if(!isRightmost()) //Is irrelevant. Exchanges with Column or PseudoColumn
			try {
				receivedFromRight = rightExchanger.exchange(new ValueBundle(rightValues, convergencesUntilHere, currentSteps));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		if (currentSteps == 0)
		{
			Thread.currentThread().interrupt();
		}
		boolean horizontalConvergencePossible = true;
		boolean inflowIsOutflowLeft = false;
		boolean inflowIsOutflowRight = false;
		
		if(!isLeftmost())
		{
			double euclideanNorm = 0.0;
			for(int i=0; i < leftValues.size(); i++)
			{
				euclideanNorm += Math.pow((leftValues.get(i) - receivedFromLeft.getValues().get(i)), 2);
			}
			euclideanNorm = Math.sqrt(euclideanNorm);
			inflowIsOutflowLeft = (euclideanNorm < epsilon);
			receiveHorizontal(leftValues);
			//TODO: Currently each column calculates the euclideanNorm in both directions
		}
			
		if(!isRightmost())
		{
			double euclideanNorm = 0.0;
			for(int i=0; i < rightValues.size(); i++)
			{
				euclideanNorm += Math.pow((rightValues.get(i) - receivedFromRight.getValues().get(i)), 2);
			}
			euclideanNorm = Math.sqrt(euclideanNorm);
			inflowIsOutflowRight = (euclideanNorm < epsilon);
			receiveHorizontal(receivedFromRight.getValues());
			//TODO: Currently each column calculates the euclideanNorm in both directions
		}
		
		columnConvergenceDetected = inflowIsOutflowLeft || inflowIsOutflowRight;
		
		/*columnConvergenceDetected = false;
		if (!nodeList.isEmpty() && horizontalConvergencePossible && verticalConvergenceDetected)
		{
			columnConvergenceDetected = true;
		}*/
			
		iterator = nodeList.listIterator();
		while(iterator.hasNext())
		{
			Node currentNode = iterator.next();
			if (!currentNode.flush())
				horizontalConvergencePossible = false;
		}
		
		stepsTotal = currentSteps;
	}
	
	/**
	 * Updates all nodes (and creates new ones if needed)
	 * with values from another column
	 * 
	 * @param TDoubleArrayList the double values that were received
	 */
	private void receiveHorizontal(TDoubleArrayList received)
	{
		ListIterator <Node> iterator = nodeList.listIterator();
		while(iterator.hasNext())
		{
			//This while-loop registers added values in all existing nodes
			Node currentNode = iterator.next();
			int y = currentNode.getY();
			currentNode.register(received.get(y));
			received.set(y, 0.0);
		}
		
		for(int y=0; y< received.size(); y++)
		{
			//At this point value is only >0 when there was no corresponding node 
			double value = received.get(y);
			if (value > 0.0)
			{
				Node newNode = new Node(value, y);
				insertNode(newNode);
			}
		}
	}
	
	/**
         * Gives an instance of node all four rates. 
         * Updates next and previous of the node, if existing.
         * @param node the node which gets rates
         */
	private void initializeNode(Node node)
	{
		System.out.println("neue Node" + x + " | "+ node.getY());
		
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
        * Inserts a node in the list, list remains sorted by y-coordinates
        * Neighbour nodes get new "previous" and "next"
        * @param node the node which gets inserted
        */
	public synchronized void insertNode(Node node)
	{
		int goalY = node.getY();
		ListIterator<Node> iterator = nodeList.listIterator();
		//TODO: binary search insert here
		int index = 0;
		while (iterator.hasNext())
		{
			if (iterator.next().getY() < goalY)
			{
				index ++;
			}
		}
		nodeList.add(index, node);
		Node candidate;
		if (index > 0)
		{
			candidate = nodeList.get(index-1);
			if (candidate.getY() == node.getY() - 1)
			{
				candidate.setNext(node);
				node.setPrevious(candidate);
			}
		}
		if (index < nodeList.size() - 1)
		{
			candidate = nodeList.get(index+1);
			if (candidate.getY() == node.getY() + 1)
			{
				node.setNext(candidate);
				candidate.setPrevious(node);
			}
		}
		initializeNode(node);
	}
	
	public  boolean isLeftmost()
	{
		return (x == 0);
	}

	public boolean isRightmost()
	{
		return (x == ginfo.width - 1);
	}
	
	/**
     * Returns a TDoubleArrayList of size height with
     * - the value of existing nodes, where nodes exist
     * - 0.0 everywhere else
     */
	public  TDoubleArrayList getNodeValues()
	{
		TDoubleArrayList values = new TDoubleArrayList(height);
		values.fill(0, height, 0.0);
		ListIterator<Node> iterator = nodeList.listIterator();
		while (iterator.hasNext())
		{
			Node currentNode = iterator.next();
			values.set(currentNode.getY(), currentNode.getValue());
		}
		return values;
	}
}
