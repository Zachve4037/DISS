package RNGs.intervals;

public class EmpiricInterval {
    private final int low;
    private final int high;
    private final double p;

    public EmpiricInterval(int low, int high, double p) {
        this.low = low;
        this.high = high;
        this.p = p;
    }

    public int getHigh() {
        return high;
    }

    public int getLow() {
        return low;
    }

    public double getP() {
        return p;
    }
}
