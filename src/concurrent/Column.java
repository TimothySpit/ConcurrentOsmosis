package concurrent;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.Exchanger;

public class Column implements Runnable {

	private static int x;
	private static int height;
	private Exchanger left;
	private Exchanger right;
	private LinkedList<Node> nodeList;
	
	public Column(int x_coord, int max_height) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
	
	synchronized void insertNode(Node node)
	{
		int goalY = node.getY();
		ListIterator<Node> iterator = nodeList.listIterator();
		//TODO: binary search insert here
		int index = 0;
		while (iterator.hasNext())
		{
			if (goalY < iterator.next().getY())
			{
				index ++;
			}
		}
		nodeList.add(index, node);
	}

}
