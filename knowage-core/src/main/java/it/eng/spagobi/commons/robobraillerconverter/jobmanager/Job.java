package it.eng.spagobi.commons.robobraillerconverter.jobmanager;

public class Job {
	public JobState getJobState() {
		return jobState;
	}

	public void setJobState(JobState jobState) {
		this.jobState = jobState;
	}

	private String jobId = null;
	private JobState jobState = null;

	public String getJobId() {
		return jobId;
	}

	public Job(String jobId) {
		this.jobId = jobId;
		this.jobState = JobState.INPROGRESS;
	}
	
	
}
