package S3.entities;

public class Patient {
    private final int id;
    private final PatientType type;

    public Patient(int id, PatientType type) {
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public PatientType getType() {
        return type;
    }
}
