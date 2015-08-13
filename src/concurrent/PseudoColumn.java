package concurrent;

import java.util.concurrent.Exchanger;

/**
 * Mediator class to be placed between the last and the first column.
 * It hosts two subclasses, one which manipulates the steps if it recognizes convergence,
 * the other which passes new step counts into the next round.
 * 
 * @author Timo Speith & Magnus Halbe
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
        this.columnCount = columnCount;
    }
    
    /**
     * Synchronized method to half the step count if the columns are convergent.
     */
    private synchronized void reduceSteps()
    {
        if(this.steps /2 > 0)
        {this.steps /= 2;}
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
        
        /**
         * Creates a listerner for the last column
         * 
         * @param ex the Exchanger with which this column comunicates with the last column
         */
        public LeftListener(Exchanger<ValueBundle> ex)
        {
            exchanger = ex;
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
                while(!Thread.interrupted())
                {
                    ValueBundle bundle = exchanger.exchange(null);
                    int convergents = bundle.getConvergents();
                    if(convergents == 0)
                    {increaseSteps();}
                    else if(convergents < columnCount)
                    {reduceSteps();}
                    else
                    {signalTermination(); Thread.currentThread().interrupt();}
                }
                exchanger.exchange(null);
                ConcOsmosis.LOCK.lock();
                ConcOsmosis.terminated = true;
                ConcOsmosis.LOCK.unlock();
                ConcOsmosis.CONDITION.signal();
            }
            catch(InterruptedException e){/* Do Nothing*/}
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
                while(!Thread.interrupted())
                {
                    int steps = getSteps();
                    ValueBundle bundle = new ValueBundle(steps);
                    exchanger.exchange(bundle);
                    if(steps==0){Thread.currentThread().interrupt();}
                }
            }
            catch(InterruptedException e){/* Do nothing*/}
        }
    }
}
