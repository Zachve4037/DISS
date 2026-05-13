package S3.entities;

public class Clinic {
    private int id;
    private ClinicType type;
    private boolean isOccupied;
    private boolean blocked;
    private boolean initialOnly;
    private boolean priority12Only;
    private boolean overflowActive;

    public Clinic(int id, ClinicType type) {
        this.id = id;
        this.type = type;
        this.isOccupied = false;
        this.blocked = false;
        this.initialOnly = false;
        this.priority12Only = false;
        this.overflowActive = false;
    }

    public int getId() {
        return id;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isInitialOnly() {
        return initialOnly;
    }

    public void setInitialOnly(boolean initialOnly) {
        this.initialOnly = initialOnly;
    }

    public boolean isPriority12Only() {
        return priority12Only;
    }

    public void setPriority12Only(boolean priority12Only) {
        this.priority12Only = priority12Only;
    }

    public boolean isOverflowActive() {
        return overflowActive;
    }

    public void setOverflowActive(boolean overflowActive) {
        this.overflowActive = overflowActive;
    }

    public ClinicType getType() {
        return type;
    }

}
