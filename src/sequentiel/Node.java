package sequentiel;

import concurrent.GraphInfo;
import concurrent.GuardedCommand;
import concurrent.Neighbour;

public class Node
{
    private double value = 0.0;
    private double[] rates = new double[4];
    private GraphInfo info = null;
    private int x;
    private int y;
    
    public Node(GraphInfo info, int x, int y)
    {
        this.info = info;
        this.x = x;
        this.y = y;
    }
    
    private void setNeighbour(int i, double rate)
    {
        rates[i] = rate;
    }
    
    public void update(Node[] neighbours)
    {
        double tmp = 0.0;
        for(int i = 0; i < 4; i++)
        {
            Node n = neighbours[i];
            if(n != null)
            {
                tmp += value*rates[i];
            }
            else
            {
                if(rates[i] > 0)
                {createNode(i);}
                
            }
        }
    }
    
    private void createNode(int i)
    {
        Node n = null;
        
        switch(i)
        {
            case 0: n = new Node(info, x-1, y); break;
            case 1: n = new Node(info, x, y-1); break;
            case 2: n = new Node(info, x+1, y); break;
            case 3: n = new Node(info, x, y+1); break;
            default: throw new RuntimeException("Bli bla blubb!");
        }
        
        n.setNeighbour(0, info.getRateForTarget(x, y, Neighbour.Left));
        n.setNeighbour(1, info.getRateForTarget(x, y, Neighbour.Top));
        n.setNeighbour(2, info.getRateForTarget(x, y, Neighbour.Right));
        n.setNeighbour(3, info.getRateForTarget(x, y, Neighbour.Bottom));   
    }
    
    private void pass(double value)
    {
        this.value += value;
    }
    
    
}
