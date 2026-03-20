package S1.RNGs.intervals;

public class UniformInterval {
    private final int low;
    private final int high;

    public UniformInterval(int low, int high) {
        this.low = low;
        this.high = high;
    }

    public int getLow() {
        return low;
    }

    public int getHigh() {
        return high;
    }
}
