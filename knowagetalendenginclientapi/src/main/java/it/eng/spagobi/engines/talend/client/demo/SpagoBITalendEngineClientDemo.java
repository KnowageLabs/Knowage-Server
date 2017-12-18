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
package it.eng.spagobi.engines.talend.client.demo;

import it.eng.spagobi.engines.talend.client.ISpagoBITalendEngineClient;
import it.eng.spagobi.engines.talend.client.JobDeploymentDescriptor;
import it.eng.spagobi.engines.talend.client.SpagoBITalendEngineClient;
import it.eng.spagobi.engines.talend.client.exception.AuthenticationFailedException;
import it.eng.spagobi.engines.talend.client.exception.EngineUnavailableException;
import it.eng.spagobi.engines.talend.client.exception.ServiceInvocationFailedException;
import it.eng.spagobi.engines.talend.client.exception.UnsupportedEngineVersionException;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

/**
 * @author Andrea Gioia
 *
 */
public class SpagoBITalendEngineClientDemo {

	private static void usage() {
		System.out.println("cmdName usr pwd host port context file");		
	}
	
	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * 
	 * @throws ZipException the zip exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws ZipException, IOException {		
		
		if(args.length < 6) {
			usage();
			System.exit(1);
		}
		
		String user = args[0];
		String password = args[1];
		String host = args[2];
		String port = args[3];
		String applicationContext = args[4];
		String deploymentFile = args[5];
		
		
		try {		
			
			// create the client
			ISpagoBITalendEngineClient client 
				= new SpagoBITalendEngineClient(user, password, host, port, applicationContext);
			
			// get some informations about the engine instance referenced by the client
			System.out.println("Engine version: " + client.getEngineVersion());
			System.out.println("Engine fullname: " + client.getEngineName());
				
			// prepare parameters used during deployment
			JobDeploymentDescriptor jobDeploymentDescriptor = new JobDeploymentDescriptor("SpagoBITalendTest", "perl");
			File zipFile = new File(deploymentFile);
				
			// deploy job on engine runtime
			boolean result = client.deployJob(jobDeploymentDescriptor, zipFile);
			if(result) System.out.println("Jobs deployed succesfully");
			else System.out.println("Jobs not deployed");
				
		
		} catch (EngineUnavailableException e) {
			System.err.println("ERRORE: " + e.getMessage());
		} catch(AuthenticationFailedException e) {
			System.err.println("ERRORE: " + e.getMessage());
		} catch (UnsupportedEngineVersionException e) {
			System.err.println("ERROR: Unsupported engine version");	
			System.err.println("You are using TalendEngineClientAPI version " 
					+ SpagoBITalendEngineClient.CLIENTAPI_VERSION_NUMBER + ". "
					+ "The TalendEngine instance you are trying to connect to require TalendEngineClientAPI version "
					+ e.getComplianceVersion() + " or grater.");
		} catch (ServiceInvocationFailedException e) {
			System.err.println("ERRORE: " + e.getMessage());
			System.err.println("StatusLine: " + e.getStatusLine()
							   + "\nresponseBody: " + e.getResponseBody());
		} 
	}

	
}
