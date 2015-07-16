package np2015;

enum Neighbor {
	Left,
	Right,
	Top,
	Bottom
}

public interface GuardedCommand {
	double getRateForTarget(final int x, final int y, Neighbor where);
}
