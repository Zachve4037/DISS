package S3.agents.agentresources;

import OSPABA.*;
import S3.entities.Clinic;
import S3.entities.ClinicType;
import S3.entities.EmerDepEmployee;
import S3.entities.EmployeeType;
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
        for (int i = 0; i < 13; ++i) {
            if (i <= 4) {
                this.clinics.add(new Clinic(i, ClinicType.A));
            } else {
                this.clinics.add(new Clinic(i, ClinicType.B));
            }
        }
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        // Setup component for the next replication
        initClinics();
    }

    //meta! userInfo="Generated code: do not modify", tag="begin"
    private void init() {
        new ManagerResources(Id.managerResources, mySim(), this);
        addOwnMessage(Mc.allocateDoctorsExaminationBResources);
        addOwnMessage(Mc.allocateInitialExaminationResources);
        addOwnMessage(Mc.allocateDoctorsExaminationAResources);
        addOwnMessage(Mc.releaseResources);
    }
    //meta! tag="end"
}