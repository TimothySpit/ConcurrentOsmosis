package concurrent;

public interface GuardedCommand
{
	double getRateForTarget(final int x, final int y, Neighbour where);
}



