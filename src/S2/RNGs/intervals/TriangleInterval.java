package S2.RNGs.intervals;

public class TriangleInterval {
    private final int low;
    private final int mid;
    private final int high;

    public TriangleInterval(int low, int mid, int high) {
        this.low = low;
        this.mid = mid;
        this.high = high;
    }

    public int getLow() {
        return low;
    }

    public int getMid() {
        return mid;
    }

    public int getHigh() {
        return high;
    }

}