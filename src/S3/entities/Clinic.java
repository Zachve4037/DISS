package S3.entities;

public class Clinic {
    private int id;
    private ClinicType type;
    private boolean isOccupied;

    public Clinic(int id, ClinicType type) {
        this.id = id;
        this.type = type;
        this.isOccupied = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public ClinicType getType() {
        return type;
    }

    public void setType(ClinicType type) {
        this.type = type;
    }
}
