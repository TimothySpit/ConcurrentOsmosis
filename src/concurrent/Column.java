package concurrent;

import gnu.trove.list.array.TDoubleArrayList;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.Exchanger;

public class Column implements Runnable {
	
	private final int x;
	public static int height;
	public static int epsilon;
	private GraphInfo ginfo;
	private ConcOsmosis cosmosis;
	
	private int stepsTotal;
	private int stepsDone;

	private Exchanger<TDoubleArrayList> leftExchanger;
	private Exchanger<TDoubleArrayList> rightExchanger;
	private LinkedList<Node> nodeList;
	
	public Column(int x_coord, int stepsTotal, ConcOsmosis cosmosis,GraphInfo ginfo, Exchanger<TDoubleArrayList> left, Exchanger<TDoubleArrayList> right) {
		x = x_coord;
		this.ginfo = ginfo;
		height = ginfo.height;
		this.cosmosis = cosmosis;
		stepsDone = 0;
		leftExchanger = left;
		rightExchanger = right;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
	
	/**
     * Iterates through the column and exchanges values
     * 
     */
	public void performSteps()
	{
		while(stepsTotal < stepsDone)
		{
			boolean verticalConvergencePossible = true;
			ListIterator<Node> iterator = nodeList.listIterator();
			while(iterator.hasNext())
			{
				Node currentNode = iterator.next();
				Node previous = currentNode.updatePrevious();
				Node next = currentNode.updateNext();
				if (previous != null)
				{
					initializeNode(previous);
					iterator.previous();
					iterator.add(previous);
					iterator.next();
				}
				if (next != null)
				{
					initializeNode(next);
					iterator.add(next);
				}
				
				//TODO: When do I flush?
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
			leftValues = new TDoubleArrayList(height, 0.0);
		if (!isRightmost())
			rightValues = new TDoubleArrayList(height, 0.0);
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
				leftValues.set(currentNode.getY(), currentNode.emitRight());
			}
		}
		
		if(!isLeftmost())
			try {
				TDoubleArrayList receivedFromLeft = leftExchanger.exchange(leftValues);
				receiveVertical(receivedFromLeft);
				
				//TODO: Add convergence and stuff
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		else
		{
			//TODO: The leftmost column is about to exchange to the right
		}
		if(!isRightmost())
			try {
				rightExchanger.exchange(rightValues);
				//TODO: Add using values, convergence and stuff
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		else
		{
			//TODO: Everyone just passed all their horizontal values
		}
		
	}
	
	/**
	 * Updates all nodes (and creates new ones if needed)
	 * with values emitted from the column on the left to this one
	 * 
	 * @param TDoubleArrayList the double values that were received
	 */
	synchronized void receiveVertical(TDoubleArrayList receivedFromLeft)
	{
		ListIterator <Node> iterator = nodeList.listIterator();
		while(iterator.hasNext())
		{
			//This while-loop registers added values in all existing nodes
			Node currentNode = iterator.next();
			int y = currentNode.getY();
			currentNode.register(receivedFromLeft.get(y));
			receivedFromLeft.set(y, 0);
		}
		
		for(int y=0; y< receivedFromLeft.size(); y++)
		{
			//At this point value is only >0 when there was no corresponding node 
			double value = receivedFromLeft.get(y);
			if (value <= 0)
			{
				Node newNode = new Node(value, y);
				initializeNode(newNode);
			}
		}
	}
	
	/**
     * Gives an instance of node all four rates. 
     * 
     * @param node the node which gets rates
     */
	synchronized void initializeNode(Node node)
	{
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
     * 
     * @param node the node which gets inserted
     */
	synchronized void insertNode(Node node)
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
		initializeNode(node);
	}
	
	public synchronized boolean isLeftmost()
	{
		return (x == 0);
	}

	public boolean isRightmost()
	{
		return (x == ginfo.width-1);
	}
}
