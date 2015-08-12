package concurrent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import com.google.gson.Gson;

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
                
                steps = originalSteps; // First step count is original step count
                
		// read data in
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
                
                //Create first Node
                Set<Integer> keys = ginfo.column2row2initialValue.keySet();
                
                
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