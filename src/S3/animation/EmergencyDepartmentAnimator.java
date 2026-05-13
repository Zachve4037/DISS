package S3.animation;

import OSPAnimator.AnimItem;
import OSPAnimator.AnimShape;
import OSPAnimator.AnimShapeItem;
import OSPAnimator.AnimTextItem;
import OSPAnimator.Animator;
import S3.agents.agentpatients.AgentPatients;
import S3.agents.agentresources.AgentResources;
import S3.entities.Clinic;
import S3.entities.ClinicType;
import S3.entities.EmerDepEmployee;
import S3.entities.EmployeeType;
import S3.entities.Patient;
import S3.entities.PatientType;
import S3.simulation.EmergencyDepartmentSimulation;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class EmergencyDepartmentAnimator {
    private static final int STATIC_BASE_ID = 1_000;
    private static final int CLINIC_ITEM_BASE_ID = 2_000;
    private static final int PATIENT_ITEM_BASE_ID = 100_000;
    private static final int DOCTOR_ITEM_BASE_ID = 200_000;
    private static final int NURSE_ITEM_BASE_ID = 300_000;

    private static final double ENTRANCE_X = 70;
    private static final double ENTRANCE_Y = 335;
    private static final double EXIT_X = 1280;
    private static final double EXIT_Y = 335;

    private static final double WAITING_ROOM_X = 185;
    private static final double WAITING_ROOM_Y = 235;
    private static final double DOCTOR_QUEUE_X = 185;
    private static final double DOCTOR_QUEUE_Y = 405;

    private static final double STAFF_ENTRY_X = 70;
    private static final double STAFF_ENTRY_Y = 555;
    private static final double DOCTOR_IDLE_X = 330;
    private static final double DOCTOR_IDLE_Y = 515;
    private static final double NURSE_IDLE_X = 330;
    private static final double NURSE_IDLE_Y = 585;

    private final EmergencyDepartmentSimulation simulation;

    private final Map<Integer, AnimShapeItem> patients = new HashMap<>();
    private final Map<Integer, AnimShapeItem> doctors = new HashMap<>();
    private final Map<Integer, AnimShapeItem> nurses = new HashMap<>();
    private final Map<Integer, AnimShapeItem> clinics = new HashMap<>();

    private boolean replicationLayoutReady;

    public EmergencyDepartmentAnimator(EmergencyDepartmentSimulation simulation) {
        this.simulation = simulation;
    }

    public void prepareReplication() {
        patients.clear();
        doctors.clear();
        nurses.clear();
        clinics.clear();
        replicationLayoutReady = false;

        if (!isActive()) {
            return;
        }

        try {
            simulation.animator().clearAll();
            Animator animator = animator();
            if (animator != null) {
                animator.setBackground(new Color(248, 250, 252));
                animator.setSynchronizedTime(false);
                animator.showMenu(true);
                animator.setView(0, 0, 1400, 720);
            }

            drawStaticLayout();
            drawClinics();
            drawEmployeesAtEntrance();
            replicationLayoutReady = true;
        } catch (Throwable ignored) {
        }
    }

    public void patientArrived(Patient patient) {
        if (patient == null || !ensureLayout()) {
            return;
        }
        AnimShapeItem item = getOrCreatePatientItem(patient);
        double y = patient.getType() == PatientType.AMBULANCE ? ENTRANCE_Y - 20 : ENTRANCE_Y + 20;
        item.setPosition(simulation.currentTime(), ENTRANCE_X, y);
        item.setVisible(simulation.currentTime(), true);
    }

    public void movePatientToWaitingRoom(Patient patient, double duration) {
        if (patient == null || !ensureLayout()) {
            return;
        }
        AnimShapeItem item = getOrCreatePatientItem(patient);
        moveTo(item, duration, WAITING_ROOM_X, WAITING_ROOM_Y);
    }

    public void refreshQueues() {
        if (!ensureLayout()) {
            return;
        }

        AgentPatients agentPatients;
        try {
            agentPatients = simulation.agentPatients();
        } catch (Throwable ignored) {
            return;
        }

        layoutInitialQueue(agentPatients.getInitialExaminationQueue());
        layoutDoctorQueue(agentPatients.getDoctorsExaminationQueue());
    }

    public void movePatientToClinic(Patient patient, Clinic clinic, double duration) {
        if (patient == null || !ensureLayout()) {
            return;
        }
        AnimShapeItem item = getOrCreatePatientItem(patient);
        Point p = clinicCenter(clinic);
        moveTo(item, duration, p.x, p.y);
    }

    public void movePatientToExit(Patient patient, double duration) {
        if (patient == null || !ensureLayout()) {
            return;
        }
        AnimShapeItem item = getOrCreatePatientItem(patient);
        moveTo(item, duration, EXIT_X, EXIT_Y);
        if (duration > 0) {
            item.remove(simulation.currentTime() + duration + 0.001);
        } else {
            item.remove(simulation.currentTime() + 0.001);
        }
        patients.remove(patient.getId());
    }

    public void patientDeparted(Patient patient) {
        if (patient == null || !isActive()) {
            return;
        }
        AnimShapeItem item = patients.remove(patient.getId());
        if (item != null) {
            item.remove(simulation.currentTime());
        }
    }

    public void moveInitialEmployee(EmerDepEmployee employee, double duration) {
        if (employee == null || !ensureLayout()) {
            return;
        }
        AnimShapeItem item = getOrCreateEmployeeItem(employee);
        Point target = employeeIdlePoint(employee);
        moveTo(item, duration, target.x, target.y);
    }

    public void employeeMoving(EmerDepEmployee employee, double duration) {
        if (employee == null || !ensureLayout()) {
            return;
        }
        AnimShapeItem item = getOrCreateEmployeeItem(employee);
        Point p = employeeMovePoint(employee);
        moveTo(item, duration, p.x, p.y);
    }

    public void employeeReserved(EmerDepEmployee employee, Clinic clinic) {
        if (employee == null || !ensureLayout()) {
            return;
        }
        AnimShapeItem item = getOrCreateEmployeeItem(employee);
        item.setColor(simulation.currentTime(), reservedColor(employee));
        if (clinic != null) {
            Point p = staffPointNearClinic(employee, clinic);
            moveTo(item, 0, p.x, p.y);
        }
    }

    public void employeeWorking(EmerDepEmployee employee, Clinic clinic) {
        if (employee == null || !ensureLayout()) {
            return;
        }
        AnimShapeItem item = getOrCreateEmployeeItem(employee);
        item.setColor(simulation.currentTime(), workingColor(employee));
        if (clinic != null) {
            Point p = staffPointNearClinic(employee, clinic);
            moveTo(item, 0, p.x, p.y);
        }
    }

    public void employeeIdle(EmerDepEmployee employee) {
        if (employee == null || !ensureLayout()) {
            return;
        }
        AnimShapeItem item = getOrCreateEmployeeItem(employee);
        item.setColor(simulation.currentTime(), idleColor(employee));
        Point p = employeeIdlePoint(employee);
        moveTo(item, 5, p.x, p.y);
    }

    public void clinicOccupied(Clinic clinic, boolean occupied) {
        if (clinic == null || !ensureLayout()) {
            return;
        }
        clinic.setOccupied(occupied);
        if (!occupied) {
            clinic.setOverflowActive(false);
        }
        updateClinicItem(clinic);
    }

    public void clinicBlocked(Clinic clinic, boolean blocked) {
        if (clinic == null || !ensureLayout()) {
            return;
        }
        clinic.setBlocked(blocked);
        if (!blocked) {
            clinic.setOverflowActive(false);
        }
        updateClinicItem(clinic);
    }

    private void updateClinicItem(Clinic clinic) {
        AnimShapeItem item = clinics.get(clinic.getId());
        if (item == null) {
            return;
        }
        item.setColor(simulation.currentTime(), clinicColor(clinic));
        item.setLabel(clinicLabel(clinic));
        item.setToolTip(clinicTooltip(clinic));
    }

    private void drawStaticLayout() {
        addText(STATIC_BASE_ID + 1, "Emergency Department", 36, 35, 22, Color.DARK_GRAY, true);
        addText(STATIC_BASE_ID + 2, "Entrance", ENTRANCE_X - 40, ENTRANCE_Y - 65, 14, Color.DARK_GRAY, false);
        addText(STATIC_BASE_ID + 3, "Exit", EXIT_X - 25, EXIT_Y - 65, 14, Color.DARK_GRAY, false);

        addRect(STATIC_BASE_ID + 10, 115, 170, 250, 145, new Color(229, 246, 255), false, "Waiting room / initial exam queue");
        addText(STATIC_BASE_ID + 11, "Waiting room", 145, 195, 15, Color.DARK_GRAY, false);
        addText(STATIC_BASE_ID + 12, "Initial queue", 145, 215, 12, Color.GRAY, false);

        addRect(STATIC_BASE_ID + 20, 115, 350, 250, 145, new Color(255, 245, 225), false, "Waiting for doctor examination");
        addText(STATIC_BASE_ID + 21, "Waiting for doctors", 145, 375, 15, Color.DARK_GRAY, false);
        addText(STATIC_BASE_ID + 22, "Priority queue", 145, 395, 12, Color.GRAY, false);

        addRect(STATIC_BASE_ID + 30, 45, 515, 280, 100, new Color(240, 240, 255), false, "Personnel entrance and idle positions");
        addText(STATIC_BASE_ID + 31, "Personnel", 70, 540, 15, Color.DARK_GRAY, false);
        addText(STATIC_BASE_ID + 32, "Doctors / nurses", 70, 560, 12, Color.GRAY, false);
        addText(STATIC_BASE_ID + 33, staffCountText(), 70, 582, 12, Color.GRAY, false);

        addRect(STATIC_BASE_ID + 40, 405, 60, 780, 455, new Color(250, 250, 250), false, "Clinics area");
        addText(STATIC_BASE_ID + 41, "Clinics", 420, 85, 16, Color.DARK_GRAY, false);
    }

    private void drawClinics() {
        try {
            for (Clinic clinic : simulation.agentResources().getClinics()) {
                Point c = clinicCenter(clinic);
                AnimShapeItem item = new AnimShapeItem(AnimShape.RECTANGLE, clinicColor(clinic), 1);
                item.setSize(92, 66);
                item.setFill(true);
                item.setPositionAlignment(AnimItem.PositionAlignment.CENTER);
                item.setPosition(c.x, c.y);
                item.setLabel(clinicLabel(clinic));
                item.setToolTip(clinicTooltip(clinic));
                item.setZIndex(2);
                simulation.animator().register(CLINIC_ITEM_BASE_ID + clinic.getId(), item);
                clinics.put(clinic.getId(), item);
            }
        } catch (Throwable ignored) {
        }
    }

    private void drawEmployeesAtEntrance() {
        try {
            for (EmerDepEmployee doctor : simulation.agentResources().getDoctors()) {
                AnimShapeItem item = getOrCreateEmployeeItem(doctor);
                item.setPosition(STAFF_ENTRY_X, STAFF_ENTRY_Y - 18);
            }
            for (EmerDepEmployee nurse : simulation.agentResources().getNurses()) {
                AnimShapeItem item = getOrCreateEmployeeItem(nurse);
                item.setPosition(STAFF_ENTRY_X, STAFF_ENTRY_Y + 18);
            }
        } catch (Throwable ignored) {
        }
    }

    private void layoutInitialQueue(Queue<Patient> queue) {
        if (queue == null) {
            return;
        }
        List<Patient> ordered = new ArrayList<>(queue);
        ordered.sort(Comparator
                .comparingInt((Patient p) -> p.getType() == PatientType.AMBULANCE ? 0 : 1)
                .thenComparingDouble(Patient::getArrivalTime)
                .thenComparingInt(Patient::getId));
        for (int i = 0; i < ordered.size(); i++) {
            Patient patient = ordered.get(i);
            AnimShapeItem item = getOrCreatePatientItem(patient);
            Point p = queueSlot(WAITING_ROOM_X, WAITING_ROOM_Y, i);
            moveTo(item, 2, p.x, p.y);
        }
    }

    private void layoutDoctorQueue(Queue<Patient> queue) {
        if (queue == null) {
            return;
        }
        List<Patient> ordered = new ArrayList<>(queue);
        ordered.sort(Comparator.comparingInt(Patient::getPriority)
                .thenComparingDouble(Patient::getWaitStartTime)
                .thenComparingInt(Patient::getId));
        for (int i = 0; i < ordered.size(); i++) {
            Patient patient = ordered.get(i);
            AnimShapeItem item = getOrCreatePatientItem(patient);
            Point p = queueSlot(DOCTOR_QUEUE_X, DOCTOR_QUEUE_Y, i);
            moveTo(item, 2, p.x, p.y);
        }
    }

    private AnimShapeItem getOrCreatePatientItem(Patient patient) {
        AnimShapeItem item = patients.get(patient.getId());
        if (item != null) {
            return item;
        }

        Color color = patient.getType() == PatientType.AMBULANCE
                ? new Color(226, 72, 72)
                : new Color(244, 159, 67);
        item = new AnimShapeItem(AnimShape.CIRCLE, color, 18);
        item.setFill(true);
        item.setSize(18);
        item.setPositionAlignment(AnimItem.PositionAlignment.CENTER);
        item.setZIndex(50);
        item.setLabel("P" + patient.getId());
        item.setToolTip("Patient " + patient.getId() + " " + patient.getType());
        item.setVisible(true);
        safeRegister(PATIENT_ITEM_BASE_ID + patient.getId(), item);
        patients.put(patient.getId(), item);
        return item;
    }

    private AnimShapeItem getOrCreateEmployeeItem(EmerDepEmployee employee) {
        Map<Integer, AnimShapeItem> map = employee.getType() == EmployeeType.DOCTOR ? doctors : nurses;
        AnimShapeItem item = map.get(employee.getId());
        if (item != null) {
            return item;
        }

        boolean doctor = employee.getType() == EmployeeType.DOCTOR;
        item = new AnimShapeItem(doctor ? AnimShape.SQUARE : AnimShape.TRIANGLE, idleColor(employee), doctor ? 24 : 26);
        item.setFill(true);
        item.setSize(doctor ? 24 : 26);
        item.setPositionAlignment(AnimItem.PositionAlignment.CENTER);
        item.setZIndex(40);
        item.setLabel((doctor ? "D" : "N") + employee.getId());
        item.setToolTip(employee.getType() + " " + employee.getId());
        item.setVisible(true);
        safeRegister((doctor ? DOCTOR_ITEM_BASE_ID : NURSE_ITEM_BASE_ID) + employee.getId(), item);
        map.put(employee.getId(), item);
        return item;
    }

    private void addRect(int id, double x, double y, double w, double h, Color color, boolean filled, String tooltip) {
        AnimShapeItem item = new AnimShapeItem(filled ? AnimShape.RECTANGLE : AnimShape.RECTANGLE_EMPTY, color, 1);
        item.setSize(w, h);
        item.setFill(filled);
        item.setPositionAlignment(AnimItem.PositionAlignment.TOP_LEFT);
        item.setPosition(x, y);
        item.setZIndex(1);
        item.setToolTip(tooltip);
        safeRegister(id, item);
    }

    private void addText(int id, String text, double x, double y, int size, Color color, boolean title) {
        AnimTextItem item = new AnimTextItem(text, color, new Font("Arial", title ? Font.BOLD : Font.PLAIN, size));
        item.setSize(size);
        item.setPosition(x, y);
        item.setZIndex(5);
        item.setAlwaysVisible(true);
        safeRegister(id, item);
    }

    private void moveTo(AnimShapeItem item, double duration, double x, double y) {
        if (item == null) {
            return;
        }
        double now = simulation.currentTime();
        double safeDuration = Math.max(0, duration);
        if (safeDuration <= 0.0001) {
            item.setPosition(now, x, y);
        } else {
            item.moveTo(now, safeDuration, x, y);
        }
    }

    private void safeRegister(int id, AnimShapeItem item) {
        if (!isActive()) {
            return;
        }
        try {
            simulation.animator().register(id, item);
        } catch (Throwable ignored) {
        }
    }

    private void safeRegister(int id, AnimTextItem item) {
        if (!isActive()) {
            return;
        }
        try {
            simulation.animator().register(id, item);
        } catch (Throwable ignored) {
        }
    }

    private boolean ensureLayout() {
        if (!isActive()) {
            return false;
        }
        if (!replicationLayoutReady) {
            prepareReplication();
        }
        return replicationLayoutReady;
    }

    private boolean isActive() {
        try {
            return simulation != null && simulation.animatorExists() && simulation.animator() != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private Animator animator() {
        try {
            return simulation.animator() instanceof Animator ? (Animator) simulation.animator() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Point clinicCenter(Clinic clinic) {
        if (clinic == null) {
            return new Point(WAITING_ROOM_X, WAITING_ROOM_Y);
        }
        int id = clinic.getId();
        if (id <= 4) {
            return new Point(470 + id * 135, 155);
        }
        return new Point(430 + (id - 5) * 115, 360);
    }

    private Point queueSlot(double startX, double startY, int index) {
        int column = index % 8;
        int row = index / 8;
        return new Point(startX + column * 24, startY + row * 24);
    }

    private Point employeeIdlePoint(EmerDepEmployee employee) {
        if (employee.getType() == EmployeeType.DOCTOR) {
            return new Point(DOCTOR_IDLE_X + employee.getId() * 28, DOCTOR_IDLE_Y);
        }
        return new Point(NURSE_IDLE_X + employee.getId() * 28, NURSE_IDLE_Y);
    }

    private Point employeeMovePoint(EmerDepEmployee employee) {
        if (employee.getType() == EmployeeType.DOCTOR) {
            return new Point(DOCTOR_IDLE_X + employee.getId() * 28, DOCTOR_IDLE_Y - 45);
        }
        return new Point(NURSE_IDLE_X + employee.getId() * 28, NURSE_IDLE_Y - 45);
    }

    private Point staffPointNearClinic(EmerDepEmployee employee, Clinic clinic) {
        Point c = clinicCenter(clinic);
        if (employee.getType() == EmployeeType.DOCTOR) {
            return new Point(c.x - 27, c.y - 28);
        }
        return new Point(c.x + 27, c.y - 28);
    }

    private Color clinicColor(Clinic clinic) {
        if (clinic != null && clinic.isBlocked()) {
            return new Color(251, 146, 60);
        }
        if (clinic != null && clinic.isOverflowActive()) {
            return new Color(94, 234, 212);
        }
        if (clinic != null && clinic.isOccupied()) {
            return new Color(255, 214, 214);
        }
        if (clinic != null && clinic.isInitialOnly()) {
            return new Color(147, 197, 253);
        }
        if (clinic != null && clinic.isPriority12Only()) {
            return new Color(216, 180, 254);
        }
        if (clinic != null && clinic.getType() == ClinicType.A) {
            return new Color(219, 234, 254);
        }
        return new Color(220, 252, 231);
    }

    private String clinicLabel(Clinic clinic) {
        if (clinic == null) {
            return "";
        }
        String label = clinic.getId() + " " + clinic.getType();
        if (clinic.isBlocked()) {
            return label + " BLOCKED";
        }
        if (clinic.isOverflowActive()) {
            return label + " OVERFLOW";
        }
        if (clinic.isInitialOnly()) {
            return label + " INITIAL ONLY";
        }
        if (clinic.isPriority12Only()) {
            return label + " P1/P2 ONLY";
        }
        if (clinic.isOccupied()) {
            return label + " BUSY";
        }
        return label;
    }

    private String clinicTooltip(Clinic clinic) {
        if (clinic == null) {
            return "";
        }
        StringBuilder tooltip = new StringBuilder("Clinic ")
                .append(clinic.getId())
                .append(" type ")
                .append(clinic.getType());
        if (clinic.isBlocked()) {
            tooltip.append(" | BLOCKED");
        }
        if (clinic.isInitialOnly()) {
            tooltip.append(" | INITIAL ONLY");
        }
        if (clinic.isPriority12Only()) {
            tooltip.append(" | P1/P2 ONLY");
        }
        if (clinic.isOverflowActive()) {
            tooltip.append(" | OVERFLOW");
        }
        if (clinic.isOccupied()) {
            tooltip.append(" | occupied");
        } else if (!clinic.isBlocked()) {
            tooltip.append(" | free");
        }
        return tooltip.toString();
    }

    private String staffCountText() {
        try {
            return "Doctors: " + simulation.getDoctorsCount() + " / Nurses: " + simulation.getNursesCount();
        } catch (Throwable ignored) {
            return "Doctors: - / Nurses: -";
        }
    }

    private Color idleColor(EmerDepEmployee employee) {
        return employee.getType() == EmployeeType.DOCTOR
                ? new Color(59, 130, 246)
                : new Color(168, 85, 247);
    }

    private Color reservedColor(EmerDepEmployee employee) {
        return employee.getType() == EmployeeType.DOCTOR
                ? new Color(96, 165, 250)
                : new Color(196, 181, 253);
    }

    private Color workingColor(EmerDepEmployee employee) {
        return employee.getType() == EmployeeType.DOCTOR
                ? new Color(30, 64, 175)
                : new Color(126, 34, 206);
    }

    private static class Point {
        final double x;
        final double y;

        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
