///*
// * Knowage, Open Source Business Intelligence suite
// * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
// *
// * Knowage is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Affero General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * Knowage is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Affero General Public License for more details.
// *
// * You should have received a copy of the GNU Affero General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package it.eng.spagobi.commons.robobraillerconverter;
//
//import java.io.File;
//import java.util.Map;
//
//import org.apache.log4j.Logger;
//import org.jboss.resteasy.client.ClientResponse;
//
//import it.eng.spagobi.commons.ADconverter.AccessibleDocumentConverter;
//import it.eng.spagobi.commons.ADconverter.ConversionType;
//import it.eng.spagobi.commons.robobraillerconverter.jobmanager.Job;
//import it.eng.spagobi.commons.robobraillerconverter.jobmanager.JobManager;
//import it.eng.spagobi.commons.robobraillerconverter.jobmanager.JobState;
//import it.eng.spagobi.commons.robobraillerconverter.restclient.AbstractRoboBrailleController;
//import it.eng.spagobi.commons.robobraillerconverter.restclient.RoboBrailleControllerFactory;
//import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
//
//public class RoboBrailleConverter implements AccessibleDocumentConverter {
//	JobManager jobManager = null;
//	static protected Logger logger = Logger.getLogger(RoboBrailleConverter.class);
//	public RoboBrailleConverter(ConversionType conversionType) {
//
//		RoboBrailleControllerFactory rcFactory = new RoboBrailleControllerFactory();
//		jobManager = rcFactory.getJobManager(conversionType);
//	}
//
//	@Override
//	public String startConversion(byte[] file, Map<String, String> params) {
//		Job job = jobManager.createJob(file, params);
//		int counter = 0;
//
//
//		while(job.getJobState() == JobState.INPROGRESS){
//			try {
//				counter++;
//
//				jobManager.setJobStatus(job);
//				if(counter>100){
//					job.setJobState(JobState.ERROR);
//					logger.error("Timeout");
//					throw new SpagoBIRuntimeException("Timeout");
//				}
//				Thread.sleep(2000);
//			} catch (InterruptedException e) {
//				logger.error("Thread interupted",e);
//				throw new SpagoBIRuntimeException("Thread interupted",e);
//			}
//		}
//
//		if(job.getJobState() == JobState.ERROR){
//			logger.error("Job State is ERROR");
//			throw new SpagoBIRuntimeException("Error in coverting");
//
//		}
//
//			return job.getJobId();
//
//	}
//	@Override
//	public File getConversionResult(String jobId) {
//		Job job = new Job(jobId);
//		File filetemp =null;
//
//		filetemp = jobManager.getJobResult(job);
//
//		jobManager.deleteJob(job);
//
//		return filetemp;
//	}
//
//}
