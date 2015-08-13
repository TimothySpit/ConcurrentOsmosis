package concurrent;

import gnu.trove.list.array.TDoubleArrayList;

public class ValueBundle {
	TDoubleArrayList values;
	int convergents = 0;
	int currentMaxSteps;
	
	public ValueBundle(TDoubleArrayList values, int convergents, int currentMaxSteps) {
		this.values = values;
		this.currentMaxSteps = currentMaxSteps;
		this.convergents = convergents;
	}
	
	public TDoubleArrayList getValues()
	{
		return values;
	}
	
	public int getConvergents()
	{
		return convergents;
	}
	
	public int getCurrentMaxSteps()
	{
		return currentMaxSteps;
	}
}
