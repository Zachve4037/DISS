package S3.entities;

public enum EmployeeState {
    IDLE,       // voľný
    RESERVED,   // pridelený k pacientovi, ale ešte nepracuje
    MOVING,     // presúva sa
    WORKING     // reálne vyšetruje pacienta
}