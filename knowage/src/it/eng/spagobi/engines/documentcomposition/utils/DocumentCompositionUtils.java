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
package it.eng.spagobi.engines.documentcomposition.utils;

import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.navigation.LightNavigationManager;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.bo.Viewpoint;
import it.eng.spagobi.analiticalmodel.document.dao.IViewpointDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ParameterValuesEncoder;
import it.eng.spagobi.container.CoreContextManager;
import it.eng.spagobi.container.SpagoBISessionContainer;
import it.eng.spagobi.container.strategy.LightNavigatorContextRetrieverStrategy;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.bo.Exporters;
import it.eng.spagobi.engines.documentcomposition.configuration.Constants;
import it.eng.spagobi.engines.documentcomposition.configuration.DocumentCompositionConfiguration;
import it.eng.spagobi.engines.documentcomposition.configuration.DocumentCompositionConfiguration.Document;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.monitoring.dao.AuditManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 * Utility Class for document composition
 */

public class DocumentCompositionUtils {
	private static transient Logger logger=Logger.getLogger(DocumentCompositionUtils.class);
	public static final String messageBundle = "component_spagobidocumentcompositionIE_messages";
 

	/**
	 * Returns an url for execute the document with the engine associated.
	 * It calls relative driver.
	 * 
	 * @param objLabel the logical label of the document (gets from the template file)
	 * @param sessionContainer session object
	 * @param requestSB request object
	 * 
	 * @return String the complete url. It use this format: <code_error>|<url>. If there is an error during the execution <code_error> is valorized and url is null, else it is null and the url is complete.
	 */
	public static String getExecutionUrl(String objLabel, SessionContainer sessionContainer, SourceBean requestSB) {
		logger.debug("IN");
		
		Monitor monitor = MonitorFactory.start("spagobi.engines.DocumentCompositionUtils.getExecutionUrl");
		
		String baseUrlReturn = "";
		String urlReturn = "";

		if (objLabel == null || objLabel.equals("")){
			logger.error("Object Label is null: cannot get engine's url.");
			return "1008|";
		}

		try{
			// get the user profile from session
			SessionContainer permSession = sessionContainer.getPermanentContainer();
			IEngUserProfile profile = (IEngUserProfile)permSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// get the execution role
			CoreContextManager contextManager = new CoreContextManager(new SpagoBISessionContainer(sessionContainer), 
					new LightNavigatorContextRetrieverStrategy(requestSB));
			ExecutionInstance instance = contextManager.getExecutionInstance(ExecutionInstance.class.getName());
			String executionRole = instance.getExecutionRole();
			Integer objId = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(objLabel).getId();
			BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectForExecutionByIdAndRole(objId, executionRole);
//			BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectForExecutionByLabelAndRole(objLabel, executionRole);
//			BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(objLabel);
			if (obj == null){ 
				logger.error("Cannot obtain engine url. Document with label " + objLabel +" doesn't exist into database.");		
				List l = new ArrayList();
				l.add(objLabel);
				throw new EMFUserError(EMFErrorSeverity.ERROR, "1005", l, messageBundle);
			}
//			Engine engine = obj.getEngine();
			
			/*ALL CONTROLS OF COMPATIBILITY ARE REMANDED TO THE SINGLE ENGINE CALLED
			// GET THE TYPE OF ENGINE (INTERNAL / EXTERNAL) AND THE SUITABLE BIOBJECT TYPES			
			Domain engineType = null;
			Domain compatibleBiobjType = null;
			try {
				engineType = DAOFactory.getDomainDAO().loadDomainById(engine.getEngineTypeId());
				compatibleBiobjType = DAOFactory.getDomainDAO().loadDomainById(engine.getBiobjTypeId());
			} catch (EMFUserError error) {
				logger.error("Error retrieving document's engine information", error);
				return "1009|";
			} catch (Exception error) {
				logger.error("Error retrieving document's engine information", error);
				return "1009|";
			}
			
			String compatibleBiobjTypeCd = compatibleBiobjType.getValueCd();
			String biobjTypeCd = obj.getBiObjectTypeCode();

			// CHECK IF THE BIOBJECT IS COMPATIBLE WITH THE TYPES SUITABLE FOR THE ENGINE
			
			if (!compatibleBiobjTypeCd.equalsIgnoreCase(biobjTypeCd)) {
				// the engine document type and the biobject type are not compatible
				logger.error("Engine cannot execute input document type: " +
						"the engine " + engine.getName() + " can execute '" + compatibleBiobjTypeCd + "' type documents " +
						"while the input document is a '" + biobjTypeCd + "'.");
				Vector params = new Vector();
				params.add(engine.getName());
				params.add(compatibleBiobjTypeCd);
				params.add(biobjTypeCd);
				//errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR, 2002, params));
				return "2002|";
			}
			 */
			// IF USER CAN'T EXECUTE THE OBJECT RETURN
			//if (!ObjectsAccessVerifier.canSee(obj, profile)) return "1010|"; 

			//get object configuration
			DocumentCompositionConfiguration docConfig = null;
			docConfig = (DocumentCompositionConfiguration)contextManager.get("docConfig");

			Document document = null;
			//get correct document configuration
			List lstDoc = docConfig.getLabelsArray();
			boolean foundDoc = false;
			for (int i = 0; i < lstDoc.size(); i++){
				document = (Document)docConfig.getDocument((String)lstDoc.get(i)); 
				if (document != null){
					if (!obj.getLabel().equalsIgnoreCase(document.getSbiObjLabel()))
						continue;
					else{
						foundDoc = true;
						break;
					}
				}
			}
			if (!foundDoc){
				List l = new ArrayList();
				l.add(obj.getLabel());
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 1079, l);
				logger.error("The object with label " + obj.getLabel() + " hasn't got a document into template" );
				return "1002|"; 
			}

			String className = obj.getEngine().getClassName();
			if ((className == null || className.trim().equals("")) &&
				(document.getSnapshot() == null || !document.getSnapshot())) {
				// external engine
				//baseUrlReturn = obj.getEngine().getUrl() + "?";
				baseUrlReturn = obj.getEngine().getUrl();
				if (baseUrlReturn.indexOf("?") < 0) baseUrlReturn += "?";
				String driverClassName = obj.getEngine().getDriverName();
				IEngineDriver aEngineDriver = (IEngineDriver)Class.forName(driverClassName).newInstance();
				Map mapPars = aEngineDriver.getParameterMap(obj, profile, executionRole);
				String id = (String) requestSB.getAttribute("vpId");
				if (id != null){
					IViewpointDAO VPDAO = DAOFactory.getViewpointDAO();		
					Viewpoint vp =  VPDAO.loadViewpointByID(new Integer(id));
					String[] vpParameters = vp.getVpValueParams().split("%26");
					if (vpParameters != null){
						for (int i=0; i< vpParameters.length; i++){
							String param = (String)vpParameters[i];
							String name = param.substring(0, param.indexOf("%3D"));
							String value = param.substring(param.indexOf("%3D")+3);
							if (mapPars.get(name) != null){
								mapPars.remove(name);
								mapPars.put(name, value);
							}
							else
								mapPars.put(name, value);
						}
					}
				}
				mapPars.put(SpagoBIConstants.SBI_CONTEXT, GeneralUtilities.getSpagoBiContext());
				mapPars.put(SpagoBIConstants.SBI_HOST, GeneralUtilities.getSpagoBiHost());
				UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
				UUID uuidObj = uuidGen.generateRandomBasedUUID();
				String executionId = uuidObj.toString();
				executionId  = executionId.replaceAll("-", "");
				mapPars.put("SBI_EXECUTION_ID", executionId);
				mapPars.put("EXECUTION_CONTEXT", "DOCUMENT_COMPOSITION");
				// Auditing
				AuditManager auditManager = AuditManager.getInstance();
				Integer executionAuditId = auditManager.insertAudit(instance.getBIObject(), null, profile, executionRole, instance.getExecutionModality());
				
				// adding parameters for AUDIT updating
				if (executionAuditId != null) {
					mapPars.put(AuditManager.AUDIT_ID, executionAuditId.toString());
				}

				Set parKeys = mapPars.keySet();
				Iterator parKeysIter = parKeys.iterator();
				do
				{
					if(!parKeysIter.hasNext())
					{
						break;
					}
					String parkey = parKeysIter.next().toString();
					String parvalue = mapPars.get(parkey).toString();
					urlReturn = (new StringBuilder()).append(urlReturn).append("&").append(parkey).append("=").append(parvalue).toString();
				} while(true);

			} else {

				// internal engine
				baseUrlReturn =  GeneralUtilities.getSpagoBIProfileBaseUrl(profile.getUserUniqueIdentifier().toString());
				urlReturn = "&"+ObjectsTreeConstants.OBJECT_LABEL + "=" + objLabel;
				// identity string for context
				UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
				UUID uuid = uuidGen.generateRandomBasedUUID();
				urlReturn += "&" + LightNavigationManager.LIGHT_NAVIGATOR_ID + "=" + uuid.toString();
				if(document.getSnapshot() != null && document.getSnapshot()){
					Snapshot snap = DAOFactory.getSnapshotDAO().getLastSnapshot(objId);		
					if(snap != null){
						urlReturn += "&SNAPSHOT_ID=" + snap.getId();
					}
					urlReturn += "&OBJECT_ID=" + objId;
					urlReturn += "&ACTION_NAME=GET_SNAPSHOT_CONTENT";
				}else{
					urlReturn += "&PAGE=ExecuteBIObjectPage&" + SpagoBIConstants.IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS + "=true";
					urlReturn += "&"+ObjectsTreeConstants.MODALITY + "=" + SpagoBIConstants.DOCUMENT_COMPOSITION;
				}
			}
			
			// I add passing of SBI_LANGUAGE and SBI_COUNTRY
			// on session container they are called AF_COUNTRY and AF_LANGUAGE
			SessionContainer sContainer=sessionContainer.getPermanentContainer();
			if(sContainer!=null){
				Object language=sContainer.getAttribute("AF_LANGUAGE");
				Object country=sContainer.getAttribute("AF_COUNTRY");
				if(language==null){
					language=sContainer.getAttribute("SBI_LANGUAGE");
				}
				if(country==null){
					country=sContainer.getAttribute("SBI_COUNTRY");
				}
				if(language!=null && country!=null){
					urlReturn += "&" + SpagoBIConstants.SBI_LANGUAGE + "=" + language +"&"+SpagoBIConstants.SBI_COUNTRY+"="+country;				
				}
			}

			urlReturn += "&" + SpagoBIConstants.ROLE + "=" + executionRole;
			urlReturn += getParametersUrl(obj, document, requestSB, instance);
			//adds '|' char for management error into jsp if is necessary.
			
			urlReturn =  baseUrlReturn + urlReturn;  

			logger.debug("urlReturn: " + "|"+urlReturn);
		}catch(Exception ex){
			logger.error("Error while getting execution url: " + ex);
			return null;
		} finally {
			monitor.stop();
		}

		logger.debug("OUT");
		
		return "|"+urlReturn;
	}

	/**
	 * Returns an url for the test of the EXTERNAL engine.
	 * 
	 * @param objLabel the logical label of the document (gets from the template file)
	 * @param sessionContainer session object
	 * @param requestSB request object
	 * 
	 * @return String the complete url. It use this format: <code_error>|<url>. If there is an error during the execution <code_error> is valorized and url is null, else it is null and the url is complete.
	 */
	public static String getEngineTestUrl(String objLabel, SessionContainer sessionContainer, SourceBean requestSB) {
		logger.debug("IN");
		
		Monitor monitor = MonitorFactory.start("spagobi.engines.DocumentCompositionUtils.getEngineTestUrl");
		
		String baseUrlReturn = "";
		String urlReturn = "";

		if (objLabel == null || objLabel.equals("")){
			logger.error("Object Label is null: cannot get engine's url.");
			return "1008|";
		}

		try{
			// get the user profile from session
			SessionContainer permSession = sessionContainer.getPermanentContainer();
			BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(objLabel);
			if (obj == null){ 
				logger.error("Cannot obtain engine url. Document with label " + objLabel +" doesn't exist into database.");		
				List l = new ArrayList();
				l.add(objLabel);
				throw new EMFUserError(EMFErrorSeverity.ERROR, "1005", l, messageBundle);
			}
			
			String className = obj.getEngine().getClassName();
			if (className == null || className.trim().equals("")) {
				// external engine
				baseUrlReturn = obj.getEngine().getUrl() +"Test?";
				urlReturn =  baseUrlReturn; 
				logger.debug("urlReturn: " + "|"+urlReturn);
			} 
		}catch(Exception ex){
			logger.error("Error while getting execution url: " + ex);
			return null;
		} finally {
			monitor.stop();
		}

		logger.debug("OUT");
		
		return "|"+urlReturn;
	}
	
	/**
	 * Return a string representative an url with all parameters set with a request value (if it is present) or
	 * with the default's value.
	 * @param doc the document object that is managed
	 * @param document the document configurator
	 * @param requestSB the request object
	 * @return a string with the url completed
	 */
	private static String getParametersUrl(BIObject obj, Document document, SourceBean requestSB, ExecutionInstance instance){
		logger.debug("IN");
		String paramUrl = "";
		//set others parameters value
		Properties lstParams = document.getParams();
		String key = "";
		List values = new ArrayList();
		String singleValue = "";
		int cont = 0;
		
		try{
			if(lstParams!=null){
				ParameterValuesEncoder encoderUtility = new ParameterValuesEncoder();
				//while(enParams.hasMoreElements()) {
				for (int i=0; i<lstParams.size(); i++) {
					String typeParam =  lstParams.getProperty("type_par_"+document.getNumOrder()+"_"+cont);
					//only for parameter in input to the document managed (type equal 'IN')
					if (typeParam != null && typeParam.indexOf("IN")>=0) {
						String tmpKey = "sbi_par_label_param_"+document.getNumOrder()+"_"+cont;
						key = lstParams.getProperty(tmpKey);
						if (key == null && !document.getTypeCross().equalsIgnoreCase(Constants.CROSS_EXTERNAL)) break;

						values = (List)requestSB.getAttributeAsList(key);
						//if value isn't defined, check if there is a value into the instance(there is when a document is called from a refresh o viewpoint mode) 
						if(values == null || values.size() == 0 || ((String)values.get(0)).equals("")){
							List instanceValue = getInstanceValue(key, instance);
							if (instanceValue != null && instanceValue.size() > 0  && !instanceValue.get(0).equals("")) 
								values = instanceValue;
						}
						//if value isn't defined, gets the default value from the template
						if(values == null || values.size() == 0 || ((String)values.get(0)).equals("")){ 							
							values.add(lstParams.getProperty(("default_value_param_"+document.getNumOrder()+"_"+cont)));
						}
						logger.debug("Values to pass : " + values);
						//define a BIObjectParameter to use it for encode (multivalue management).
						if (obj.getEngine().getClassName() == null || obj.getEngine().getClassName().equalsIgnoreCase("")){
							//EXTERNAL ENGINES
							BIObjectParameter par = getBIObjectParameter(obj, key);
							par.setParameterValues(values);
							Parameter tmpPar = par.getParameter() ;				
							logger.debug("Manage parameter : " + tmpPar.getLabel() + "...");
							if (tmpPar != null && values.size()>1 && tmpPar.getModalityValue() != null &&
									((!(par).isMultivalue()) ||
										tmpPar.getModalityValue().getITypeCd().equalsIgnoreCase(SpagoBIConstants.INPUT_TYPE_MAN_IN_CODE))){ 
								logger.debug("Force the multivalue modality for parameter " + tmpPar.getLabel());
								//force the multivalue management if the parameter has defined as MANUAL INPUT and the values is multiple.							
								tmpPar.getModalityValue().setMultivalue(true);
								tmpPar.getModalityValue().setITypeCd(SpagoBIConstants.INPUT_TYPE_QUERY_CODE);
								par.setParameter(tmpPar);
							}
							String parsValue = encoderUtility.encode(par);
							//conversion in UTF-8 of the par
							Map parsMap = new HashMap();
							parsMap.put(key, parsValue);
							String tmpUrl = GeneralUtilities.getUrl("",  parsMap);
							logger.debug("tmpUrl for " + obj.getLabel() + ": " + tmpUrl);
							paramUrl += "&" + tmpUrl.substring(tmpUrl.indexOf("?")+1);
							
							//paramUrl += "&" + key + "=" + tmpUrl;
						}else{
							//INTERNAL ENGINES
							for (int j=0; j < values.size(); j++){
								singleValue = (String)values.get(j);
								if (singleValue.equals("%")) singleValue = "%25";
								//setting an url likes &key=val1;val2;valn
								if (j == 0) {
									paramUrl += "&" + key + "=" + singleValue;
								}else{
									paramUrl += ";" + singleValue;
								}
							}
						}
						
						cont++;
					}
				}
			}
		}catch(Exception ex){
			logger.error("Error while getting parameter's document " + document.getSbiObjLabel() + " param: " + key + ": " + ex);
			return null;
		}

		/*
		if (forInternalEngine)
			paramUrl = paramUrl.substring(0, paramUrl.length()-3); 
		else
			paramUrl = paramUrl.substring(0, paramUrl.length()-5); 
		 */
		logger.debug("paramUrl: " + paramUrl);
		logger.debug("OUT");
		return paramUrl;
	}

	/**
	 * Return an hashmap of all parameters for the document managed
	 * @param urlReturn String with url and parameters
	 * @return HashMap 
	 */
	private static HashMap getAllParamsValue(String urlReturn){
		HashMap retHM = new HashMap();
		String tmpStr = urlReturn.substring(urlReturn.indexOf("?")+1);
		String[] tmpArr = tmpStr.split("&");
		for (int i=0; i<tmpArr.length; i++){
			String strPar = (String)tmpArr[i];
			String key = strPar.substring(0,strPar.indexOf("="));
			String value = strPar.substring(strPar.indexOf("=")+1);
			retHM.put(key, value);
		}
		return retHM;
	}

	private static List getInstanceValue(String key, ExecutionInstance instance){
		List retVal = new ArrayList();
		BIObject obj = instance.getBIObject();
		List objPars = obj.getBiObjectParameters();

		for (int i=0; i < objPars.size(); i++){
			BIObjectParameter objPar = (BIObjectParameter)objPars.get(i);
			if (objPar.getParameterUrlName().equalsIgnoreCase(key)){
				retVal.add((objPar.getParameterValues()==null)?"":(String)objPar.getParameterValues().get(0));
				break;
			}
		}
		return retVal;

	}
	
	/**
	 * Return the BIObjectParameter with the key passed
	 * @param key String with url (identifier) of parameter
	 * @return BIObjectParameter 
	 */
	private static BIObjectParameter getBIObjectParameter(BIObject obj, String urlKey){
		if (urlKey == null || urlKey.equals("")) return null;
		
		BIObjectParameter par = null;
		List objParams = obj.getBiObjectParameters();
		for (int i = 0, l = objParams.size(); i < l; i++){
			par = (BIObjectParameter) objParams.get(i);
			if (par.getParameterUrlName().equals(urlKey))
				break;
		}
		
		return par;
	}
	/**Method called by document composition publisher , that returns alla available exporters for a single document contained in the composed one.
	 * @param objLabel
	 * @param sessionContainer
	 * @param requestSB
	 * @return
	 */
	public static List getAvailableExporters(String objLabel, SessionContainer sessionContainer, SourceBean requestSB){
		logger.debug("IN");

		List<Exporters> exporters = null;
		List<String> exportersTypes = null;
		if (objLabel == null || objLabel.equals("")){
			logger.error("Object Label is null: cannot get engine's url.");
			return null;
		}

		try{
			// get the user profile from session
			SessionContainer permSession = sessionContainer.getPermanentContainer();
			IEngUserProfile profile = (IEngUserProfile)permSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(objLabel);
			if (obj == null){
				logger.error("Cannot obtain engine url. Document with label " + objLabel +" doesn't exist into database.");		
				List l = new ArrayList();
				l.add(objLabel);
				throw new EMFUserError(EMFErrorSeverity.ERROR, "1005", l, messageBundle);
			}
			Engine engine = obj.getEngine();
			exporters = DAOFactory.getEngineDAO().getAssociatedExporters(engine);
			if(exporters != null){
				exportersTypes = new ArrayList<String>();
				for(int i=0; i< exporters.size(); i++){
					Domain domain = DAOFactory.getDomainDAO().loadDomainById(exporters.get(i).getDomainId());
					String cd = domain.getValueCd();
					exportersTypes.add(cd);
				}
			}
		}catch(Exception e){
			logger.error("Error while getting document's exporters for label :" + objLabel+ ": " + e);
			return null;
		}finally{
			logger.debug("OUT");
		}
		
		return exportersTypes;
	}
}
