package concurrent;

import gnu.trove.list.array.TDoubleArrayList;

public class ValueBundle
{
	private TDoubleArrayList values;
	private int convergents = 0;
	private int currentSteps;
	
	public ValueBundle(TDoubleArrayList values, int convergents, int currentSteps)
        {
            this.values = values;
            this.currentSteps = currentSteps;
            this.convergents = convergents;
	}
        
        public ValueBundle(int currentSteps)
        {
            this(null, 0, currentSteps);   
        }
	
	public TDoubleArrayList getValues()
	{
            return values;
	}
	
	public int getConvergents()
	{
            return convergents;
	}
	
	public int getCurrentSteps()
	{
            return currentSteps;
	}
}
