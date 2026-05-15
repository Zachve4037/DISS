package S3.simulation;

import S2.Statistics.ArithmeticalStatistic;
import S2.Statistics.ConfidenceIntervalStatistic;
import S3.animation.EmergencyDepartmentAnimator;
import S3.agents.agentboss.AgentBoss;
import S3.agents.agentenvironment.AgentEnvironment;
import S3.agents.agenthospital.AgentHospital;
import S3.agents.agentmovement.AgentMovement;
import S3.agents.agentpatients.AgentPatients;
import S3.agents.agentresources.AgentResources;
import S3.entities.Patient;
import S3.entities.PatientType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EmergencyDepartmentSimulation extends OSPABA.Simulation {
    private final EmergencyDepartmentAnimator edAnimator = new EmergencyDepartmentAnimator(this);
    private final Map<Integer, Patient> currentPatients = new ConcurrentHashMap<>();

    public EmergencyDepartmentAnimator edAnimator() {
        return edAnimator;
    }

    private boolean ambulanceCleanupBlockEnabled;
    private boolean reserveInitialClinicsEnabled;
    private boolean reservePriority12ClinicEnabled;
    private boolean dynamicInitialOverflowEnabled;
    private int dynamicInitialOverflowThreshold = 10;
    private int warmupDays = 0;
    private double warmupEndTime = 0.0;

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

    private ArithmeticalStatistic simulationArrivalsMean;
    private ArithmeticalStatistic simulationDeparturesMean;
    private ArithmeticalStatistic simulationAmbulanceArrivalsMean;
    private ArithmeticalStatistic simulationWalkInArrivalsMean;
    private ArithmeticalStatistic simulationInitialWaitMean;
    private ArithmeticalStatistic simulationDoctorWaitAmbulanceMean;
    private ArithmeticalStatistic simulationDoctorWaitWalkInMean;
    private ArithmeticalStatistic simulationSystemTimeMean;

    private ConfidenceIntervalStatistic simulationArrivalsCi;
    private ConfidenceIntervalStatistic simulationDeparturesCi;
    private ConfidenceIntervalStatistic simulationAmbulanceArrivalsCi;
    private ConfidenceIntervalStatistic simulationWalkInArrivalsCi;
    private ConfidenceIntervalStatistic simulationInitialWaitCi;
    private ConfidenceIntervalStatistic simulationDoctorWaitAmbulanceCi;
    private ConfidenceIntervalStatistic simulationDoctorWaitWalkInCi;
    private ConfidenceIntervalStatistic simulationSystemTimeCi;

    private List<Double> doctorWaitAmbulanceConvergence;
    private List<Double> doctorWaitWalkInConvergence;

    public EmergencyDepartmentSimulation() {
        init();
        resetSimulationStatistics();
        resetReplicationStatistics();
    }

    public void recordArrival(Patient patient) {
        if (patient == null) {
            return;
        }

        currentPatients.put(patient.getId(), patient);

        if (!isStatisticsEligible(patient)) {
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

    public void recordDeparture(Patient patient) {
        if (patient == null) {
            return;
        }

        currentPatients.remove(patient.getId());

        if (!isStatisticsEligible(patient)) {
            return;
        }

        double initialWait = patient.getInitialExamWaitTime();
        double doctorWait = patient.getDoctorsExamWaitTime();
        double systemTime = patient.getTotalTimeInSystem();

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

    public void resetSimulationStatistics() {
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

        simulationArrivalsMean = new ArithmeticalStatistic();
        simulationDeparturesMean = new ArithmeticalStatistic();
        simulationAmbulanceArrivalsMean = new ArithmeticalStatistic();
        simulationWalkInArrivalsMean = new ArithmeticalStatistic();
        simulationInitialWaitMean = new ArithmeticalStatistic();
        simulationDoctorWaitAmbulanceMean = new ArithmeticalStatistic();
        simulationDoctorWaitWalkInMean = new ArithmeticalStatistic();
        simulationSystemTimeMean = new ArithmeticalStatistic();

        simulationArrivalsCi = new ConfidenceIntervalStatistic();
        simulationDeparturesCi = new ConfidenceIntervalStatistic();
        simulationAmbulanceArrivalsCi = new ConfidenceIntervalStatistic();
        simulationWalkInArrivalsCi = new ConfidenceIntervalStatistic();
        simulationInitialWaitCi = new ConfidenceIntervalStatistic();
        simulationDoctorWaitAmbulanceCi = new ConfidenceIntervalStatistic();
        simulationDoctorWaitWalkInCi = new ConfidenceIntervalStatistic();
        simulationSystemTimeCi = new ConfidenceIntervalStatistic();

        doctorWaitAmbulanceConvergence = new ArrayList<>();
        doctorWaitWalkInConvergence = new ArrayList<>();
    }

    public void resetReplicationStatistics() {
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

    private boolean isStatisticsEligible(Patient patient) {
        return patient.getArrivalTime() >= warmupEndTime;
    }

    private double avg(double sum, long count) {
        return count == 0 ? 0.0 : sum / count;
    }

    public double getSimulationMeanArrivalsPerReplication() {
        return simulationArrivalsMean.getValue();
    }

    public double getSimulationArrivalsCiHalfWidth() {
        return simulationArrivalsCi.getValue();
    }

    public double getSimulationMeanDeparturesPerReplication() {
        return simulationDeparturesMean.getValue();
    }

    public double getSimulationDeparturesCiHalfWidth() {
        return simulationDeparturesCi.getValue();
    }

    public double getSimulationMeanAmbulanceArrivalsPerReplication() {
        return simulationAmbulanceArrivalsMean.getValue();
    }

    public double getSimulationAmbulanceArrivalsCiHalfWidth() {
        return simulationAmbulanceArrivalsCi.getValue();
    }

    public double getSimulationMeanWalkInArrivalsPerReplication() {
        return simulationWalkInArrivalsMean.getValue();
    }

    public double getSimulationWalkInArrivalsCiHalfWidth() {
        return simulationWalkInArrivalsCi.getValue();
    }

    public double getSimulationMeanInitialExamWait() {
        return simulationInitialWaitMean.getValue();
    }

    public double getSimulationInitialExamWaitCiHalfWidth() {
        return simulationInitialWaitCi.getValue();
    }

    public double getSimulationMeanDoctorWaitAmbulance() {
        return simulationDoctorWaitAmbulanceMean.getValue();
    }

    public double getSimulationDoctorWaitAmbulanceCiHalfWidth() {
        return simulationDoctorWaitAmbulanceCi.getValue();
    }

    public double getSimulationMeanDoctorWaitWalkIn() {
        return simulationDoctorWaitWalkInMean.getValue();
    }

    public double getSimulationDoctorWaitWalkInCiHalfWidth() {
        return simulationDoctorWaitWalkInCi.getValue();
    }

    public double getSimulationMeanSystemTime() {
        return simulationSystemTimeMean.getValue();
    }

    public double getSimulationSystemTimeCiHalfWidth() {
        return simulationSystemTimeCi.getValue();
    }

    public List<Double> getDoctorWaitAmbulanceConvergence() {
        return List.copyOf(doctorWaitAmbulanceConvergence);
    }

    public List<Double> getDoctorWaitWalkInConvergence() {
        return List.copyOf(doctorWaitWalkInConvergence);
    }

    public List<Patient> getCurrentPatientsSnapshot() {
        return currentPatients.values().stream()
                .sorted(Comparator.comparingDouble(Patient::getArrivalTime)
                        .thenComparingInt(Patient::getId))
                .toList();
    }

    public long getReplicationArrivals() {
        return replicationArrivals;
    }

    public long getReplicationDepartures() {
        return replicationDepartures;
    }

    public long getReplicationAmbulanceArrivals() {
        return replicationAmbulanceArrivals;
    }

    public long getReplicationWalkInArrivals() {
        return replicationWalkInArrivals;
    }

    public double getReplicationAverageInitialExamWait() {
        return avg(replicationInitialWaitSum, replicationInitialWaitCount);
    }

    public double getReplicationAverageDoctorWaitAmbulance() {
        return avg(replicationDoctorWaitAmbulanceSum, replicationDoctorWaitAmbulanceCount);
    }

    public double getReplicationAverageDoctorWaitWalkIn() {
        return avg(replicationDoctorWaitWalkInSum, replicationDoctorWaitWalkInCount);
    }

    public double getReplicationAverageSystemTime() {
        return avg(replicationSystemTimeSum, replicationSystemTimeCount);
    }

    public void setParameters(int doctorsCount, int nursesCount) {
        agentResources().setNumberOfPersonal(doctorsCount, nursesCount);
    }

    public void setWarmupDays(int warmupDays) {
        this.warmupDays = Math.max(0, warmupDays);
        this.warmupEndTime = this.warmupDays * 24.0 * 3600.0;
    }

    public void setExperimentSettings(
            boolean ambulanceCleanupBlockEnabled,
            boolean reserveInitialClinicsEnabled,
            boolean reservePriority12ClinicEnabled,
            boolean dynamicInitialOverflowEnabled,
            int dynamicInitialOverflowThreshold
    ) {
        this.ambulanceCleanupBlockEnabled = ambulanceCleanupBlockEnabled;
        this.reserveInitialClinicsEnabled = reserveInitialClinicsEnabled;
        this.reservePriority12ClinicEnabled = reservePriority12ClinicEnabled;
        this.dynamicInitialOverflowEnabled = dynamicInitialOverflowEnabled;
        this.dynamicInitialOverflowThreshold = Math.max(1, dynamicInitialOverflowThreshold);
    }

    public boolean isAmbulanceCleanupBlockEnabled() {
        return ambulanceCleanupBlockEnabled;
    }

    public boolean isReserveInitialClinicsEnabled() {
        return reserveInitialClinicsEnabled;
    }

    public boolean isReservePriority12ClinicEnabled() {
        return reservePriority12ClinicEnabled;
    }

    public boolean isDynamicInitialOverflowEnabled() {
        return dynamicInitialOverflowEnabled;
    }

    public int getDynamicInitialOverflowThreshold() {
        return dynamicInitialOverflowThreshold;
    }

    public int getDoctorsCount() {
        return agentResources().getDoctors().size();
    }

    public int getNursesCount() {
        return agentResources().getNurses().size();
    }

    @Override
    public void prepareSimulation() {
        super.prepareSimulation();
        warmupEndTime = warmupDays * 24.0 * 3600.0;
        currentPatients.clear();
        resetSimulationStatistics();
        resetReplicationStatistics();
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        currentPatients.clear();
        resetReplicationStatistics();
        edAnimator.prepareReplication();

        agentBoss().startSimulation();

    }

    @Override
    public void replicationFinished() {
        collectReplicationStatistics();
        super.replicationFinished();
    }

    @Override
    public void simulationFinished() {
        super.simulationFinished();
    }

    private void collectReplicationStatistics() {
        addReplicationValue(simulationArrivalsMean, simulationArrivalsCi, replicationArrivals);
        addReplicationValue(simulationDeparturesMean, simulationDeparturesCi, replicationDepartures);
        addReplicationValue(simulationAmbulanceArrivalsMean, simulationAmbulanceArrivalsCi, replicationAmbulanceArrivals);
        addReplicationValue(simulationWalkInArrivalsMean, simulationWalkInArrivalsCi, replicationWalkInArrivals);
        addReplicationValue(simulationInitialWaitMean, simulationInitialWaitCi, getReplicationAverageInitialExamWait());
        addReplicationValue(simulationDoctorWaitAmbulanceMean, simulationDoctorWaitAmbulanceCi, getReplicationAverageDoctorWaitAmbulance());
        addReplicationValue(simulationDoctorWaitWalkInMean, simulationDoctorWaitWalkInCi, getReplicationAverageDoctorWaitWalkIn());
        addReplicationValue(simulationSystemTimeMean, simulationSystemTimeCi, getReplicationAverageSystemTime());
        doctorWaitAmbulanceConvergence.add(simulationDoctorWaitAmbulanceMean.getValue());
        doctorWaitWalkInConvergence.add(simulationDoctorWaitWalkInMean.getValue());
    }

    private void addReplicationValue(ArithmeticalStatistic mean, ConfidenceIntervalStatistic ci, double value) {
        mean.addValue(value, 1);
        ci.addValue(value, 1);
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
