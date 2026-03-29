package S2.Objects;

import java.util.LinkedList;

public class Traveller {
    private final int Id;
    private final LinkedList<Crate> crates;

    public Traveller(int id, int numOfCrates, int startCrateId) {
        this.Id = id;
        this.crates = new LinkedList<>();
        for (int i = startCrateId; i < numOfCrates + startCrateId; i++) {
            this.crates.add(new Crate(i, id));
        }
    }

    public int getId() {
        return Id;
    }

    public LinkedList<Crate> getCrates() {
        return crates;
    }
}
