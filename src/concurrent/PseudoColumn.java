package concurrent;

import java.util.concurrent.Exchanger;

/**
 * 
 * @author Timo Speith
 */
public class PseudoColumn
{
    private final int maxSteps;
    
    private int steps;
    
    public PseudoColumn(int maxSteps)
    {
        this.maxSteps = maxSteps;
    }
    
    private synchronized void reduceSteps()
    {
        if(this.steps /2 > 0)
        {this.steps /= 2;}
    }
    
    private synchronized void increaseSteps()
    {
        if(this.steps *2 <= maxSteps)
        {this.steps *= 2;}
    }
    
    private synchronized int getSteps()
    {
        return this.steps;
    }
    
    public class LeftListener implements Runnable
    {
        private Exchanger<ValueBundle> exchanger;
        
        public LeftListener(Exchanger<ValueBundle> ex)
        {
            exchanger = ex;
        }
        
        @Override
        public void run()
        {
            try
            {
                while(true)
                {
                    ValueBundle bundle = exchanger.exchange(null);
                    int convergents = bundle.getConvergents();
                    if(convergents > 0)
                    {reduceSteps();}
                    else
                    {increaseSteps();}
                }
            }
            catch(InterruptedException e){/* Do Nothing*/}
        }
    }
    
    public class RightPasser implements Runnable
    {
        private Exchanger<ValueBundle> exchanger;
        
        public RightPasser(Exchanger<ValueBundle> ex)
        {
            exchanger = ex;
        }
        
        @Override
        public void run()
        {
            try
            {
                while(true)
                {
                    ValueBundle bundle = new ValueBundle(getSteps());
                    exchanger.exchange(bundle);
                }
            }
            catch(InterruptedException e){/* Do Nothing*/}
        }
    }
}
