package S2.Objects;

public class Crate {
    private final int id;
    private final int tId;

    public Crate(int id, int tId) {
        this.id = id;
        this.tId = tId;
    }

    public int getId() {
        return id;
    }

    public int gettId() {
        return tId;
    }
}
