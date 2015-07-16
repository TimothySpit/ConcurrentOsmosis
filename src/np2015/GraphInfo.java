package np2015;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class GraphInfo implements GuardedCommand {

	public final int width;
	public final int height;
	private boolean allGuardsAdded = false;
	public final HashMap<Integer, HashMap<Integer, Double>> column2row2initialValue = new HashMap<>();
	// guards are simple equations of the form  gx <= x < gX /\ gy <= y < gY  :ax + by + c, where x and y are the coordinates and a, b
	// and c are fixed constants
	public final ArrayList<int[]> guards = new ArrayList<>();
	public final ArrayList<double[]> commands = new ArrayList<>();

	
	public GraphInfo(final int width, final int height) {
		this.width = width;
		this.height = height;
	}
		
	public void addInitialEntry(int row, int column, double value) {
		HashMap<Integer, Double> tmp =  column2row2initialValue.getOrDefault(row, new HashMap<>());
		tmp.put(column, value);
		column2row2initialValue.put(row, tmp);
	}
	
	// adds the guard from (x,y) to its neighbor if gx <= x < gX /\ gy <= y < gY with rate  a*xy + b*x + c*y + d
	public void addGuard(int gx, int gX, int gy, int gY,  Neighbor neighbor, double a, double b, double c, double d) {
		assert(!allGuardsAdded);
		final int[] guard = new int[5];
		final double[] command = new double[4];
		guard[0] = neighbor.ordinal();		
		guard[1] = gx;
		guard[2] = gX;
		guard[3] = gy;
		guard[4] = gY;
		command[0] = a;
		command[1] = b;
		command[2] = c;
		command[3] = d;
		guards.add(guard);
		commands.add(command);
	}
	
	public void normalizeGuards() {
		// TODO: a later implementation will normalize guards, in case the sum of outgoing rates
		// is > 1 at some point
		// all provided files are already normalized
		allGuardsAdded = true;
	}
	
	
	/**
	 * 
	 * @param path2file: The path where you want to store the result file
	 * @param ic: You should implement this interface
	 * Writes the matrix to the file at path2file in a format understood by gnuplot
	 */
	public void write2File(String path2file, ImageConvertible ic) {
		StringBuilder builder = new StringBuilder();
		for (int row=0;row<height;++row) {
			for (int column=0;column<width;++column) {
				builder.append(ic.getValueAt(column, row));
				builder.append(" ");
			}
			builder.append("\n");
		}
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path2file)))) {
		    out.println(builder.toString());
		}catch (IOException e) {
		    System.err.println(e);
		}		
	}
	
	@Override
	/** 
	 * This is the most useful method for you in the project:
	 * By just providing the x and y coordinate + the desired neighbor
	 * you'll get the correct rate
	 */
	public synchronized double getRateForTarget(int x, int y, Neighbor where) {
		assert(guards.size() > 0);
		assert(allGuardsAdded);
		if (where == Neighbor.Left && x == 0)
			return 0;
		else if (where == Neighbor.Right && x == width-1)
			return 0;
		else if (where == Neighbor.Top && y == 0)
			return 0;
		else if (where == Neighbor.Bottom && y == height-1)
			return 0;
		for (int i = 0; i<guards.size(); ++i) {
			int[] guard = guards.get(i);
			if (guard[0] == where.ordinal() &&
				guard[1] <= x &&
				x < guard[2] &&
				guard[3] <= y &&
				y < guard[4]
			    ) {
				double[] command = commands.get(i);
				return command[0]*x*y + command[1]*x + command[2]*y + command[3];
			}
		}
		return 0;
	}

}
