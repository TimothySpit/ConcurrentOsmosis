package concurrent;

import gnu.trove.list.array.TDoubleArrayList;

/**
 * Bundle of values which is passed via Exchangers between different columns
 * 
 * @author Timo Speith & Magnus Halbe
 * @version 1.0
 */
public class ValueBundle
{
	private TDoubleArrayList values; // List of all values
	private int convergents = 0; // Number of convergent columns
	private int currentSteps; // Steps to use
	
        /**
         * Creates a new instance of ValueBundle
         * 
         * @param values the values which should be passed
         * @param convergents the number of convergent threads until now
         * @param currentSteps the number of steps the successor should use
         */
	public ValueBundle(TDoubleArrayList values, int convergents, int currentSteps)
        {
            this.values = values;
            this.currentSteps = currentSteps;
            this.convergents = convergents;
	}
        
        /**
         * Creates a new instance of ValueBundle with no values and no convergents.
         * 
         * @param currentSteps the number of steps the sucessor should use
         */
        public ValueBundle(int currentSteps)
        {
            this(null, 0, currentSteps);   
        }
	
        /**
         * Returns this columns node-values
         * 
         * @return this columns node-values
         */
	public TDoubleArrayList getValues()
	{
            return values;
	}
	
        /**
         * Returns the convergent columns until this exchange
         * 
         * @return the convergent columns until this exchange
         */
	public int getConvergents()
	{
            return convergents;
	}
	
        /**
         * Returns the step count the successor column should use
         * 
         * @return the step count the successor column should use
         */
	public int getCurrentSteps()
	{
            return currentSteps;
	}
}
