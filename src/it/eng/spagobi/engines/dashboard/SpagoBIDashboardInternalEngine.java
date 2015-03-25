/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.dashboard;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ParameterValuesEncoder;
import it.eng.spagobi.engines.InternalEngineIFace;
import it.eng.spagobi.engines.chart.utils.DataSetAccessFunctions;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.utilities.ParametersDecoder;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.cglib.transform.impl.AddDelegateTransformer;

import org.apache.log4j.Logger;

public class SpagoBIDashboardInternalEngine implements InternalEngineIFace {

    private static transient Logger logger = Logger.getLogger(SpagoBIDashboardInternalEngine.class);

    public static final String messageBundle = "MessageFiles.component_spagobidashboardIE_messages";
    private static final String CONF_FROM_DATASET = "CONF_FROM_DATASET";
    private static final String CONF_FROM_DATASET_VALUE = "CONF_FROM_DATASET_VALUE";
    private static final String CONF_FROM_TEMPLATE = "CONF_FROM_TEMPLATE";
    
    Map confParameters;
    Map dataParameters;
    Map drillParameters;

    /**
     * Executes the document and populates the response.
     * 
     * @param requestContainer The <code>RequestContainer</code> object (the session
     * can be retrieved from this object)
     * @param obj The <code>BIObject</code> representing the document to
     * be executed
     * @param response The response <code>SourceBean</code> to be populated
     * 
     * @throws EMFUserError the EMF user error
     */
    public void execute(RequestContainer requestContainer, BIObject obj, SourceBean response) throws EMFUserError {

		logger.debug("IN");
	
		try {
	
		    if (obj == null) {
				logger.error("The input object is null.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, "100", messageBundle);
		    }
	
		    if (!obj.getBiObjectTypeCode().equalsIgnoreCase("DASH")) {
				logger.error("The input object is not a dashboard.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, "1001", messageBundle);
		    }
	
		    byte[] contentBytes = null;
		    try {
			ObjTemplate template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(obj.getId());
			if (template == null)
			    throw new Exception("Active Template null");
			contentBytes = template.getContent();
			if (contentBytes == null)
			    throw new Exception("Content of the Active template null");
		    } catch (Exception e) {
				logger.error("Error while recovering template content: \n" , e);
				throw new EMFUserError(EMFErrorSeverity.ERROR, "1002", messageBundle);
		    }
		    // get bytes of template and transform them into a SourceBean
		    SourceBean content = null;
		    try {
				String contentStr = new String(contentBytes);
				content = SourceBean.fromXMLString(contentStr);
		    } catch (Exception e) {
				logger.error("Error while converting the Template bytes into a SourceBean object");
				throw new EMFUserError(EMFErrorSeverity.ERROR, "1003", messageBundle);
		    }
		    // get information from the conf SourceBean and pass them into the
		    // response
		    String movie = (String) content.getAttribute("movie");
		    String width = (String) content.getAttribute("DIMENSION.width");
		    String height = (String) content.getAttribute("DIMENSION.height");
	
		    String dataurl = (String) content.getAttribute("DATA.url");
		    
		    // get all the parameters for data url
		    dataParameters = new LinkedHashMap();
		    confParameters = new LinkedHashMap();
		    drillParameters = new LinkedHashMap();
		    
		    SessionContainer session = requestContainer.getSessionContainer();
		    IEngUserProfile profile = (IEngUserProfile) session.getPermanentContainer().getAttribute(
			    IEngUserProfile.ENG_USER_PROFILE);
		    
		    SourceBean serviceRequest=requestContainer.getServiceRequest();
		    
			 // get all the parameters for dash configuration		    
		    defineDataParameters(content, obj, profile);
		    defineConfParameters(content, profile);
		    defineLinkParameters(content, serviceRequest);
		    
		    // set information into response
		    response.setAttribute(ObjectsTreeConstants.SESSION_OBJ_ATTR, obj);
		    response.setAttribute("movie", movie);
		    response.setAttribute("dataurl", dataurl);
		    response.setAttribute("width", width);
		    response.setAttribute("height", height);
		    
		    response.delAttribute("confParameters");
		    response.setAttribute("confParameters", getConfParameters());
		    response.delAttribute("dataParameters");		    
		    response.setAttribute("dataParameters", getDataParameters());	
		    response.delAttribute("drillParameters");
		    response.setAttribute("drillParameters", getDrillParameters());	

		    // set information for the publisher
		    response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "DASHBOARD");
	
		} catch (EMFUserError error) {
		    logger.error("Cannot exec the dashboard", error);
		    throw error;
		} catch (Exception e) {
		    logger.error("Cannot exec the dashboard", e);
		    throw new EMFUserError(EMFErrorSeverity.ERROR, "100", messageBundle);
		} finally {
		    logger.debug("OUT");
		}
    }

    /**
     * The <code>SpagoBIDashboardInternalEngine</code> cannot manage
     * subobjects so this method must not be invoked.
     * 
     * @param requestContainer The <code>RequestContainer</code> object (the session
     * can be retrieved from this object)
     * @param obj The <code>BIObject</code> representing the document
     * @param response The response <code>SourceBean</code> to be populated
     * @param subObjectInfo An object describing the subobject to be executed
     * 
     * @throws EMFUserError the EMF user error
     */
    public void executeSubObject(RequestContainer requestContainer, BIObject obj, SourceBean response,
	    Object subObjectInfo) throws EMFUserError {
    	// it cannot be invoked
		logger.error("SpagoBIDashboardInternalEngine cannot exec subobjects.");
		throw new EMFUserError(EMFErrorSeverity.ERROR, "101", messageBundle);
    }

    /**
     * Function not implemented. Thid method should not be called
     * 
     * @param requestContainer The <code>RequestContainer</code> object (the session
     * can be retrieved from this object)
     * @param response The response <code>SourceBean</code> to be populated
     * @param obj the obj
     * 
     * @throws InvalidOperationRequest the invalid operation request
     * @throws EMFUserError the EMF user error
     */
    public void handleNewDocumentTemplateCreation(RequestContainer requestContainer, BIObject obj, SourceBean response)
	    throws EMFUserError, InvalidOperationRequest {
		logger.error("SpagoBIDashboardInternalEngine cannot build document template.");
		throw new InvalidOperationRequest();

    }

    /**
     * Function not implemented. Thid method should not be called
     * 
     * @param requestContainer The <code>RequestContainer</code> object (the session
     * can be retrieved from this object)
     * @param response The response <code>SourceBean</code> to be populated
     * @param obj the obj
     * 
     * @throws InvalidOperationRequest the invalid operation request
     * @throws EMFUserError the EMF user error
     */
    public void handleDocumentTemplateEdit(RequestContainer requestContainer, BIObject obj, SourceBean response)
	    throws EMFUserError, InvalidOperationRequest {
		logger.error("SpagoBIDashboardInternalEngine cannot build document template.");
		throw new InvalidOperationRequest();
    }
    
    /**
	 * set parameters configuration for the creation of the dashboard getting them from template or from Dataset/LOV.
	 * 
	 * @param content the content of the template.
	 * @param profile the user's profile
	 * 
	 * @return A chart that displays a value as a dial.
	 */
	private void defineConfParameters(SourceBean content, IEngUserProfile profile) throws Exception {
		logger.debug("IN");
		
		boolean isDsConfDefined = false;
		String confDataset = "";
		String confType = "";
	
		// get all the parameters for dash configuration
	    confParameters = new LinkedHashMap();
	    SourceBean confSB = (SourceBean) content.getAttribute("CONF");
	    List confAttrsList = confSB.getContainedSourceBeanAttributes();
	    Iterator confAttrsIter = confAttrsList.iterator();
	    while (confAttrsIter.hasNext()) {
			SourceBeanAttribute paramSBA = (SourceBeanAttribute) confAttrsIter.next();
			SourceBean param = (SourceBean) paramSBA.getValue();
			String nameParam = (String) param.getAttribute("name");
			boolean isTitle = false;
			if (nameParam.equals("title")) isTitle = true;
			String valueParam = replaceParsInString((String) param.getAttribute("value"), isTitle);	
			
			confParameters.put(nameParam, valueParam);
	    }	
	    
	    //defines if configuration is by specific dataset
	    if(confParameters.get("confdataset")!=null && !(((String)confParameters.get("confdataset")).equalsIgnoreCase("") )){	
			confDataset=(String)confParameters.get("confdataset");
			isDsConfDefined=true;
		}
		else {
			isDsConfDefined=false;
		}
	    
	    //gets the configuration type (by template, specific dataset or value's dataset
	    if(confParameters.get("conftype")!=null && !(((String)confParameters.get("conftype")).equalsIgnoreCase("") )){	
			confType=(String)confParameters.get("conftype");
		}
		else {
			//force the value for compatibility to previous version
			if (isDsConfDefined){
				confType = CONF_FROM_DATASET; 
			}
			else
				confType = CONF_FROM_TEMPLATE;
		}
	    // if the configuration is defined into template goes out
	    if (confType.equalsIgnoreCase(CONF_FROM_TEMPLATE)) return;

	    //if the configuration is defined into specific dataset reading it and compiles attributes with its values.
		if(confType.equalsIgnoreCase(CONF_FROM_DATASET) && isDsConfDefined){
			logger.debug("configuration defined in dataset "+confDataset);
			
			String parameters=DataSetAccessFunctions.getDataSetResultFromLabel(profile, confDataset, dataParameters);			
			SourceBean sourceBeanResult=null;
			try {
				sourceBeanResult = SourceBean.fromXMLString(parameters);
			} catch (SourceBeanException e) {
				logger.error("error in reading configuration lov");
				throw new Exception("error in reading configuration lov");
			}
			SourceBean sbRow = (SourceBean)sourceBeanResult.getAttribute("ROW");
			if (sbRow == null){				
				logger.error("The specific configuration dataset '" + confDataset +"' doesn't return rows. Get configuration by template. ATTENTION: the widget could not be created correctly!");
				//throw new EMFUserError(EMFErrorSeverity.ERROR, "1001", messageBundle);
			}else{
				addDashboardConfigValues(0, sbRow);
				addGenericConfigValues(sbRow);
			}
		}else if(confType.equalsIgnoreCase(CONF_FROM_DATASET_VALUE)){
			logger.debug("configuration defined in dataset of data values (dynamic case) " + confDataset);
			
			String datasetID =(String)dataParameters.get("datasetid");
			String series = DataSetAccessFunctions.getDataSetResultFromId(profile, datasetID, dataParameters);
			SourceBean sourceBeanResult = null;
			try {
				sourceBeanResult = SourceBean.fromXMLString(series);
			} catch (SourceBeanException e) {
				logger.error("error in reading configuration lov");
				throw new Exception("error in reading configuration lov");
			}
			List<SourceBean> sbRows = (List)sourceBeanResult.getAttributeAsList("ROW");
			//defines the number of the dashboards from the number or rows (1 row = 1 serie)
			int numCharts = sbRows.size();
			if (numCharts == 0){
				logger.error("The specific configuration dataset with id '" + datasetID +"' doesn't return rows. Get default configuration.");
				confParameters.put("numCharts", "1");
				addDefaultDashboardConfig();
			}else{
				confParameters.put("numCharts", String.valueOf(numCharts));
				for(int i=0; i<numCharts; i++){
					SourceBean sbRow = (SourceBean)sbRows.get(i);
					if (sbRow == null){
						logger.error("The configuration getted by value's dataset with identifier '" + datasetID +"' doesn't return rows. \n Get configuration by template. ATTENTION: the widget could not be created correctly!");
						break;
					}else{
						addDashboardConfigValues(i, sbRow);
					}
				}
			}
		}
		else
			logger.debug("Configuration set in template");
		
		logger.debug("OUT");
	}
	
    /**
	 * set parameters for getting the data 
	 * 
	 * @param content the content of the template.
	 * @param obj the object document
	 * @param profile the user's profile
	 * 
	 * @return A chart that displays a value as a dial.
	 */
	private void defineDataParameters(SourceBean content, BIObject obj, IEngUserProfile profile) throws Exception {
		logger.debug("IN");
		SourceBean dataSB = (SourceBean) content.getAttribute("DATA");
	    List dataAttrsList = dataSB.getContainedSourceBeanAttributes();
	    Iterator dataAttrsIter = dataAttrsList.iterator();
	    
	    if(obj.getDataSetId()!=null){
		    String dataSetId=obj.getDataSetId().toString();
		    dataParameters.put("datasetid", dataSetId);
	    }
	    while (dataAttrsIter.hasNext()) {
			SourceBeanAttribute paramSBA = (SourceBeanAttribute) dataAttrsIter.next();
			SourceBean param = (SourceBean) paramSBA.getValue();
			String nameParam = (String) param.getAttribute("name");
			String valueParam = (String) param.getAttribute("value");
	
			dataParameters.put(nameParam, valueParam);
	    }
	    
	    // puts the document id
	    dataParameters.put("documentId", obj.getId().toString());
	    // puts the userId into parameters for data recovery
	    dataParameters.put("userid", ((UserProfile)profile).getUserUniqueIdentifier());

	    // create the title
	    /*
	    String title = "";
	    title += obj.getName();
	    String objDescr = obj.getDescription();
	    if ((objDescr != null) && !objDescr.trim().equals("")) {
	    	title += ": " + objDescr;
	    }
	    */
	    //Search if the chart has parameters
	    /*
		String parameters="";
		List parametersList=obj.getBiObjectParameters();
		logger.debug("Check for BIparameters and relative values");
		if(parametersList!=null){
			ParametersDecoder decoder = new ParametersDecoder();
			for (Iterator iterator = parametersList.iterator(); iterator.hasNext();) {
				BIObjectParameter par= (BIObjectParameter) iterator.next();
				String url=par.getParameterUrlName();
				List values=par.getParameterValues();
				
				
				if(values!=null){
					
					if(values.size()==1){
						String value=(String)values.get(0);
						dataParameters.put(url, value);
					}else if(values.size() >=1){
						String value = "'"+(String)values.get(0)+"'";					
						for(int k = 1; k< values.size() ; k++){
							value = value + ",'" + (String)values.get(k)+"'";
						}
						dataParameters.put(url, value);
					}
				}
			}	
		}
		*/
		dataParameters = addBIParameters (obj, dataParameters);
		logger.debug("OUT");
	}
	
	
	/**
     * Add into the parameters map the BIObject's BIParameter names and values
     * @param biobj BIOBject to execute
     * @param pars Map of the parameters for the execution call  
     * @return Map The map of the execution call parameters
     */
	private Map addBIParameters(BIObject biobj, Map pars) {
		logger.debug("IN");
		
		if(biobj==null) {
			logger.warn("BIObject parameter null");	    
		    return pars;
		}
		
		ParameterValuesEncoder parValuesEncoder = new ParameterValuesEncoder();
		if(biobj.getBiObjectParameters() != null){
			BIObjectParameter biobjPar = null;
			for(Iterator it = biobj.getBiObjectParameters().iterator(); it.hasNext();){
				try {
					biobjPar = (BIObjectParameter)it.next();									
					String value = parValuesEncoder.encode(biobjPar);
		            if (biobjPar!=null && biobjPar.getParameterUrlName()!=null && value!=null) {
						 pars.put(biobjPar.getParameterUrlName(), value);
						 logger.debug("Add parameter:"+biobjPar.getParameterUrlName()+"/"+value);
		             } else {
		                 logger.warn("NO parameter are added... something is null");
		             }
				} catch (Exception e) {
					logger.error("Error while processing a BIParameter",e);
				}
			}
		}
		
		logger.debug("OUT");
  		return pars;
	}
	
	/**
     * Decode special chars into the parameters map (ie. for %25 that otherwise isn't decodified)
     * @param pars Map of the parameters for the execution call  
     * @return Map The map of the execution call parameters
     */
	private Map encodePars(Map pars) {
		logger.debug("IN");
		
		if(pars==null) {
			logger.warn("There aren't parameters to decode");	    
		    return pars;
		}
		
		Map toReturn = new LinkedHashMap(); 
		Iterator parsIter = pars.entrySet().iterator();		
		while (parsIter.hasNext()) {
			Map.Entry obj = (Map.Entry)parsIter.next();
		    String name = (String)obj.getKey();
		    String value = (String) obj.getValue();			
		    if (value.equals("%")) value = "%25";
		    else if (value.equals(";%")) value = ";%25";
		    toReturn.put(name, value);
		 }
		logger.debug("OUT");
  		return toReturn;
	}
	
    /**
	 * set parameters for the drill action.
	 * 
	 * @param content the content of the template.
	 * 
	 * @return A chart that displays a value as a dial.
	 */
	private void defineLinkParameters(SourceBean content, SourceBean serviceRequest) throws Exception {
		logger.debug("IN");
		
		SourceBean drillSB = (SourceBean)content.getAttribute("DRILL");
		String drillLabel="";
		Map tmpDrillParameters= new LinkedHashMap();
		
		if(drillSB!=null){
			String lab=(String)drillSB.getAttribute("document");
			if(lab!=null) drillLabel=lab;
			else{
				logger.info("Drill label not found");
			}

			List parameters =drillSB.getAttributeAsList("PARAM");
			if(parameters!=null){
				for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
					SourceBean att = (SourceBean) iterator.next();
					String name=(String)att.getAttribute("name");
					String type=(String)att.getAttribute("type");
					String value=replaceParsInString((String)att.getAttribute("value"), false);

					//looking for the parameter before into the request, then into data parameters.
					//if the value is a dataset value it leaves the tag field $F{...}. The swf file will replace the value.
					if (!value.startsWith("$F{")){
						String reqValue = (String)serviceRequest.getAttribute(name);
					
						if(reqValue == null) {
							if (getDataParameters().get(name)!=null)
								value=(String)getDataParameters().get(name);
						}
						else
							value = reqValue;
					}
					tmpDrillParameters.put(name, value);
				}
			}
		}
		
		//creates the drill url
		int i=0;
		String drillUrl  = "javascript:execCrossNavigation(this.name, '"+drillLabel+"','";

		if (serviceRequest.getAttribute(ObjectsTreeConstants.MODALITY) != null &&
				((String)serviceRequest.getAttribute(ObjectsTreeConstants.MODALITY)).equals(SpagoBIConstants.DOCUMENT_COMPOSITION) )
			drillUrl  = "javascript:parent.execCrossNavigation(this.name, '"+drillLabel+"','";

		for (Iterator iterator = tmpDrillParameters.keySet().iterator(); iterator.hasNext();) {
			String tmpName = (String) iterator.next();			
			String tmpValue=(String)tmpDrillParameters.get(tmpName);
			
			if (i>0)
				drillUrl += "%26";
				
			drillUrl += tmpName + "%3D" + tmpValue;
			i++;
		}
		drillUrl += "');";
		if (i>0)
			getDrillParameters().put("drillUrl", drillUrl);
		
		logger.debug("drillUrl: " + drillUrl);
		
		logger.debug("out");
	}

	/**
	 * set parameters for the drill action.
	 * 
	 * @param content the content of the template.
	 * 
	 * @return A chart that displays a value as a dial.
	 */
	private String replaceParsInString(String strToRep, boolean isTitle) throws Exception {
		logger.debug("IN");
		
		if (strToRep == null) return "";
		
		String strRet = strToRep;
		
		logger.debug("String to replace: " + strToRep);
		int startIdx = strToRep.indexOf("$P{");
		int endIdx = strToRep.indexOf("}");
		boolean cleanTitle = true;
		
		while (startIdx != -1){
			if (startIdx > -1 && endIdx > -1){
				String namePar = strToRep.substring(startIdx+3, endIdx);
				String valuePar = (getDataParameters().get(namePar)==null)?"":(String)getDataParameters().get(namePar);
				if (isTitle){
					//for the title replaces the % char (it's doesn't viewed) and decode a multivalue list 
					//like a simple string list
					ParametersDecoder decoder = new ParametersDecoder();
				    List values = decoder.decode(valuePar);
				    String finalValuePar = "";
				    for (int i=0, l=values.size(); i<l; i++){
				    	finalValuePar += (values.get(i).equals("%25"))?"" : values.get(i);				    	
				    	if (i < l-1) finalValuePar += ", ";
				    	if (!finalValuePar.equals("")) cleanTitle = false;
				    }
					valuePar = finalValuePar.replaceAll("'","");				 
				}
				strRet = strRet.replace("$P{"+namePar+"}", valuePar);
			}
			strToRep = strRet; 
			startIdx = strToRep.indexOf("$P{");
			endIdx = strToRep.indexOf("}");
		}
				
		if (isTitle && cleanTitle) {
			//only for title: if the all parameters value are % the title value is cleaned (specific requirement).
			strRet = "";
			logger.debug("String title is cleaned becasue parameters containing only the '%' char! " );
		}
		logger.debug("String replaced: " + strRet);
		
		logger.debug("OUT");
		return strRet;
	}
	
	private void addDashboardConfigValues(int idx, SourceBean sbRow) throws Exception{
		LinkedHashMap singleConfParameters = new LinkedHashMap();
		String parValue = "";
		parValue = (sbRow.getAttribute("minValue")!=null)?(String)sbRow.getAttribute("minValue"):(String)sbRow.getAttribute("MINVALUE");
		if (parValue != null) singleConfParameters.put("minValue", parValue);
		parValue = (sbRow.getAttribute("maxValue")!=null)?(String)sbRow.getAttribute("maxValue"):(String)sbRow.getAttribute("MAXVALUE");
		if (parValue != null) singleConfParameters.put("maxValue", parValue);
		parValue = (sbRow.getAttribute("lowValue")!=null)?(String)sbRow.getAttribute("lowValue"):(String)sbRow.getAttribute("LOWVALUE");
		if (parValue != null) singleConfParameters.put("lowValue", parValue);
		parValue = (sbRow.getAttribute("highValue")!=null)?(String)sbRow.getAttribute("highValue"):(String)sbRow.getAttribute("HIGHVALUE");
		if (parValue != null) singleConfParameters.put("highValue", parValue);
		//parValue = (sbRow.getAttribute("highValue")!=null)?(String)sbRow.getAttribute("highValue"):(String)sbRow.getAttribute("HIGHVALUE");
		//if (parValue != null) singleConfParameters.put("colorArc1", parValue);
		parValue = (sbRow.getAttribute("colorArc1")!=null)?(String)sbRow.getAttribute("colorArc1"):(String)sbRow.getAttribute("COLORARC1");
		if (parValue != null) singleConfParameters.put("colorArc1", parValue);
		parValue = (sbRow.getAttribute("colorArc2")!=null)?(String)sbRow.getAttribute("colorArc2"):(String)sbRow.getAttribute("COLORARC2");
		if (parValue != null) singleConfParameters.put("colorArc2", parValue);		
		parValue = (sbRow.getAttribute("colorArc3")!=null)?(String)sbRow.getAttribute("colorArc3"):(String)sbRow.getAttribute("COLORARC3");
		if (parValue != null) singleConfParameters.put("colorArc3", parValue);
		parValue = (sbRow.getAttribute("valueDesc")!=null)?(String)sbRow.getAttribute("valueDesc"):(String)sbRow.getAttribute("VALUEDESC");
		if (parValue != null) singleConfParameters.put("valueDesc", parValue);		
		//defining needles configuration
		parValue = (sbRow.getAttribute("numNeedles")!=null)?(String)sbRow.getAttribute("numNeedles"):(String)sbRow.getAttribute("NUMNEEDLES");
		singleConfParameters.put("numNeedles",parValue);
	
		int numNeedles = 0;
		try{
			numNeedles = Integer.parseInt(parValue);
		}catch(Exception e){
			logger.error("error in reading configuration dataset. Number of needles is invalid." );
			throw new Exception("error in reading configuration dataset. Number of needles is invalid.");
		}
		
		for (int i=0 ; i < numNeedles; i++){				
			parValue = (sbRow.getAttribute("colorNeedle"+(i+1))!=null)?(String)sbRow.getAttribute("colorNeedle"+(i+1)):(String)sbRow.getAttribute("COLORNEEDLE"+(i+1));
			if (parValue != null) singleConfParameters.put("colorNeedle"+(i+1),parValue);
			parValue = (sbRow.getAttribute("value"+(i+1))!=null)?(String)sbRow.getAttribute("value"+(i+1)):(String)sbRow.getAttribute("VALUE"+(i+1));
			if (parValue != null) singleConfParameters.put("value"+(i+1), parValue);
		}
		confParameters.put("dash__" + idx, singleConfParameters);
	}
	
	
	private void addDefaultDashboardConfig() throws Exception{
		LinkedHashMap singleConfParameters = new LinkedHashMap();		
		singleConfParameters.put("minValue", "0");		
		singleConfParameters.put("maxValue", "100");		
		singleConfParameters.put("lowValue", "25");		
		singleConfParameters.put("highValue", "50");
		singleConfParameters.put("colorArc1", "0x9cff00");
		singleConfParameters.put("colorArc2", "0xfff999");		
		singleConfParameters.put("colorArc3", "0xff5454");
		singleConfParameters.put("valueDesc", "");		
		singleConfParameters.put("numNeedles", "1");
		singleConfParameters.put("colorNeedle1","red");
		singleConfParameters.put("value1","0");

		confParameters.put("dash__0", singleConfParameters);
	}
	
	private void addGenericConfigValues(SourceBean sbRow) throws Exception{
		String parValue = "";
		if (!confParameters.containsKey("multichart")){
			parValue = (sbRow.getAttribute("multichart")!=null)?(String)sbRow.getAttribute("multichart"):(String)sbRow.getAttribute("MULTICHART");
			if (parValue != null) confParameters.put("multichart", parValue);
		}
		if (!confParameters.containsKey("numCharts")){
			parValue = (sbRow.getAttribute("numCharts")!=null)?(String)sbRow.getAttribute("numCharts"):(String)sbRow.getAttribute("NUMCHARTS");
			if (parValue != null) confParameters.put("numCharts", parValue);
		}
		if (!confParameters.containsKey("orientation_multichart")){
			parValue = (sbRow.getAttribute("orientation_multichart")!=null)?(String)sbRow.getAttribute("orientation_multichart"):(String)sbRow.getAttribute("ORIENTATION_MULTICHART");
			if (parValue != null) confParameters.put("orientation_multichart", parValue);
		}
		if (!confParameters.containsKey("numChartsForRow")){
			parValue = (sbRow.getAttribute("numChartsForRow")!=null)?(String)sbRow.getAttribute("numChartsForRow"):(String)sbRow.getAttribute("NUMCHARTSFORROW");
			if (parValue != null) confParameters.put("numChartsForRow", parValue);
		}

		//defining title and legend variables	
		/*
		if (!confParameters.containsKey("displayTitleBar")){			
			parValue = (sbRow.getAttribute("displayTitleBar")!=null)?(String)sbRow.getAttribute("displayTitleBar"):(String)sbRow.getAttribute("DISPLAYTITLEBAR");
			if (parValue != null) confParameters.put("displayTitleBar", parValue);
		}
		*/
		if (!confParameters.containsKey("title")){
			parValue = (sbRow.getAttribute("title")!=null)?(String)sbRow.getAttribute("title"):(String)sbRow.getAttribute("TITLE");
			if (parValue != null) confParameters.put("title", parValue);
		}
		if (!confParameters.containsKey("colorTitle")){
			parValue = (sbRow.getAttribute("colorTitle")!=null)?(String)sbRow.getAttribute("colorTitle"):(String)sbRow.getAttribute("COLORTITLE");
			if (parValue != null) confParameters.put("colorTitle", parValue);
		}
		if (!confParameters.containsKey("sizeTitle")){
			parValue = (sbRow.getAttribute("sizeTitle")!=null)?(String)sbRow.getAttribute("sizeTitle"):(String)sbRow.getAttribute("SIZETITLE");
			if (parValue != null) confParameters.put("sizeTitle", parValue);
		}
		if (!confParameters.containsKey("fontTitle")){
			parValue = (sbRow.getAttribute("fontTitle")!=null)?(String)sbRow.getAttribute("fontTitle"):(String)sbRow.getAttribute("FONTTITLE");
			if (parValue != null) confParameters.put("fontTitle", parValue);
		}
		if (!confParameters.containsKey("colorTitleSerie")){
			parValue = (sbRow.getAttribute("colorTitleSerie")!=null)?(String)sbRow.getAttribute("colorTitleSerie"):(String)sbRow.getAttribute("COLORTITLESERIE");
			if (parValue != null) confParameters.put("colorTitleSerie", parValue);
		}
		if (!confParameters.containsKey("sizeTitleSerie")){
			parValue = (sbRow.getAttribute("sizeTitleSerie")!=null)?(String)sbRow.getAttribute("sizeTitleSerie"):(String)sbRow.getAttribute("SIZETITLESERIE");
			if (parValue != null) confParameters.put("sizeTitleSerie", parValue);
		}
		if (!confParameters.containsKey("fontTitleSerie")){
			parValue = (sbRow.getAttribute("fontTitleSerie")!=null)?(String)sbRow.getAttribute("fontTitleSerie"):(String)sbRow.getAttribute("FONTTITLESERIE");
			if (parValue != null) confParameters.put("fontTitleSerie", parValue);
		}
		if (!confParameters.containsKey("legend")){
			parValue = (sbRow.getAttribute("legend")!=null)?(String)sbRow.getAttribute("legend"):(String)sbRow.getAttribute("LEGEND");
			if (parValue != null) confParameters.put("legend", parValue);
		}
		if (!confParameters.containsKey("detailDesc")){
			parValue = (sbRow.getAttribute("detailDesc")!=null)?(String)sbRow.getAttribute("detailDesc"):(String)sbRow.getAttribute("DETAILDESC");
			if (parValue != null) confParameters.put("detailDesc", parValue);
		}		
	}
	
	/**
	 * @return the confParameters
	 */
	public Map getConfParameters() {
		return confParameters;
	}

	/**
	 * @param confParameters the confParameters to set
	 */
	public void setConfParameters(Map confParameters) {
		this.confParameters = confParameters;
	}

	/**
	 * @return the dataParameters
	 */
	public Map getDataParameters() {
		Map encodedDataParameters = encodePars(dataParameters);
		return encodedDataParameters;
	}

	/**
	 * @param dataParameters the dataParameters to set
	 */
	public void setDataParameters(Map dataParameters) {
		this.dataParameters = dataParameters;
	}

	/**
	 * @return the drillParameters
	 */
	public Map getDrillParameters() {
		return drillParameters;
	}

	/**
	 * @param drillParameters the drillParameters to set
	 */
	public void setDrillParameters(Map drillParameters) {
		this.drillParameters = drillParameters;
	}
}
