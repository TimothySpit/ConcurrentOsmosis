package concurrent;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.Exchanger;

public class Column implements Runnable {
	
	private final int x;
	public static int height;
	public static int epsilon;
	private GraphInfo ginfo;
	
	private int stepsTotal;
	private int stepsDone;

	private Exchanger left;
	private Exchanger right;
	private LinkedList<Node> nodeList;
	
	public Column(int x_coord, GraphInfo ginfo) {
		x = x_coord;
		this.ginfo = ginfo;
		height = ginfo.height;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
	
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
				if (previous != null)
				{
					initializeNode(previous);
				}
				Node next = currentNode.updateNext();
				if (next != null)
				{
					initializeNode(next);
				}
				
				//TODO: When do I flush?
			}
			stepsDone ++;
		}
	}
	
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
	}

}
