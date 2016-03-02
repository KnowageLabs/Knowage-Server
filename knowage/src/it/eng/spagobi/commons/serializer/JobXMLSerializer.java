/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.commons.serializer;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.services.scheduler.service.XSchedulerServiceSupplier;
import it.eng.spagobi.tools.scheduler.bo.Job;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JobXMLSerializer implements Serializer {
	
	Properties properties;
	
	public static final String PROPERTY_CONSUMER = "consumer";
	
	public static final String JOB_NAME = "jobName";
	public static final String JOB_GROUP = "jobGroupName";
	public static final String JOB_DESCRIPTION = "jobDescription";	
	public static final String JOB_CLASS = "jobClass";
	public static final String JOB_DURABILITY = "jobDurability";
	public static final String JOB_REQUEST_RECOVERY = "jobRequestRecovery";	
	public static final String USE_VOLATILITY = "jobVolatility";
	
	static private Logger logger = Logger.getLogger(JobXMLSerializer.class);
	
	public JobXMLSerializer() {
		properties = new Properties();
	}
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		StringBuffer  result = null;
		
		if( !(o instanceof Job) ) {
			throw new SerializationException("JobXMLSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			Job job = (Job)o;
			result = new StringBuffer();
			
			String jobName = job.getName();
			String jobGroupName = job.getGroupName();
			String jobDescription = job.getDescription();
			String jobClassName = job.getJobClass().getName();
			String jobDurability = job.isDurable() ? "true" : "false";
			String jobRequestRecovery = job.isRequestsRecovery() ? "true" : "false";
			String jobVolatility = job.isVolatile() ? "true" : "false";
			Map<String, String>  parameters = job.getParameters();
			
			boolean isForServiceConsumer = "service".equalsIgnoreCase( properties.getProperty( PROPERTY_CONSUMER ) );
			
			String rootTag = "ROW";
			if( isForServiceConsumer ) rootTag = "JOB_DETAIL";
			
			result.append("<" + rootTag + " ");
			result.append(" " + JOB_NAME + "=\"" + (jobName != null ? jobName : "") + "\"");
			result.append(" " + JOB_GROUP + "=\"" + (jobGroupName != null ? jobGroupName : "") + "\"");
			result.append(" " + JOB_DESCRIPTION + "=\"" + (jobDescription != null ? jobDescription : "") + "\"");
			result.append(" " + JOB_CLASS + "=\"" + (jobClassName != null ? jobClassName : "") + "\"");
			result.append(" " + JOB_DURABILITY + "=\"" + jobDurability + "\"");
			result.append(" " + JOB_REQUEST_RECOVERY + "=\"" + jobRequestRecovery + "\"");
			result.append(" " + USE_VOLATILITY + "=\"" + jobVolatility + "\"");
			if( isForServiceConsumer ) {
				result.append(">");
				result.append("<JOB_PARAMETERS>");
				
				Set<String> keys = parameters.keySet();
				for (String key : keys) {
					result.append("<JOB_PARAMETER ");
				
					String value = parameters.get(key);
					if (value == null) {
						logger.warn("Job parameter [" + key + "] has no value");
						continue;
					}
					result.append(" name=\"" + key + "\"");
					result.append(" value=\"" + value + "\"");
					result.append(" />");
				}
								
				result.append("</JOB_PARAMETERS>");
				result.append("</JOB_DETAIL>");
			} else {
				result.append(" />");
			}
			
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result.toString();
	}
	
	public void setProperty(String name, String value) {
		properties.setProperty(name, value);
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	
}
