package concurrent;

import gnu.trove.list.array.TDoubleArrayList;

/**
 * Bundle of values which is passed via Exchangers between different columns
 *
 * @author Timo Speith and Magnus Halbe
 * @version 2.0
 */
public class ValueBundle
{
        private TDoubleArrayList values; // List of all values
	private TDoubleArrayList pass; // List of values to pass
	private int convergents = 0; // Number of horizontal convergent columns
	private int currentSteps; // Steps to use

        /**
         * Creates a new instance of ValueBundle
         *
         * @param values the values of all nodes
         * @param pass the values which should be passed
         * @param hConvergents the number of horizontal convergent threads until now
         * @param currentSteps the number of steps the successor should use
         */
	public ValueBundle(TDoubleArrayList values, TDoubleArrayList pass,
                int hConvergents, int currentSteps)
        {
            this.values = values;
            this.pass = pass;
            this.convergents = hConvergents;
            this.currentSteps = currentSteps;
	}

        /**
         * Creates a new instance of ValueBundle with no values and no convergents.
         *
         * @param currentSteps the number of steps the successor should use
         */
        public ValueBundle(int currentSteps)
        {
            this(new TDoubleArrayList(), null, 0, currentSteps);
        }

        /**
         * Creates a new instance of ValueBundle with no convergents and no steps.
         *
         * @param pass the values to be passed
         */
        public ValueBundle(TDoubleArrayList pass)
        {
            this(null, pass, 0, 0);
        }
        
        /**
         * Returns all node values
         *
         * @return all node values
         */
	public TDoubleArrayList getValues()
	{
            return values;
	}

        /**
         * Returns this columns node-values
         *
         * @return this columns node-values
         */
	public TDoubleArrayList getPass()
	{
            return pass;
	}

        /**
         * Returns the horizontal convergent columns until this exchange
         *
         * @return the horizontal convergent columns until this exchange
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