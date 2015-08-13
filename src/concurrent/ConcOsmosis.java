package concurrent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.concurrent.Exchanger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConcOsmosis
{
        private static Column[] columns;
        
        public static double epsilon;
        
        private static int width, height;
        
        private final static int STEPS = 100;
        
        
        public static boolean terminated;
        public static final ReentrantLock LOCK = new ReentrantLock();
        public static final Condition CONDITION = LOCK.newCondition();
    
	public static void main(String[] args) throws IOException, InterruptedException
        {
		Gson gson = new Gson();
		String json = "";
                
		// Read data in
		if (args.length != 0)
                {
                    Path path = Paths.get(args[0]);
                    try
                    {json = new String(Files.readAllBytes(path));}
                    catch (IOException e){/* Do Nothing */}
		}
                else
                {System.err.println("You must provide the serialized file as the first argument!");}
                
                // Get info to work on
		GraphInfo ginfo = gson.fromJson(json, GraphInfo.class);
                width = ginfo.width;
                height = ginfo.height;
                epsilon = ginfo.epsilon;
                
                columns = new Column[width];
                
                // Create step-mediator
                PseudoColumn mediator = new PseudoColumn(STEPS, width);
                Exchanger<ValueBundle> rightExchanger = new Exchanger<>();
                Exchanger<ValueBundle> leftExchanger = new Exchanger<>();
                
                PseudoColumn.LeftListener leftPasser = mediator.new LeftListener(leftExchanger);
                PseudoColumn.RightPasser rightPasser = mediator.new RightPasser(rightExchanger);
                
                // Get coordinates and value for first Node
                Set<Integer> keys = ginfo.column2row2initialValue.keySet();
                int column = keys.iterator().next();
                HashMap<Integer, Double> valueMap = ginfo.column2row2initialValue.get(column);
                Set<Integer> keys2 = valueMap.keySet();
                int row = keys.iterator().next();
                double value = valueMap.get(row);
                
                //Create all Column
                Thread t;
                Exchanger<ValueBundle> left = rightExchanger;
                Exchanger<ValueBundle> right = new Exchanger<>();
                for(int i = 0; i < column; i++)
                {
                    Column current = new Column(i, STEPS, ginfo, left, right);
                    columns[i] = current;
                    t = new Thread(current);
                    t.start();
                    left = right;
                    right = new Exchanger<>();
                }
                
                Column start = new Column(column, STEPS, ginfo, left, right);
                columns[column] = start;
                Node first = new Node(value, row);
                start.initializeNode(first);
                start.insertNode(first);
                t = new Thread(start);
                t.start();
                
                left = right;
                right = new Exchanger<>(); 
                
                for(int i = column + 1; i < width-1; i++)
                {
                    Column current = new Column(i, STEPS, ginfo, left, right);
                    columns[i] = current;
                    t = new Thread(current);
                    t.start();
                    left = right;
                    right = new Exchanger<>(); 
                }
                
                right = leftExchanger;
                Column last = new Column(width-1, STEPS, ginfo, left, right);
                columns[width-1] = last;
                t = new Thread(last);
                t.start();
                
                // Start passers
                Thread rt = new Thread(rightPasser);
                Thread lt = new Thread(leftPasser);
                rt.start();
                lt.start();
                
                // Waiting for termination
                LOCK.lock();
                while(!terminated)
                {
                    CONDITION.await();
                    LOCK.lock();
                }
                LOCK.unlock();
                
                // Plotting the whole thing
		ImageConvertible graph = new Converter();
		ginfo.write2File("./result.txt", graph);
	}
        
        private static class Converter implements ImageConvertible
        {

            @Override
            public double getValueAt(int column, int row)
            {
                return columns[column].getNodeValues().get(row);
            }
        }
}