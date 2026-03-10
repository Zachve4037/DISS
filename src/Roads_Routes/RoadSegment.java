package Roads_Routes;

public class RoadSegment {

    private final RoadColor color;
    private final int length;
    private final boolean k;

    public RoadSegment(RoadColor color, int length, boolean startsFromK) {
        this.color = color;
        this.length = length;
        this.k = startsFromK;
    }

    public RoadColor getColor() {
        return color;
    }

    public int getLength() {
        return length;
    }

    public boolean isK() {
        return k;
    }
}
