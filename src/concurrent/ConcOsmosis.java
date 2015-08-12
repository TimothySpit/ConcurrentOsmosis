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
                
                // Create first Node
                // Only create necessary Exchangers
                Exchanger left = null;
                Exchanger right = null;
                
                if(column > 0) 
                {left = new Exchanger();}
                if(column < width)//TODO width - 1
                {right = new Exchanger();}
                
                //Create the first entry and starts calcuating
                Column start = new Column(column, steps, ginfo, left, right);
                Node first = new Node(value, row);
                start.initializeNode(first);
                start.insertNode(first);
                Thread t = new Thread(start);
                t.start();
                
                //Plotting the whole thing
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