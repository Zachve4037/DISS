package S3.simulation;

import S3.agents.agentpatients.*;
import S3.agents.agentresources.*;
import S3.agents.agentenvironment.*;
import S3.agents.agentboss.*;
import S3.agents.agenthospital.*;
import S3.agents.agentmovement.*;

public class MySimulation extends OSPABA.Simulation {
    public MySimulation() {
        init();
    }

    @Override
    public void prepareSimulation() {
        super.prepareSimulation();
        // Create global statistcis
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
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
