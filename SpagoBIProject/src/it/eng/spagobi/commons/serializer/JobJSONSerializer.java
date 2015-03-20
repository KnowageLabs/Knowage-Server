/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.tools.scheduler.bo.Job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class JobJSONSerializer implements Serializer {
	private static transient Logger logger = Logger.getLogger(JobJSONSerializer.class);

	public static final String JOB_NAME = "jobName";
	public static final String JOB_GROUP = "jobGroup";
	public static final String JOB_DESCRIPTION = "jobDescription";
	public static final String JOB_CLASS = "jobClass";
	public static final String JOB_DURABILITY = "jobDurability";
	public static final String JOB_REQUEST_RECOVERY = "jobRequestRecovery";
	public static final String USE_VOLATILITY = "useVolatility";
	public static final String JOB_PARAMETERS = "jobParameters";
	public static final String JOB_DOCUMENTS = "documents";
	public static final String JOB_DOCUMENTS_PARAMETERS = "parameters";
	public static final String JOB_DOCUMENTS_CONDENSED_PARAMETERS = "condensedParameters";

	
	public Object serialize(Object o, Locale locale)
			throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof Job) ) {
			throw new SerializationException("JobJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		try {
			Job job = (Job)o;
			result = new JSONObject();
			result.put(JOB_NAME, job.getName());
			result.put(JOB_GROUP, job.getGroupName());
			result.put(JOB_DESCRIPTION, job.getDescription());
			result.put(JOB_CLASS, job.getJobClass().getName());
			result.put(JOB_DURABILITY, job.isDurable());
			result.put(JOB_REQUEST_RECOVERY, job.isRequestsRecovery());
			result.put(USE_VOLATILITY, job.isVolatile() );

			JSONArray parsListJSON = new JSONArray();
			Map<String, String>  parameters = job.getParameters();
			Set<String> keys = parameters.keySet();
			for (String key : keys) {
				String value = parameters.get(key);
				
				JSONObject jsonPar = new JSONObject();
				jsonPar.put("name", key);
				jsonPar.put("value", value);
				parsListJSON.put(jsonPar);
			}
			result.put(JOB_PARAMETERS, parsListJSON);	
						
			//Search parameters for extracting documents name and linked parameters with values
			String[] documentsLabels = null;
			String documentsLabelsParam = parameters.get("documentLabels"); //this is a parameter with documents name
			if ((documentsLabelsParam != null) && (!documentsLabelsParam.isEmpty())){
				documentsLabels = documentsLabelsParam.split(",");
			}
			JSONArray documentsJSON = new JSONArray();
			if (documentsLabels != null){
				for(int i=0; i<documentsLabels.length; i++) {
					//this will clean the string and get only the document (biobj) real name
					String documentName  = documentsLabels[i].substring(0, documentsLabels[i].indexOf("__")); 
					JSONObject aDocumentJSON = new JSONObject();

					aDocumentJSON.put("name",documentName);
					
					StringBuffer parametersCondensedString = new StringBuffer();
					
					JSONArray documentParametersJSON = new JSONArray();
					//search document parameters
					String internalDocumentName = documentName + "__" + (i+1);
					
					//retrieve iterative parameters (for document iterating on each document parameter's value)
					//------------------------------------------------
					Set iterativeParameters = new LinkedHashSet();
					String documentIterativeParameters = parameters.get(internalDocumentName+ "_iterative");
					if ((documentIterativeParameters != null) && (!documentIterativeParameters.isEmpty())){				
							String[] iterativeParametersArray = documentIterativeParameters.split(";");
							iterativeParameters.addAll(Arrays.asList(iterativeParametersArray));					
					}
					
					//retrieve loadAtRuntime parameters (for document parameters using Analitical Drivers)
					Map<String, String> loadAtRuntimeParameters = new HashMap<String, String>();
					String documentLoadAtRuntimeParameters = parameters.get(internalDocumentName+ "_loadAtRuntime");

					if ((documentLoadAtRuntimeParameters != null) && (!documentLoadAtRuntimeParameters.isEmpty())){
						String[] loadAtRuntimeArray = documentLoadAtRuntimeParameters.split(";");
						for (int count = 0; count < loadAtRuntimeArray.length; count++) {
							String loadAtRuntime = loadAtRuntimeArray[count];
							int parameterUrlNameIndex = loadAtRuntime.lastIndexOf("(");
							String parameterUrlName = loadAtRuntime.substring(0, parameterUrlNameIndex);
							String userAndRole = loadAtRuntime.substring(parameterUrlNameIndex + 1, loadAtRuntime.length() - 1);
							loadAtRuntimeParameters.put(parameterUrlName, userAndRole);
							
							JSONObject jsonPar = new JSONObject();
							jsonPar.put("name", parameterUrlName);
							jsonPar.put("value", userAndRole);
							jsonPar.put("type", "loadAtRuntime");
							if (iterativeParameters.contains(parameterUrlName)){
								jsonPar.put("iterative", true);
							} else {
								jsonPar.put("iterative", false);
							}
							documentParametersJSON.put(jsonPar);
							
						}
					}
					

					
					//retrieve formula parameters 
					//------------------------------------------------
					Map<String, String> useFormulaParameters = new HashMap<String, String>();
					String documentFormulaParameters = parameters.get(internalDocumentName+ "_useFormula");
					if ((documentFormulaParameters != null) && (!documentFormulaParameters.isEmpty())){
						String[] useFormulaArray = documentFormulaParameters.split(";");
						for (int count = 0; count < useFormulaArray.length; count++) {
							String useFormula = useFormulaArray[count];
							int parameterUrlNameIndex = useFormula.lastIndexOf("(");
							String parameterUrlName = useFormula.substring(0, parameterUrlNameIndex);
							String fName = useFormula.substring(parameterUrlNameIndex + 1, useFormula.length() - 1);
							useFormulaParameters.put(parameterUrlName, fName);
							
							JSONObject jsonPar = new JSONObject();
							jsonPar.put("name", parameterUrlName);
							jsonPar.put("value", fName);
							jsonPar.put("type", "formula");
							if (iterativeParameters.contains(parameterUrlName)){
								jsonPar.put("iterative", true);
							} else {
								jsonPar.put("iterative", false);
							}

							documentParametersJSON.put(jsonPar);
						}
					}
					
					//retrieve documents parameters (normal parameters in Query Parameters format style)
					//------------------------------------------------
					String documentParametersString = parameters.get(internalDocumentName);

					if ((documentParametersString != null) && (!documentParametersString.isEmpty())){
						String[] parCouples = documentParametersString.split("%26");
						for(int j=0; j<parCouples.length; j++) {
							String parCouple = parCouples[j];
							String[] parDef = parCouple.split("=");
							
							
							String parameterName = parDef[0];
							String parameterValues = parDef[1];

							JSONObject jsonPar = new JSONObject();
							jsonPar.put("name", parameterName);
							jsonPar.put("value", parameterValues);
							jsonPar.put("type", "fixed");
							if (iterativeParameters.contains(parameterName)){
								jsonPar.put("iterative", true);
							} else {
								jsonPar.put("iterative", false);
							}
							documentParametersJSON.put(jsonPar);
							
							parametersCondensedString.append(" "+parameterName+" = "+parameterValues+" | ");
							
						}

					}
					
					//put condensed parameters representation inside aDocument JSONObject
					aDocumentJSON.put(JOB_DOCUMENTS_CONDENSED_PARAMETERS,parametersCondensedString.toString());

					//put parameters JSONArray inside aDocument JSONObject
					aDocumentJSON.put(JOB_DOCUMENTS_PARAMETERS,documentParametersJSON);
					
					//put the document JSONObject in the documents JSONArray
					documentsJSON.put(aDocumentJSON);
				}
			}

			
			result.put(JOB_DOCUMENTS, documentsJSON );
			
			
			
			
		}catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {

			
		}
		
		return result;
		
	}

}
