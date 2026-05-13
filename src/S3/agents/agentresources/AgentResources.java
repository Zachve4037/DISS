package S3.agents.agentresources;

import OSPABA.*;
import S3.entities.Clinic;
import S3.entities.ClinicType;
import S3.entities.EmerDepEmployee;
import S3.entities.EmployeeType;
import S3.entities.Patient;
import S3.simulation.*;

import java.util.ArrayList;
import java.util.List;

//meta! id="61"
public class AgentResources extends OSPABA.Agent {
    private List<Clinic> clinics;
    private List<EmerDepEmployee> doctors;
    private List<EmerDepEmployee> nurses;

    private int numberOfDocs;
    private int numberOfNurses;

    public AgentResources(int id, Simulation mySim, Agent parent) {
        super(id, mySim, parent);
        init();
        this.clinics = new ArrayList<>();
        this.doctors = new ArrayList<>();
        this.nurses = new ArrayList<>();
    }

    public List<Clinic> getClinics() {
        return clinics;
    }

    public List<EmerDepEmployee> getDoctors() {
        return doctors;
    }

    public List<EmerDepEmployee> getNurses() {
        return nurses;
    }

    public void setNumberOfPersonal(int doctors, int nurses) {
        this.numberOfDocs = doctors;
        this.numberOfNurses = nurses;

        this.doctors.clear();
        this.nurses.clear();

        for (int i = 0; i < numberOfDocs; i++) {
            this.doctors.add(new EmerDepEmployee(mySim(), i, EmployeeType.DOCTOR));
        }
        for (int i = 0; i < numberOfNurses; i++) {
            this.nurses.add(new EmerDepEmployee(mySim(), i, EmployeeType.NURSE));
        }
    }

    public void initClinics() {
        this.clinics.clear();
        for (int i = 0; i < 12; ++i) {
            if (i <= 4) {
                this.clinics.add(new Clinic(i, ClinicType.A));
            } else {
                this.clinics.add(new Clinic(i, ClinicType.B));
            }
        }

        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();
        if (sim.isReserveInitialClinicsEnabled()) {
            this.clinics.stream()
                    .filter(c -> c.getType() == ClinicType.B)
                    .limit(2)
                    .forEach(c -> c.setInitialOnly(true));
        }

        if (sim.isReservePriority12ClinicEnabled()) {
            this.clinics.stream()
                    .filter(c -> c.getType() == ClinicType.A)
                    .findFirst()
                    .ifPresent(c -> c.setPriority12Only(true));
        }
    }

    public boolean isClinicFree(Clinic clinic) {
        return clinic != null && !clinic.isOccupied() && !clinic.isBlocked();
    }

    public boolean canClinicServeInitial(Clinic clinic, boolean overflowRequest) {
        if (!isClinicFree(clinic) || clinic.getType() != ClinicType.B) {
            return false;
        }

        if (overflowRequest) {
            return !clinic.isInitialOnly() && !hasOverflowClinicActive();
        }

        return true;
    }

    public boolean canClinicServeDoctor(Patient patient, Clinic clinic) {
        if (patient == null || !isClinicFree(clinic)) {
            return false;
        }

        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();
        if (sim.isReserveInitialClinicsEnabled() && clinic.isInitialOnly()) {
            return false;
        }

        int priority = patient.getPriority();
        if (sim.isReservePriority12ClinicEnabled() && clinic.isPriority12Only() && priority > 2) {
            return false;
        }

        if (clinic.getType() == ClinicType.A) {
            return priority >= 1 && priority <= 4;
        }
        return priority >= 3 && priority <= 5;
    }

    public Clinic firstFreeInitialClinic(boolean overflowRequest) {
        if (!overflowRequest && ((EmergencyDepartmentSimulation) mySim()).isReserveInitialClinicsEnabled()) {
            Clinic reserved = clinics.stream()
                    .filter(Clinic::isInitialOnly)
                    .filter(c -> canClinicServeInitial(c, false))
                    .findFirst()
                    .orElse(null);
            if (reserved != null) {
                return reserved;
            }
        }

        return clinics.stream()
                .filter(c -> canClinicServeInitial(c, overflowRequest))
                .findFirst()
                .orElse(null);
    }

    public Clinic firstFreeDoctorClinic(Patient patient, ClinicType requestedType) {
        if (patient == null || requestedType == null) {
            return null;
        }

        Clinic lastClinic = patient.getLastClinic();
        if (lastClinic != null
                && lastClinic.getType() == requestedType
                && canClinicServeDoctor(patient, lastClinic)) {
            return lastClinic;
        }

        return clinics.stream()
                .filter(c -> c.getType() == requestedType)
                .filter(c -> canClinicServeDoctor(patient, c))
                .findFirst()
                .orElse(null);
    }

    public Clinic firstFreeDoctorClinic(Patient patient) {
        if (patient == null) {
            return null;
        }

        Clinic lastClinic = patient.getLastClinic();
        if (lastClinic != null && canClinicServeDoctor(patient, lastClinic)) {
            return lastClinic;
        }

        if (patient.getPriority() <= 2) {
            return firstFreeDoctorClinic(patient, ClinicType.A);
        }

        if (patient.getPriority() == 5) {
            return firstFreeDoctorClinic(patient, ClinicType.B);
        }

        Clinic clinicA = firstFreeDoctorClinic(patient, ClinicType.A);
        return clinicA != null ? clinicA : firstFreeDoctorClinic(patient, ClinicType.B);
    }

    public boolean hasOverflowClinicActive() {
        return clinics.stream().anyMatch(Clinic::isOverflowActive);
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        initClinics();
    }

    //meta! userInfo="Generated code: do not modify", tag="begin"
    private void init() {
        new ManagerResources(Id.managerResources, mySim(), this);
        addOwnMessage(Mc.allocateDoctorsExaminationBResources);
        addOwnMessage(Mc.allocateInitialExaminationResources);
        addOwnMessage(Mc.allocateDoctorsExaminationAResources);
        addOwnMessage(Mc.allocateDoctorsExaminationResources);
        addOwnMessage(Mc.releaseResources);
    }
    //meta! tag="end"
}
