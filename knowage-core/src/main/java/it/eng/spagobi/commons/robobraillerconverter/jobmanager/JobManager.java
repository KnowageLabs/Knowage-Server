package it.eng.spagobi.commons.robobraillerconverter.jobmanager;



import java.io.File;
import java.util.Map;

public interface JobManager {
	
	public Job createJob(byte[] file,Map<String, String> params);
	public void setJobStatus(Job job);
	public File getJobResult(Job job);
	public void deleteJob(Job job);
}
