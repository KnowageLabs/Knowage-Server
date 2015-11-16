/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.commonj.runtime;


import it.eng.spago.base.SourceBean;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

/**
 * contiene le configurazioni del WORK quando le legge da template
 * @author bernabei
 *
 */
public class CommonjWork {

	String pId;
	String workName;
	String className;
	String command;
	String command_environment;

	/** parameters set in template*/
	Vector<String> cmdParameters;
	Vector<String> analyticalParameters;
	Vector<String> classpathParameters;

	/** map of analyticalDriver from SpagoBIDocument*/
	Map sbiParametersMap;

	static final String COMMAND="cmd";
	static final String COMMAND_ENVIRONMENT="cmd_env";
	static final String SBI_ANALYTICAL_DRIVER="sbi_Analytical_Driver";
	static final String CMD_PAR="CMD_PAR";
	static final String CLASSPATH="classpath";

	private static transient Logger logger = Logger.getLogger(CommonjWork.class);

	public CommonjWork(SourceBean template) throws SpagoBIEngineException {
		logger.debug("IN");
		this.load(template);
		logger.debug("OUT");
	}


	/**
	 * Instantiates a new work.
	 * 
	 * @param name the name
	 * @param className the className
	 */
	public CommonjWork(String name, String className) {
		this.workName = name;
		this.className= className;
	}		

	public void load(SourceBean template) throws it.eng.spagobi.engines.commonj.exception.TemplateParseException {
		logger.debug("IN");
		SourceBean workSB;

		Assert.assertNotNull(template, "Input parameter [template] cannot be null");

		workSB = (SourceBean)template.getAttribute("WORK");
		Assert.assertNotNull(workSB, "template cannot be null");

		workName = (String)workSB.getAttribute("workName");
		if(workName == null) {
			logger.error("Missing  work name in document template");
			throw new it.eng.spagobi.engines.commonj.exception.TemplateParseException(template, "Missing  work name in document template");
		}


		className = (String)workSB.getAttribute("className");
		if(className == null) {
			logger.error("Missing class specification in document template");
			throw new it.eng.spagobi.engines.commonj.exception.CommonjEngineException("Missing class specification in document template");
		}

		cmdParameters= new Vector<String>();
		analyticalParameters=new Vector<String>();
		classpathParameters=new Vector<String>();

		// check for parameters, in particular cmd and cmd_env
		SourceBean parametersSB=(SourceBean)workSB.getAttribute("PARAMETERS");
		if(parametersSB!=null){
			List parameterList=parametersSB.getAttributeAsList("PARAMETER");
			if(parameterList!=null){
				for (Iterator iterator = parameterList.iterator(); iterator.hasNext();) {
					SourceBean parameter = (SourceBean) iterator.next();
					String name=(String)parameter.getAttribute("name");
					String value=(String)parameter.getAttribute("value");

					// if it is the command name
					if(name.equalsIgnoreCase(COMMAND)){
						logger.debug("command parameter "+value);
						command=value;				
					}
					else // if it is the command environment
						if(name.equalsIgnoreCase(COMMAND_ENVIRONMENT)){
							logger.debug("command environment parameter"+value);
							command_environment=value;	
						}
						else{
							logger.debug("general parameter"+value);
							// if it is a spagobi Analytical driver url name
							if(name.equalsIgnoreCase(SBI_ANALYTICAL_DRIVER)){
								analyticalParameters.add(value);
							}
							// if it is a classpath variable
							else if(name.equalsIgnoreCase(CLASSPATH)){
								classpathParameters.add(value);
							}
							else if(name.equalsIgnoreCase(CMD_PAR)){
								// else it is a command parameter name = value
								cmdParameters.add(value);
							}




						}

				}

				// Build arrays
//				if(cmdparametersVect.size()>0){
//					cmdParameters=new String[cmdparametersVect.size()];
//					int i=0;
//					for (Iterator iterator = cmdparametersVect.iterator(); iterator.hasNext();) {
//						String string = (String) iterator.next();
//						cmdParameters[i]=string;
//					}
//				}
//				if(analyticalVect.size()>0){
//					analyticalDriverParameters=new String[analyticalVect.size()];
//					int i=0;
//					for (Iterator iterator = analyticalVect.iterator(); iterator.hasNext();) {
//						String string = (String) iterator.next();
//						analyticalDriverParameters[i]=string;
//					}
//				}
//				if(classPathVect.size()>0){
//					classpathParameters=new String[classPathVect.size()];
//					int i=0;
//					for (Iterator iterator = classPathVect.iterator(); iterator.hasNext();) {
//						String string = (String) iterator.next();
//						classpathParameters[i]=string;
//					}
//				}
				
				
			}
		}
		logger.debug("OUT");
	}


	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getWorkName() {
		return workName;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setWorkName(String name) {
		this.workName = name;
	}



	public String getClassName() {
		return className;
	}


	public void setClassName(String className) {
		this.className = className;
	}



	public String getCommand() {
		return command;
	}


	public void setCommand(String command) {
		this.command = command;
	}


	public String getCommand_environment() {
		return command_environment;
	}


	public void setCommand_environment(String command_environment) {
		this.command_environment = command_environment;
	}


	/**
	 * To xml.
	 * 
	 * @return the string
	 */
	public String toXml() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<COMMONJ>");
		buffer.append("<WORK");
		if(workName != null && !workName.trim().equalsIgnoreCase("")) buffer.append(" workName=" + workName);
		if(className != null && !className.trim().equalsIgnoreCase("")) buffer.append(" className=" + className);
		buffer.append("/>");
		buffer.append("</COMMONJ>");

		return buffer.toString();
	}





	public String getPId() {
		return pId;
	}

	/** calculate work pid,
	 * 
	 */

	public String calculatePId() {

		UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
		UUID uuidObj = uuidGen.generateTimeBasedUUID();
		String executionId = uuidObj.toString();
		pId = executionId;
		return executionId;	
	
	}


	//}


	public Map getSbiParametersMap() {
		return sbiParametersMap;
	}


	public void setSbiParametersMap(Map sbiParametersMap) {
		this.sbiParametersMap = sbiParametersMap;
	}


	public Vector<String> getAnalyticalParameters() {
		return analyticalParameters;
	}


	public void setAnalyticalParameters(Vector<String> analyticalParameters) {
		this.analyticalParameters = analyticalParameters;
	}


	public Vector<String> getClasspathParameters() {
		return classpathParameters;
	}


	public void setClasspathParameters(Vector<String> classpathParameters) {
		this.classpathParameters = classpathParameters;
	}


	public void setCmdParameters(Vector<String> cmdParameters) {
		this.cmdParameters = cmdParameters;
	}


	public Vector<String> getCmdParameters() {
		return cmdParameters;
	}





}
