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
package it.eng.spagobi.engines.commonj.runtime;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

/**
 * contiene le configurazioni del WORK quando le legge da template
 * @author bernabei
 *
 */
public class CommonjWork {

	private static final Logger LOGGER = Logger.getLogger(CommonjWork.class);

	private static final String COMMAND = "cmd";
	private static final String COMMAND_ENVIRONMENT = "cmd_env";
	private static final String SBI_ANALYTICAL_DRIVER = "sbi_Analytical_Driver";
	private static final String CMD_PAR = "CMD_PAR";
	private static final String CLASSPATH = "classpath";

	private String pId;
	private String workName;
	private String className;
	private String command;
	private String commandEnvironment;

	/** parameters set in template*/
	private List<String> cmdParameters;
	private List<String> analyticalParameters;
	private List<String> classpathParameters;

	/** map of analyticalDriver from SpagoBIDocument*/
	private Map sbiParametersMap;

	public CommonjWork(SourceBean template) throws SpagoBIEngineException {
		LOGGER.debug("IN");
		this.load(template);
		LOGGER.debug("OUT");
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
		LOGGER.debug("IN");
		SourceBean workSB;

		Assert.assertNotNull(template, "Input parameter [template] cannot be null");

		workSB = (SourceBean)template.getAttribute("WORK");
		Assert.assertNotNull(workSB, "template cannot be null");

		workName = (String)workSB.getAttribute("workName");
		if(workName == null) {
			LOGGER.error("Missing  work name in document template");
			throw new it.eng.spagobi.engines.commonj.exception.TemplateParseException(template, "Missing  work name in document template");
		}


		className = (String)workSB.getAttribute("className");
		if(className == null) {
			LOGGER.error("Missing class specification in document template");
			throw new it.eng.spagobi.engines.commonj.exception.CommonjEngineException("Missing class specification in document template");
		}

		cmdParameters = new ArrayList<>();
		analyticalParameters = new ArrayList<>();
		classpathParameters = new ArrayList<>();

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
						LOGGER.debug("command parameter "+value);
						command=value;
					}
					else // if it is the command environment
						if(name.equalsIgnoreCase(COMMAND_ENVIRONMENT)){
							LOGGER.debug("command environment parameter"+value);
							commandEnvironment=value;
						}
						else{
							LOGGER.debug("general parameter"+value);
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

			}
		}
		LOGGER.debug("OUT");
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


	public String getCommandEnvironment() {
		return commandEnvironment;
	}


	public void setCommandEnvironment(String commandEnvironment) {
		this.commandEnvironment = commandEnvironment;
	}


	/**
	 * To xml.
	 *
	 * @return the string
	 */
	public String toXml() {
		StringBuilder buffer = new StringBuilder();
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

	public Map getSbiParametersMap() {
		return sbiParametersMap;
	}


	public void setSbiParametersMap(Map sbiParametersMap) {
		this.sbiParametersMap = sbiParametersMap;
	}


	public List<String> getAnalyticalParameters() {
		return analyticalParameters;
	}


	public void setAnalyticalParameters(List<String> analyticalParameters) {
		this.analyticalParameters = analyticalParameters;
	}


	public List<String> getClasspathParameters() {
		return classpathParameters;
	}


	public void setClasspathParameters(List<String> classpathParameters) {
		this.classpathParameters = classpathParameters;
	}


	public void setCmdParameters(List<String> cmdParameters) {
		this.cmdParameters = cmdParameters;
	}


	public List<String> getCmdParameters() {
		return cmdParameters;
	}

}
