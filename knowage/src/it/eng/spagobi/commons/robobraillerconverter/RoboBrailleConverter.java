package it.eng.spagobi.commons.robobraillerconverter;

import java.io.File;
import java.util.Map;

import org.jboss.resteasy.client.ClientResponse;

import it.eng.spagobi.commons.ADconverter.AccessibleDocumentConverter;
import it.eng.spagobi.commons.ADconverter.ConversionType;
import it.eng.spagobi.commons.robobraillerconverter.jobmanager.Job;
import it.eng.spagobi.commons.robobraillerconverter.jobmanager.JobManager;
import it.eng.spagobi.commons.robobraillerconverter.jobmanager.JobState;
import it.eng.spagobi.commons.robobraillerconverter.restclient.RoboBrailleControllerFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class RoboBrailleConverter implements AccessibleDocumentConverter {
	JobManager jobManager = null;
	
	public RoboBrailleConverter(ConversionType conversionType) {
		
		RoboBrailleControllerFactory rcFactory = new RoboBrailleControllerFactory();
		jobManager = rcFactory.getJobManager(conversionType);
	}
	
	@Override
	public String startConversion(byte[] file, Map<String, String> params) {
		Job job = jobManager.createJob(file, params);
		int counter = 0;
		
		
		while(job.getJobState() == JobState.INPROGRESS){
			try {
				counter++;
				
				jobManager.setJobStatus(job);
				if(counter>15){
					job.setJobState(JobState.ERROR);
				}
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				throw new SpagoBIRuntimeException("Thread interupted",e);
			}
		}
		
		if(job.getJobState() == JobState.ERROR){
			
			throw new SpagoBIRuntimeException("Error in coverting");
			
		}
			
			return job.getJobId();
		
	}
	@Override
	public File getConversionResult(String jobId) {
		Job job = new Job(jobId);
		File filetemp =null;
		
		filetemp = jobManager.getJobResult(job);
		
		jobManager.deleteJob(job);
		
		return filetemp;
	}

}
