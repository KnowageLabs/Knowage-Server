/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.engines.talend.client;

import it.eng.spagobi.engines.talend.client.exception.AuthenticationFailedException;
import it.eng.spagobi.engines.talend.client.exception.EngineUnavailableException;
import it.eng.spagobi.engines.talend.client.exception.ServiceInvocationFailedException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * @author Andrea Gioia
 *
 */
class SpagoBITalendEngineClient_0_5_0 implements ISpagoBITalendEngineClient {
	String host;
	String port;
	String appContext;
			
	private static final String JOB_UPLOAD_SERVICE = "JobUploadService";
	private static final String ENGINE_INFO_SERVICE = "EngineInfoService";
	
	private String getServiceUrl(String serviceName) {
		return ("http://" + host + ":" + port + "/" + appContext + "/" + serviceName);
	}
	
	
	/**
	 * Instantiates a new spago bi talend engine client_0_5_0.
	 * 
	 * @param usr the usr
	 * @param pwd the pwd
	 * @param host the host
	 * @param port the port
	 * @param appContext the app context
	 */
	public SpagoBITalendEngineClient_0_5_0(String usr, String pwd, String host,String port, String appContext) { 
		this.host = host;
		this.port = port;
		this.appContext = appContext;
	} 

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.talend.client.ISpagoBITalendEngineClient#getEngineVersion()
	 */
	public String getEngineVersion() 
	throws EngineUnavailableException, ServiceInvocationFailedException {
		return getEngineInfo("version");
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.talend.client.ISpagoBITalendEngineClient#getEngineName()
	 */
	public String getEngineName() 
	throws EngineUnavailableException, ServiceInvocationFailedException {
		return getEngineInfo("name");
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.talend.client.ISpagoBITalendEngineClient#isEngineAvailible()
	 */
	public boolean isEngineAvailible()  {
		
		try {
			getEngineInfo("version");
		} catch (EngineUnavailableException e) {
			return false;
		} catch (ServiceInvocationFailedException e) {
			return false;
		}
		
		return true;		
	}
	
	private String getEngineInfo(String infoType) 
	throws EngineUnavailableException, ServiceInvocationFailedException {
		String version;
		HttpClient client;
		PostMethod method;
		NameValuePair[] nameValuePairs;
		
		version = null;
		client = new HttpClient();
		method = new PostMethod(getServiceUrl(ENGINE_INFO_SERVICE));
		
		// Provide custom retry handler is necessary
	    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
	    		new DefaultHttpMethodRetryHandler(3, false));

	    nameValuePairs   = new NameValuePair[] {
	    		new NameValuePair("infoType", infoType)
	    };
       
        method.setRequestBody( nameValuePairs );
	    
	    try {
	      // Execute the method.
	      int statusCode = client.executeMethod(method);

	      if (statusCode != HttpStatus.SC_OK) {  
	    	  throw new ServiceInvocationFailedException("Service '" + ENGINE_INFO_SERVICE + "' execution failed", 
		        		method.getStatusLine().toString(),
		        		method.getResponseBodyAsString());
	      } else {
	    	  version = method.getResponseBodyAsString();
	      }      

	    } catch (HttpException e) {
	    	throw new EngineUnavailableException("Fatal protocol violation: " + e.getMessage());	
	    } catch (IOException e) {
	    	throw new EngineUnavailableException("Fatal transport error: " + e.getMessage());
	    } finally {
	      // Release the connection.
	      method.releaseConnection();
	    }  	
		
		return version;
	}
		
	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.talend.client.ISpagoBITalendEngineClient#deployJob(it.eng.spagobi.engines.talend.client.JobDeploymentDescriptor, java.io.File)
	 */
	public boolean deployJob(JobDeploymentDescriptor jobDeploymentDescriptor, File executableJobFiles)  
	throws EngineUnavailableException, AuthenticationFailedException, ServiceInvocationFailedException {
		
		HttpClient client;
		PostMethod method;	
		File deploymentDescriptorFile;
		boolean result = false;
		
		client = new HttpClient();
		method = new PostMethod(getServiceUrl(JOB_UPLOAD_SERVICE));
		deploymentDescriptorFile = null;
		
		try {			
			deploymentDescriptorFile = File.createTempFile("deploymentDescriptor", ".xml");		
			FileWriter writer = new FileWriter(deploymentDescriptorFile);
			writer.write(jobDeploymentDescriptor.toXml());
			writer.flush();
			writer.close();
					
				        
			Part[] parts = {
					new FilePart(executableJobFiles.getName(), executableJobFiles),
	                new FilePart("deploymentDescriptor", deploymentDescriptorFile)
	        };
			
			method.setRequestEntity(
					new MultipartRequestEntity(parts, method.getParams())
	        );
			
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			
        	int status = client.executeMethod(method);
            if (status == HttpStatus.SC_OK) {
                if(method.getResponseBodyAsString().equalsIgnoreCase("OK")) result = true;
            }  else {
            	throw new ServiceInvocationFailedException("Service '" + JOB_UPLOAD_SERVICE + "' execution failed", 
 		        		method.getStatusLine().toString(),
 		        		method.getResponseBodyAsString());
            }
		} catch (HttpException e) {
	    	throw new EngineUnavailableException("Fatal protocol violation: " + e.getMessage());	
	    } catch (IOException e) {
	    	throw new EngineUnavailableException("Fatal transport error: " + e.getMessage());
	    } finally {
            method.releaseConnection();
            if(deploymentDescriptorFile != null) deploymentDescriptorFile.delete();
        }
        
        return result;
	}	
}
