package S3.entities;

public class EmerDepEmployee {
    private final int id;
    private final EmployeeType type;

    public EmerDepEmployee(int id, EmployeeType type) {
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public EmployeeType getType() {
        return type;
    }
}
