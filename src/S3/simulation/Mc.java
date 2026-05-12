package S3.simulation;

public class Mc extends OSPABA.IdList
{
	//meta! userInfo="Generated code: do not modify", tag="begin"
	public static final int allocateInitialExaminationResources = 1015;
	public static final int allocateDoctorsExaminationAResources = 1016;
	public static final int allocateDoctorsExaminationBResources = 1017;
	public static final int handleDoctorsMovement = 1018;
	public static final int handleNursesMovement = 1019;
	public static final int handlePatientToWaitingRoom = 1020;
	public static final int handleLeavingPatient = 1021;
	public static final int handlePatientToClinic = 1022;
	public static final int getPatientForInitialExamination = 1023;
	public static final int getPatientForDoctorsExamination = 1024;
	public static final int putPatient = 1025;
	public static final int patientArrival = 1002;
	public static final int patientDeparture = 1010;
	public static final int handlePatient = 1011;
	public static final int handleInitialPersonalMovement = 1028;
	//meta! tag="end"
	public static final int releaseResources = 1;
	public static final int tryStartInitialExamination = 2;
	public static final int tryStartDoctorsExamination = 3;

	// 1..1000 range reserved for user
}