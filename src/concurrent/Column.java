package concurrent;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.Exchanger;

public class Column implements Runnable {
	
	private final int x;
	public static int height;
	public static int epsilon;
	
	private int stepsTotal;
	private int stepsDone;

	private Exchanger left;
	private Exchanger right;
	private LinkedList<Node> nodeList;
	
	public Column(int x_coord, int max_height, int epsilon) {
		x = x_coord;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
	
	public void performStep()
	{
		while(stepsTotal < stepsDone)
		{
			
			boolean verticalConvergencePossible = true;
			ListIterator<Node> iterator = nodeList.listIterator();
			while(iterator.hasNext())
			{
				Node currentNode = iterator.next();
				/*Node previous = currentNode.updatePrevious();
				if (previous != null)
				{
					
				}*/
			}
			stepsDone ++;
		}
	}
	
	synchronized void initializeNode(Node node)
	{
		
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
