package concurrent;

import gnu.trove.list.array.TDoubleArrayList;

import java.util.concurrent.Exchanger;

/**
 * Mediator class to be placed between the last and the first column.
 * It hosts two subclasses, one which manipulates the steps if it recognizes convergence,
 * the other which passes new step counts into the next round.
 * 
 * @author Timo Speith and Magnus Halbe
 * @version 1.0
 */
public class PseudoColumn
{
    // Original step count. It cannot be passed.
    private final int maxSteps;
    // Column count for convergence indication
    private final int columnCount;
    
    // Current step count. Halfed if convergent, doubled if not convergent.
    private int steps;
    
    // Vaiables for periodical plotting
    private int stepCount = 0;
    private final int plottingInterval = 10000;
    private int painted = 0;
    private final boolean plottingEnabled = true;
    
    /**
     * Creates a new PseudoColumn with a specified maximal step count.
     * It is guaranteed that this step count is never surpassed.
     * 
     * @param maxSteps the original step count
     * @param columnCount the number of columns used
     */
    public PseudoColumn(int maxSteps, int columnCount)
    {
        this.maxSteps = maxSteps;
        this.steps = maxSteps;
        this.columnCount = columnCount;
    }
    
    /**
     * Synchronized method to half the step count if the columns are convergent.
     */
    private synchronized void decreaseSteps()
    {
        if(this.steps /2 > 0)
        {this.steps /= 2;}
    }
    
    /**
     * Synchronized method to set the step count to two for plotting.
     */
    private synchronized void forceTwo()
    {
        this.steps = 2;
    }
    
    /**
     * Synchronized method to double the steps if no columns are convergent
     */
    private synchronized void increaseSteps()
    {
        if(this.steps *2 <= maxSteps)
        {this.steps *= 2;}
    }
    
    /**
     * Synchronized method to signal termination. This is realized by setting
     * the step count to 0.
     */
    private synchronized void signalTermination()
    {
        this.steps = 0;
    }
    
    /**
     * Synchronized Method to get the current step count
     * @return the current step count
     */
    private synchronized int getSteps()
    {
        return this.steps;
    }
    
    /**
     * Class which listens to the last column. If there are convergent columns,
     * the step count is halved. If there are no convergent columns, the step count is doubled
     * (unless it is the original step count). If every column converges, the step count is set to 0
     * to indicate termination.
     */
    public class LeftListener implements Runnable
    {
        // Exchanger which is shared with last column
        private final Exchanger<ValueBundle> exchanger;
        private TDoubleArrayList oldValues;
        
        /**
         * Creates a listerner for the last column
         * 
         * @param ex the Exchanger with which this column comunicates with the last column
         */
        public LeftListener(Exchanger<ValueBundle> ex)
        {
            exchanger = ex;
            oldValues = new TDoubleArrayList();
        }
        
        /**
         * Run method for Threads. It mainly recognizes convergence.
         * This Method is very important for termination. The last exchange after all columns
         * terminated invokes the plotting.
         */
        @Override
        public void run()
        {
            try
            {
                boolean terminate = false;
                while(!terminate)
                {
                    stepCount += getSteps();
                    
                    ValueBundle bundle = exchanger.exchange(null);
                    
                    int convergents = bundle.getConvergents();
                    TDoubleArrayList currentValues = bundle.getValues();
                    // Euclidean norm is calculated, when convergence is detected
                    if (getSteps() <= 4 && !oldValues.isEmpty())
                    {
                    	double euclideanNorm = differenceNorm(oldValues, currentValues);
                    	if (euclideanNorm < ConcOsmosis.getEpsilon())
                    	{
                    		signalTermination();
                                terminate = true;
                    	}
                    }
                    
                    // Detect convergents
                    if(convergents == (columnCount - 1))
                    {decreaseSteps();}
                    else
                    {increaseSteps();}
                    
                    // Plotting results
                    if(plottingEnabled && stepCount >= plottingInterval && !currentValues.isEmpty())
                    {
                        final int i = painted;
                        final Converter c = new Converter(currentValues);
                        new Thread(() -> {c.write2File("./results/result" + i + ".txt");}).start();
                        stepCount = 0;
                        painted++;
                    }
                    
                    // Forcing steps to two, so that the overall values are passed.
                    if(plottingEnabled && stepCount >= plottingInterval)
                    {
                        forceTwo();
                    }
                    
                    oldValues = currentValues;
                    
                }
                
                // Wait for columns to pass 0 steps back.
                int count;
                ValueBundle vb;
                do
                {
                	vb = exchanger.exchange(null);
                	count = vb.getCurrentSteps();
                }
                while(count != 0);
                
                // Plot final version
                TDoubleArrayList finalValues = vb.getValues();
                final Converter c = new Converter(finalValues);
                c.write2File("./result.txt");
            }
            catch(InterruptedException e){System.err.println("Interrupted Listener!");}
        }
        
        
        public double differenceNorm(TDoubleArrayList oldValues, TDoubleArrayList newValues)
        {
        	double euclideanNorm = 0.0;
        	for(int i=0; i < newValues.size(); i++)
        	{
        		euclideanNorm += (oldValues.get(i) - 
        				newValues.get(i)) * (oldValues.get(i) - newValues.get(i));
        	}
        	euclideanNorm = Math.sqrt(euclideanNorm);
        	return euclideanNorm;
        }
    }
    
    /**
     * Class which comunicates with the first column. It is used to pass the current steps
     * into the next round.
     */
    public class RightPasser implements Runnable
    {
        // Exchanger which is shared with last column
        private final Exchanger<ValueBundle> exchanger;
        
        /**
         * Creates a communicator for the first column.
         * 
         * @param ex the Exchanger with which this column comunicates with the last column
         */
        public RightPasser(Exchanger<ValueBundle> ex)
        {
            exchanger = ex;
        }
        
        /**
         * Run method for Threads. It mainly passes the step count.
         */
        @Override
        public void run()
        {
            try
            {
                boolean terminate = false;
                while(!terminate)
                {
                    int steps = getSteps();
                    ValueBundle bundle = new ValueBundle(steps);
                    exchanger.exchange(bundle);
                    if(steps==0){terminate = true;}
                }
            }
            catch(InterruptedException e){System.err.println("Interrupted Passer!");}
        }
    }
}
