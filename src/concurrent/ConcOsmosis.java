package concurrent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.concurrent.Exchanger;

public class ConcOsmosis {

        private static final int originalSteps = 100;
        public static int steps;
        
        private static Column[] columns;
        
        public static double epsilon;
        
        // Parameter for convergence
        private static int notifications = 0;
        private static int posnot = 0;
        
        private static int width, height;
    
	public static void main(String[] args) throws IOException, InterruptedException
        {
		Gson gson = new Gson();
		String json = "";
                
                // First step count is original step count
                steps = originalSteps;
                
		// Read data in
		if (args.length != 0)
                {
                    Path path = Paths.get(args[1]);
                    try
                    {json = new String(Files.readAllBytes(path));}
                    catch (IOException e)
                    {e.printStackTrace();}
		}
                else
                {System.err.println("You must provide the serialized file as the first argument!");}
                
                // Get info to work on
		GraphInfo ginfo = gson.fromJson(json, GraphInfo.class);
                
                width = ginfo.width;
                height = ginfo.height;
                epsilon = ginfo.epsilon;
                
                // Get coordinates and value for first Node
                Set<Integer> keys = ginfo.column2row2initialValue.keySet();
                int column = keys.iterator().next();
                HashMap<Integer, Double> valueMap = ginfo.column2row2initialValue.get(column);
                Set<Integer> keys2 = valueMap.keySet();
                int row = keys.iterator().next();
                double value = valueMap.get(row);
                
                //Create all Column
                Thread t;
                Exchanger left = null;
                Exchanger right = new Exchanger();
                for(int i = 0; i < column; i++)
                {
                    Column current = new Column(i, steps, ginfo, left, right);
                    t = new Thread(current);
                    t.start();
                    left = right;
                    right = new Exchanger();
                }
                
                Column start = new Column(column, steps, ginfo, left, right);
                Node first = new Node(value, row);
                start.initializeNode(first);
                start.insertNode(first);
                t = new Thread(start);
                t.start();
                
                left = right;
                right = new Exchanger(); 
                
                for(int i = column + 1; i < width - 1; i++)
                {
                    Column current = new Column(i, steps, ginfo, left, right);
                    t = new Thread(current);
                    t.start();
                    left = right;
                    right = new Exchanger(); 
                }
                
                left = right;
                right = null;
                Column last = new Column(width-1, steps, ginfo, left, right);
                
                // Plotting the whole thing
		ImageConvertible graph = null;
		ginfo.write2File("./result.txt", graph);
	}
        
        public static synchronized void checkForConvergence(int column, boolean converges)
        {
            notifications++;
            if(converges){posnot++;}
            if(notifications == width)
            {
                if(posnot > 0)
                {
                    steps /= 2;
                }
                else if(posnot == 0 && steps < originalSteps)
                {
                    steps *= 2;
                }
                notifications = 0;
            }
        }
}