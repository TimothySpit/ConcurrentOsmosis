package concurrent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import com.google.gson.Gson;

public class ConcOsmosis {

        public static int steps = 100;
        public static double epsilon = 0.00005;
        
        private static int notifications = 0;
        private static int posnot = 0;
        
        private static int width, height;
    
	public static void main(String[] args) throws IOException, InterruptedException {
		Gson gson = new Gson();
		String json = "";
                
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
                
		GraphInfo ginfo = gson.fromJson(json, GraphInfo.class);
                
                width = ginfo.width;
                height = ginfo.height;
                        
                Set<Integer> keys = ginfo.column2row2initialValue.keySet();
                
		// Your implementation can now access ginfo to read out all important values
		ImageConvertible graph = null; // <--- you should implement ImageConvertible to write the graph out
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
                notifications = 0;
            }
        }
}
