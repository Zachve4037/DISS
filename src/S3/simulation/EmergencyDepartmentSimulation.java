package S3.simulation;

import S3.agents.agentboss.AgentBoss;
import S3.agents.agentenvironment.AgentEnvironment;
import S3.agents.agenthospital.AgentHospital;
import S3.agents.agentmovement.AgentMovement;
import S3.agents.agentpatients.AgentPatients;
import S3.agents.agentresources.AgentResources;
import S3.entities.Patient;
import S3.entities.PatientType;

public class EmergencyDepartmentSimulation extends OSPABA.Simulation {
    // simulation-level statistics
    private long simulationArrivals;
    private long simulationDepartures;
    private long simulationAmbulanceArrivals;
    private long simulationWalkInArrivals;

    private double simulationInitialWaitSum;
    private double simulationDoctorWaitAmbulanceSum;
    private double simulationDoctorWaitWalkInSum;
    private double simulationSystemTimeSum;

    private long simulationInitialWaitCount;
    private long simulationDoctorWaitAmbulanceCount;
    private long simulationDoctorWaitWalkInCount;
    private long simulationSystemTimeCount;

    // replication-level statistics
    private long replicationArrivals;
    private long replicationDepartures;
    private long replicationAmbulanceArrivals;
    private long replicationWalkInArrivals;

    private double replicationInitialWaitSum;
    private double replicationDoctorWaitAmbulanceSum;
    private double replicationDoctorWaitWalkInSum;
    private double replicationSystemTimeSum;

    private long replicationInitialWaitCount;
    private long replicationDoctorWaitAmbulanceCount;
    private long replicationDoctorWaitWalkInCount;
    private long replicationSystemTimeCount;

    public EmergencyDepartmentSimulation() {
        init();
    }

    public synchronized void recordArrival(Patient patient) {
        if (patient == null) {
            return;
        }

        simulationArrivals++;
        replicationArrivals++;

        if (patient.getType() == PatientType.AMBULANCE) {
            simulationAmbulanceArrivals++;
            replicationAmbulanceArrivals++;
        } else if (patient.getType() == PatientType.WALK_IN) {
            simulationWalkInArrivals++;
            replicationWalkInArrivals++;
        }
    }

    public synchronized void recordDeparture(Patient patient) {
        if (patient == null) {
            return;
        }

        double initialWait = patient.getInitialExamWaitTime();
        double doctorWait = patient.getDoctorsExamWaitTime();
        double systemTime = patient.getTotalTimeInSystem();

        // simulation stats
        simulationDepartures++;
        simulationInitialWaitSum += initialWait;
        simulationSystemTimeSum += systemTime;
        simulationInitialWaitCount++;
        simulationSystemTimeCount++;

        if (patient.getType() == PatientType.AMBULANCE) {
            simulationDoctorWaitAmbulanceSum += doctorWait;
            simulationDoctorWaitAmbulanceCount++;
        } else if (patient.getType() == PatientType.WALK_IN) {
            simulationDoctorWaitWalkInSum += doctorWait;
            simulationDoctorWaitWalkInCount++;
        }

        // replication stats
        replicationDepartures++;
        replicationInitialWaitSum += initialWait;
        replicationSystemTimeSum += systemTime;
        replicationInitialWaitCount++;
        replicationSystemTimeCount++;

        if (patient.getType() == PatientType.AMBULANCE) {
            replicationDoctorWaitAmbulanceSum += doctorWait;
            replicationDoctorWaitAmbulanceCount++;
        } else if (patient.getType() == PatientType.WALK_IN) {
            replicationDoctorWaitWalkInSum += doctorWait;
            replicationDoctorWaitWalkInCount++;
        }
    }

    public synchronized void resetSimulationStatistics() {
        simulationArrivals = 0;
        simulationDepartures = 0;
        simulationAmbulanceArrivals = 0;
        simulationWalkInArrivals = 0;

        simulationInitialWaitSum = 0;
        simulationDoctorWaitAmbulanceSum = 0;
        simulationDoctorWaitWalkInSum = 0;
        simulationSystemTimeSum = 0;

        simulationInitialWaitCount = 0;
        simulationDoctorWaitAmbulanceCount = 0;
        simulationDoctorWaitWalkInCount = 0;
        simulationSystemTimeCount = 0;
    }

    public synchronized void resetReplicationStatistics() {
        replicationArrivals = 0;
        replicationDepartures = 0;
        replicationAmbulanceArrivals = 0;
        replicationWalkInArrivals = 0;

        replicationInitialWaitSum = 0;
        replicationDoctorWaitAmbulanceSum = 0;
        replicationDoctorWaitWalkInSum = 0;
        replicationSystemTimeSum = 0;

        replicationInitialWaitCount = 0;
        replicationDoctorWaitAmbulanceCount = 0;
        replicationDoctorWaitWalkInCount = 0;
        replicationSystemTimeCount = 0;
    }

    private double avg(double sum, long count) {
        return count == 0 ? 0.0 : sum / count;
    }

    // simulation getters
    public synchronized long getSimulationArrivals() {
        return simulationArrivals;
    }

    public synchronized long getSimulationDepartures() {
        return simulationDepartures;
    }

    public synchronized long getSimulationAmbulanceArrivals() {
        return simulationAmbulanceArrivals;
    }

    public synchronized long getSimulationWalkInArrivals() {
        return simulationWalkInArrivals;
    }

    public synchronized double getSimulationAverageInitialExamWait() {
        return avg(simulationInitialWaitSum, simulationInitialWaitCount);
    }

    public synchronized double getSimulationAverageDoctorWaitAmbulance() {
        return avg(simulationDoctorWaitAmbulanceSum, simulationDoctorWaitAmbulanceCount);
    }

    public synchronized double getSimulationAverageDoctorWaitWalkIn() {
        return avg(simulationDoctorWaitWalkInSum, simulationDoctorWaitWalkInCount);
    }

    public synchronized double getSimulationAverageSystemTime() {
        return avg(simulationSystemTimeSum, simulationSystemTimeCount);
    }

    // replication getters
    public synchronized long getReplicationArrivals() {
        return replicationArrivals;
    }

    public synchronized long getReplicationDepartures() {
        return replicationDepartures;
    }

    public synchronized long getReplicationAmbulanceArrivals() {
        return replicationAmbulanceArrivals;
    }

    public synchronized long getReplicationWalkInArrivals() {
        return replicationWalkInArrivals;
    }

    public synchronized double getReplicationAverageInitialExamWait() {
        return avg(replicationInitialWaitSum, replicationInitialWaitCount);
    }

    public synchronized double getReplicationAverageDoctorWaitAmbulance() {
        return avg(replicationDoctorWaitAmbulanceSum, replicationDoctorWaitAmbulanceCount);
    }

    public synchronized double getReplicationAverageDoctorWaitWalkIn() {
        return avg(replicationDoctorWaitWalkInSum, replicationDoctorWaitWalkInCount);
    }

    public synchronized double getReplicationAverageSystemTime() {
        return avg(replicationSystemTimeSum, replicationSystemTimeCount);
    }

    public void setParameters(int doctorsCount, int nursesCount) {
        agentResources().setNumberOfPersonal(doctorsCount, nursesCount);
    }

    @Override
    public void prepareSimulation() {
        super.prepareSimulation();
        resetSimulationStatistics();
        resetReplicationStatistics();
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        resetReplicationStatistics();

        agentBoss().startSimulation();

        // Reset entities, queues, local statistics, etc...
    }

    @Override
    public void replicationFinished() {
        // Collect local statistics into global, update UI, etc...
        super.replicationFinished();
    }

    @Override
    public void simulationFinished() {
        // Display simulation results
        super.simulationFinished();
    }

    //meta! userInfo="Generated code: do not modify", tag="begin"
    private void init() {
        setAgentBoss(new AgentBoss(Id.agentBoss, this, null));
        setAgentEnvironment(new AgentEnvironment(Id.agentEnvironment, this, agentBoss()));
        setAgentHospital(new AgentHospital(Id.agentHospital, this, agentBoss()));
        setAgentResources(new AgentResources(Id.agentResources, this, agentHospital()));
        setAgentPatients(new AgentPatients(Id.agentPatients, this, agentHospital()));
        setAgentMovement(new AgentMovement(Id.agentMovement, this, agentHospital()));
    }

    private AgentBoss _agentBoss;

    public AgentBoss agentBoss() {
        return _agentBoss;
    }

    public void setAgentBoss(AgentBoss agentBoss) {
        _agentBoss = agentBoss;
    }

    private AgentEnvironment _agentEnvironment;

    public AgentEnvironment agentEnvironment() {
        return _agentEnvironment;
    }

    public void setAgentEnvironment(AgentEnvironment agentEnvironment) {
        _agentEnvironment = agentEnvironment;
    }

    private AgentHospital _agentHospital;

    public AgentHospital agentHospital() {
        return _agentHospital;
    }

    public void setAgentHospital(AgentHospital agentHospital) {
        _agentHospital = agentHospital;
    }

    private AgentResources _agentResources;

    public AgentResources agentResources() {
        return _agentResources;
    }

    public void setAgentResources(AgentResources agentResources) {
        _agentResources = agentResources;
    }

    private AgentPatients _agentPatients;

    public AgentPatients agentPatients() {
        return _agentPatients;
    }

    public void setAgentPatients(AgentPatients agentPatients) {
        _agentPatients = agentPatients;
    }

    private AgentMovement _agentMovement;

    public AgentMovement agentMovement() {
        return _agentMovement;
    }

    public void setAgentMovement(AgentMovement agentMovement) {
        _agentMovement = agentMovement;
    }
    //meta! tag="end"
}