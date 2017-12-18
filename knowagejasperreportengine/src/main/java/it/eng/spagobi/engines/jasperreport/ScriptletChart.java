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

package it.eng.spagobi.engines.jasperreport;




import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import net.sf.jasperreports.engine.fill.JRFillParameter;
import net.sf.jasperreports.engine.fill.JRFillVariable;

import org.apache.log4j.Logger;

import it.eng.spagobi.services.proxy.DocumentExecuteServiceProxy;
import it.eng.spagobi.utilities.engines.EngineConstants;

/**
 * @author Giulio Gavardi
 *         giulio.gavardi@eng.it
 */

public class ScriptletChart extends JRDefaultScriptlet {

	private static transient Logger logger=Logger.getLogger(ScriptletChart.class);

	public static final String CHART_LABEL="chart_label"; 
	public static final String CHART_IMAGE="chart_image"; 
	public static int iii=1;

	public void afterReportInit() throws JRScriptletException {

		logger.debug("IN");


		try {
			// Creazione GET method

			HashMap parametersMap=(HashMap)this.getParameterValue("REPORT_PARAMETERS_MAP");

			DocumentExecuteServiceProxy proxy=(DocumentExecuteServiceProxy)parametersMap.get(EngineConstants.ENV_DOCUMENT_EXECUTE_SERVICE_PROXY);
			logger.debug("DocumentExecuteServiceProxy is equal to [" + proxy + "]");
			HashMap chartParameters=new HashMap();

			// Get all defined variables wich start with prefix sbichart,
			// each is an image to fill
			Map allVariables = this.variablesMap;
			Map allParameters = this.parametersMap;

			boolean oldModeRetrievingChart=false;
			boolean done=false;
			if(allVariables==null)allVariables=new HashMap();
			if(allParameters==null)allParameters=new HashMap();


			// First search for the chart_label variable, if present switch to "old mode"

			for (Iterator iterator = allVariables.keySet().iterator(); iterator.hasNext() && done==false;) {
				String varName = (String) iterator.next();
				if(varName.equalsIgnoreCase(CHART_LABEL)){
					oldModeRetrievingChart=true;
					logger.debug("old mode for retrieving chart, getting label from CHART LABEL variable and inserting in CHART_IMAGE variable");
					done=true;
				}
			}


			logger.debug("Running all variables");
			done=false;
			for (Iterator iterator = allVariables.keySet().iterator(); iterator.hasNext() && done==false;) {
				String varName = (String) iterator.next();
				if(oldModeRetrievingChart==true){
					if(varName.equalsIgnoreCase(CHART_LABEL)){
						JRFillVariable labelValueO = (JRFillVariable) allVariables.get(CHART_LABEL);
						String labelValue = null;
						if(labelValueO.getValue()==null){
							logger.error("CHART_LABEL variable has no value");
							return;
						}
						labelValue=(String)labelValueO.getValue();
						
						// Set other parameters (only not system defined)
						for (Iterator iterator2 = allParameters.keySet().iterator(); iterator2.hasNext();) {
							String namePar = (String) iterator2.next();
							JRFillParameter par=(JRFillParameter)allParameters.get(namePar);
							if(!par.isSystemDefined() && par.getValue()!=null){
								Object val=par.getValue();
								chartParameters.put(namePar, val.toString());
							}
						}

						logger.debug("execute chart with lable "+labelValue);
						
						logger.debug("Calling Service");
						byte[] image=proxy.executeChart(labelValue, chartParameters);
						logger.debug("Back from Service");

						InputStream is=new ByteArrayInputStream(image);

						logger.debug("Input Stream filled, Setting variable");
						if(allVariables.keySet().contains(CHART_IMAGE)){
							this.setVariableValue(CHART_IMAGE, is);
						}
						else{
							logger.error("variable where to set image chart "+CHART_IMAGE+ " not defined");
							return;
						}
						done=true;
					}
				}
				else if(oldModeRetrievingChart==false){
					if(varName.startsWith("sbichart_")){
						logger.debug("Processing variable "+varName);					
						JRFillVariable variable=(JRFillVariable)allVariables.get(varName);
						if(variable.getValue()!=null){
							chartParameters=new HashMap();
//							the realVarName is the name of the target variable!
							String areaValue=varName.substring(9);
							// call a utility function that parse the variable, in the form var1=val1;var2=val2
							String varVal=(String)variable.getValue();
							// Value is defined as chart_label=label;par1=val1;par2=val2;
							Map nameValuePars=parseVariable(varVal);

							// check if there is the main parameters defined:
							// chart_label : indicating the label of the chart that has to be called.
							if(nameValuePars.get(CHART_LABEL)!=null){
								String labelValue=(String) nameValuePars.get(CHART_LABEL);
								logger.debug("execute chart with lable "+labelValue);

								// Set other parameters
								for (Iterator iterator2 = nameValuePars.keySet().iterator(); iterator2.hasNext();) {
									String namePar = (String) iterator2.next();
									if(!namePar.equalsIgnoreCase(CHART_LABEL)){
										Object value=nameValuePars.get(namePar);
										chartParameters.put(namePar, value);
									}
								}

								logger.debug("Calling Service");
								byte[] image=proxy.executeChart(labelValue, chartParameters);
								logger.debug("Back from Service");

								InputStream is=new ByteArrayInputStream(image);

								logger.debug("Input Stream filled, Setting variable");
								if(variablesMap.keySet().contains(areaValue)){
									this.setVariableValue(areaValue, is);
								}
								else{
									logger.error("variable where to set image chart "+areaValue+ " not defined");
								}

								//is.close();
							}
							else{
								logger.error("chart_label not specified");							
							}
						}
						else{
							logger.warn("no value associated to the sbichart_ variable");
						}
					}

				}
			}

			logger.debug("OUT");
		} 
		catch (Exception e) {
			logger.error("Error in scriptlet",e);
			throw new JRScriptletException(e);
		}
	}

	/**
	 *  varValue: parses the variable value in the form att1=value1; att2=value2;
	 */

	protected Map parseVariable(String varValue){
		logger.debug("IN");
		HashMap toReturn=new HashMap<String, String>();
		try{
			StringTokenizer tokenizer=new StringTokenizer(varValue,";");
			while(tokenizer.hasMoreTokens()){
				String token=tokenizer.nextToken();
				int indexEqual=token.indexOf("=");
				String namePar=token.substring(0,indexEqual);
				String valuePar=token.substring(indexEqual+1);
				toReturn.put(namePar, valuePar);
			}
		}
		catch (Exception e) {
			logger.error("Error in target definition (should be target_x[att: val, att2: val])", e);
			return new HashMap<String, String>();
		}
		logger.debug("OUT");
		return toReturn;
	}













}
