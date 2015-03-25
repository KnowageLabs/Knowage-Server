/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.deserializer;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JobXMLDeserializer implements Deserializer {
	
	public static String JOB_NAME = "jobName";
	public static String JOB_GROUP = "jobGroupName";
	public static String JOB_DESCRIPTION = "jobDescription";
	public static String JOB_CLASS = "jobClass";
	public static String JOB_REQUEST_RECOVERY = "jobRequestRecovery";
	public static String JOB_PARAMETERS = "PARAMETERS";
	
	
	

	private static Logger logger = Logger.getLogger(JobXMLDeserializer.class);
	  
	public Object deserialize(Object o, Class clazz) throws DeserializationException {
		
		Job job;
		String jobName;
		String jobGroupName;
		String jobDescription;
		boolean jobRequestRecovery;
		Map<String, String> jobParameters;
		Class jobClass;
		
		
		logger.debug("IN");
		
		job = new Job();		
		
		try {
			Assert.assertNotNull(o, "Input parameter [" + o + "] cannot be null");
			Assert.assertNotNull(o, "Input parameter [" + clazz + "] cannot be null");
			
			SourceBean xml = null;
			if(o instanceof SourceBean) {
				xml = (SourceBean)o;
			} else if (o instanceof String) {
				xml = SourceBean.fromXMLString( (String)o);
			} else {
				throw new DeserializationException("Impossible to deserialize from an object of type [" + o.getClass().getName() +"]");
			}
			
			jobName = (String)xml.getAttribute( JOB_NAME );
			jobGroupName = (String)xml.getAttribute( JOB_GROUP );
			jobDescription = (String)xml.getAttribute( JOB_DESCRIPTION );
			jobRequestRecovery = deserializeRequestRecoveryAttribute(xml);
			jobParameters = deserializeParametersAttribute(xml);
			jobClass = deserializeClassAttribute(xml);
					
			job.setName(jobName);
			job.setGroupName(jobGroupName);
			job.setDescription(jobDescription);
			job.setRequestsRecovery(jobRequestRecovery);
			job.addParameters(jobParameters);
			job.setJobClass(jobClass);
		} catch (Throwable t) {
			throw new DeserializationException("An error occurred while deserializing object: " + o, t);
		} finally {
			logger.debug("OUT");
		}
		
		return job;
	}
	
	private boolean deserializeRequestRecoveryAttribute(SourceBean xml) {
		boolean jobRequestRecovery;
		
		jobRequestRecovery = false;
		
		String jobRequestRecoveryStr = (String)xml.getAttribute( JOB_REQUEST_RECOVERY );
		if((jobRequestRecoveryStr!=null) && (jobRequestRecoveryStr.trim().equalsIgnoreCase("true"))) {
			jobRequestRecovery = true;
		}
		
		return jobRequestRecovery;
	}
	
	private Class deserializeClassAttribute(SourceBean xml) {
		Class jobClass;
		
		String jobClassName = (String)xml.getAttribute( JOB_CLASS );
		
		if(StringUtilities.isEmpty(jobClassName)) return null;
		
		jobClass = null;
		try {
			jobClass = Class.forName(jobClassName);
		} catch (ClassNotFoundException e) {
			throw new SpagoBIRuntimeException("Impossible to resolve job class [" + jobClassName + "]", e);
		}
		
		return jobClass;
	}
	
	private  Map<String, String> deserializeParametersAttribute(SourceBean xml) {
		Map<String, String> parameters = new HashMap<String, String>();
		
		SourceBean jobParameters = (SourceBean)xml.getAttribute( JOB_PARAMETERS );
		
		parameters.put("empty", "empty");
		if(jobParameters != null) {
			List paramsSB = jobParameters.getContainedAttributes();
			Iterator iterParSb = paramsSB.iterator();
			while(iterParSb.hasNext()) {
					SourceBeanAttribute paramSBA = (SourceBeanAttribute)iterParSb.next();
					String nameAttr = (String)paramSBA.getKey();
					if(nameAttr.equalsIgnoreCase("PARAMETER")) {
						SourceBean paramSB = (SourceBean)paramSBA.getValue();
						String name = (String)paramSB.getAttribute("name");
						String value = (String)paramSB.getAttribute("value");
						parameters.put(name, value);
					}
			}
		}
		return parameters;
	}

}
