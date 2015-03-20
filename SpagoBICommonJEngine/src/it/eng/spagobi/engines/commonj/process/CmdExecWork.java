/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.commonj.process;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

public class CmdExecWork extends SpagoBIWork {

	String command;
	String commandEnvironment;

	/** parameters  passed to command*/
	Vector<String> cmdParameters;
	/** to be added to classpath*/
	Vector<String> classpathParameters;
	/** Logger */
	static private Logger logger = Logger.getLogger(CmdExecWork.class);
	/** the process aunche*/
	Process process = null;
	/** the flag for automatic instance ID (external creation) */
	static final String INSTANCE_AUTO="INSTANCE=AUTO";

	public boolean isDaemon() {
		return false;
	}

	public void release() {
		logger.debug("IN");
		super.release();
		if(process != null){
			process.destroy();
			logger.info("Release the JOB");
		}
		logger.debug("OUT");
	}




	/** this method executes command followed by command parameters taken from template
	 *  and by sbi parameters
	 *  and add classpath variables followed by -cp
	 * 
	 * @param cmd
	 * @param envFile
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */

	public int execCommand() throws InterruptedException, IOException {
		logger.debug("IN");
		File directoryExec = null;
		boolean isInstanceAuto = false;
		
		if(commandEnvironment != null) {
			directoryExec = new File(commandEnvironment);
			logger.info("commandEnvironment="+commandEnvironment);
		}
		// add -cp 
		if(classpathParameters.size()>0){
			command += " -cp ";
			for (Iterator iterator = classpathParameters.iterator(); iterator.hasNext();) {
				String add = (String) iterator.next();
				command += add;
				if(iterator.hasNext()){
					command += ";";
				}
			}
		}

		command += " ";

		// add command parameters
		for (Iterator iterator = cmdParameters.iterator(); iterator.hasNext();) {
			String par = (String) iterator.next();		
			command += par + " ";
			isInstanceAuto = (par.toUpperCase().indexOf(INSTANCE_AUTO) > -1)?true:false;
		}


		// select sbi driver from MAP and add values!
		for (Iterator iterator = analyticalParameters.iterator(); iterator.hasNext();) {
			String url= (String) iterator.next();
			if (sbiParameters.get(url) != null){
				Object value = sbiParameters.get(url);
				if (value!=null && !value.equals("") ) {
					command += url + "=" + value.toString() +" "; 
				}
			}
		}

		// add pid to command if it's non defined as 'AUTO' definition
		if (!isInstanceAuto){
			String pidStr = "instance="+pid;
			command += pidStr;
		}
		

    	if(isRunning()){
			logger.info("launch command "+command);
			process = Runtime.getRuntime().exec(command, null, directoryExec);
			logger.info("Wait for the end of the process... ");
			
			StreamGobbler errorGobbler = new 
            StreamGobbler(process.getErrorStream(), "ERROR");
			
			 StreamGobbler outputGobbler = new 
             StreamGobbler(process.getInputStream(), "OUTPUT");
			 
			 errorGobbler.start();
	         outputGobbler.start();
	         
	         int exitVal = process.waitFor();

/*
		 		BufferedReader input =
		 	 	new BufferedReader(new InputStreamReader(process.getInputStream()));
		 	 	while (( input.readLine()) != null) {
		 	 		
		 	 	}
		 	 	
		 	 	input.close();
		 	 	process.waitFor();
		 	 	*/
		 	 	
			logger.info("Process END "+command);
		}
		else{
			logger.warn("Command not launched cause work has been stopper");
		}

		logger.debug("OUT");
		return 0;

	}


	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getCommandEnvironment() {
		return commandEnvironment;
	}

	public void setCommandEnvironment(String commandEnvironment) {
		this.commandEnvironment = commandEnvironment;
	}

	public Vector<String> getCmdParameters() {
		return cmdParameters;
	}

	public void setCmdParameters(Vector<String> cmdParameters) {
		this.cmdParameters = cmdParameters;
	}

	public Vector<String> getClasspathParameters() {
		return classpathParameters;
	}

	public void setClasspathParameters(Vector<String> classpathParameters) {
		this.classpathParameters = classpathParameters;
	}


	class StreamGobbler extends Thread
	{
	    InputStream is;
	    String type;
	    
	    StreamGobbler(InputStream is, String type)
	    {
	        this.is = is;
	        this.type = type;
	    }
	    
	    public void run()
	    {
	        try
	        {
	            InputStreamReader isr = new InputStreamReader(is);
	            BufferedReader br = new BufferedReader(isr);
	            String line=null;
	            while ( (line = br.readLine()) != null)
	                System.out.println(type + ">" + line);    
	            } catch (IOException ioe)
	              {
	            	logger.error(ioe);
	              }
	    }
	}

}
