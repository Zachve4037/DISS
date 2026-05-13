package S3.simulation;

import OSPABA.*;
import S3.entities.Clinic;
import S3.entities.Patient;
import S3.entities.EmerDepEmployee;
import S3.entities.ClinicType;

public class Message extends OSPABA.MessageForm {
    private Patient patient;
    private EmerDepEmployee doctor;
    private EmerDepEmployee nurse;
    private Clinic clinic;
    private ClinicType requestedClinicType;
    private double processDuration;

    public Message(Simulation mySim) {
        super(mySim);
        this.patient = null;
        this.doctor = null;
        this.nurse = null;
        this.clinic = null;
        this.requestedClinicType = null;
        this.processDuration = -1;
    }

    private Message(Message original) {
        super(original);
        // copy() is called in superclass
    }

    @Override
    public MessageForm createCopy() {
        return new Message(this);
    }

    @Override
    protected void copy(MessageForm message) {
        super.copy(message);
        Message original = (Message) message;
        this.patient = original.patient;
        this.doctor = original.doctor;
        this.nurse = original.nurse;
        this.clinic = original.clinic;
        this.requestedClinicType = original.requestedClinicType;
        this.processDuration = original.processDuration;
    }

    public EmerDepEmployee getNurse() {
        return nurse;
    }

    public void setNurse(EmerDepEmployee nurse) {
        this.nurse = nurse;
    }

    public Clinic getClinic() {
        return clinic;
    }

    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public EmerDepEmployee getDoctor() {
        return doctor;
    }

    public void setDoctor(EmerDepEmployee doctor) {
        this.doctor = doctor;
    }

    public ClinicType getRequestedClinicType() {
        return requestedClinicType;
    }

    public void setRequestedClinicType(ClinicType requestedClinicType) {
        this.requestedClinicType = requestedClinicType;
    }

    public double getProcessDuration() {
        return processDuration;
    }

    public void setProcessDuration(double processDuration) {
        this.processDuration = processDuration;
    }
}