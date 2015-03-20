/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.kpi;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.InternalEngineIFace;
import it.eng.spagobi.engines.drivers.AbstractDriver;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.engines.kpi.bo.KpiLine;
import it.eng.spagobi.engines.kpi.bo.KpiLineVisibilityOptions;
import it.eng.spagobi.engines.kpi.bo.KpiResourceBlock;
import it.eng.spagobi.engines.kpi.utils.KpiEngineUtil;
import it.eng.spagobi.engines.kpi.utils.StyleLabel;
import it.eng.spagobi.kpi.config.bo.Kpi;
import it.eng.spagobi.kpi.config.bo.KpiDocuments;
import it.eng.spagobi.kpi.config.bo.KpiInstance;
import it.eng.spagobi.kpi.config.bo.KpiValue;
import it.eng.spagobi.kpi.config.dao.IKpiDAO;
import it.eng.spagobi.kpi.config.dao.IKpiErrorDAO;
import it.eng.spagobi.kpi.exceptions.MissingKpiValueException;
import it.eng.spagobi.kpi.model.bo.Model;
import it.eng.spagobi.kpi.model.bo.ModelInstanceNode;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrant;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrantNode;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitHierarchy;
import it.eng.spagobi.monitoring.dao.AuditManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.udp.bo.UdpValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * 
 * @author Chiara Chiarelli
 * 
 */

public class SpagoBIKpiInternalEngine extends AbstractDriver implements InternalEngineIFace {


	static transient Logger logger = Logger.getLogger(SpagoBIKpiInternalEngine.class);

	public static final String messageBundle = "MessageFiles.messages";

	protected static final String RESOURCE="RES_NAME";

	protected String name = "";// Document's title
	protected String subName = "";// Document's subtitle
	protected StyleLabel styleTitle;// Document's title style
	protected StyleLabel styleSubTitle;// Document's subtitle style
	protected String userIdField=null;

	public List resources;// List of resources linked to the
	// ModelInstanceNode

	protected Integer periodInstID = null;

	protected Integer modelInstanceRootId = null;

	public KpiTemplateConfiguration templateConfiguration = new KpiTemplateConfiguration(
			"KPI_DEFAULT_PUB", "KPI_METADATA_DEFAULT_PUB", "TREND_DEFAULT_PUB", false, "MODEL", null, null, null, null, null, false, true,
			false, false, false, false, true, false, false, false, false,
			false, null, null);
	
	public KpiEnginData data;	
	
	public KpiParametrization parameters = new KpiParametrization(
			new Date(), null, "default", null, null, null, null, null);
	
	protected KpiValueComputation computation; 
	// used to set the return of the execution
	protected List<KpiResourceBlock> kpiResultsList;
	

	//collection of OU available and valid
	private ArrayList<OrganizationalUnitGrantNode> ouList = new ArrayList<OrganizationalUnitGrantNode>();
	private String ouWarning = null;

	public HashMap confMap;


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getSubName() {
		return subName;
	}


	public void setSubName(String subName) {
		this.subName = subName;
	}

	//Method only called by a specific configuration of the scheduler created through the class KPIEngineJob.java
	public void executeByKpiEngineJob(RequestContainer requestContainer, SourceBean response) throws EMFUserError, SourceBeanException {
		logger.debug("IN");
		//setting locale, formats, profile
		this.data = KpiEngineUtil.setGeneralVariables(requestContainer);
		this.parameters.setParametersObject(new HashMap());

		String recalculate = (String)requestContainer.getAttribute("recalculate_anyway");
		if(recalculate.equals("true")){
			this.templateConfiguration.setRecalculate_anyway(true);
		}	
		// Date for which we want to see the KpiValues
		this.parameters.setDateOfKPI((Date)requestContainer.getAttribute("start_date"));
		this.parameters.setEndKpiValueDate((Date)requestContainer.getAttribute("end_date"));
		
		this.parameters.setVisibilityParameterValues((String)requestContainer.getAttribute("visibilityParameter"));

		String cascade = (String)requestContainer.getAttribute("cascade");	

		// **************take informations on the modelInstance and its KpiValues*****************
		String modelNodeInstance = (String) requestContainer.getAttribute("model_node_instance");
		logger.info("ModelNodeInstance : " + modelNodeInstance);

		if (modelNodeInstance == null) {
			logger.error("The modelNodeInstance specified in the template is null");
			throw new EMFUserError(EMFErrorSeverity.ERROR, "10106", messageBundle);
		}

		List kpiRBlocks = new ArrayList();// List of KpiValues Trees for each Resource: it will be sent to the jsp

		if(!parameters.getParametersObject().containsKey("ParKpiDate")){
			String dateForDataset = KpiEngineUtil.getDateForDataset(parameters.getDateOfKPI());	
			parameters.getParametersObject().put("ParKpiDate", dateForDataset);
		}

		// gets the ModelInstanceNode
		ModelInstanceNode mI = DAOFactory.getModelInstanceDAO().loadModelInstanceByLabel(modelNodeInstance, this.parameters.getDateOfKPI());
		logger.debug("ModelInstanceNode, ID=" + mI.getModelInstanceNodeId());
		modelInstanceRootId = mI.getModelInstanceNodeId();
		logger.debug("Loaded the modelInstanceNode with LABEL " + modelNodeInstance);

		if(templateConfiguration.isDataset_multires()){//if datasets return a value for each resource
			this.resources = mI.getResources(); //Set all the Resources for the Model Instance
			logger.info("Dataset multiresource");
			try {
				calculateAndInsertKpiValueWithResources(mI,this.resources);  	
			} catch (EMFInternalError e) {
				e.printStackTrace();
				logger.error("Error in calculateAndInsertKpiValueWithResources",e);
			}
			logger.info("Inserted all values!!");
			return;    	
		}

		// I set the list of resources of that specific ModelInstance
		if (this.resources == null || this.resources.isEmpty()) {
			this.resources = mI.getResources();
		}
		logger.debug("Setted the List of Resources related to the specified Model Instance");
		computation = new KpiValueComputation(this); 
		KpiLineVisibilityOptions options = KpiEngineUtil.setVisibilityOptions(this.templateConfiguration);

		if (cascade!=null && cascade.equals("true")){//in case all the kpi children have to be calculated too

			try {
				if (this.resources == null || this.resources.isEmpty()) {
					logger.debug("There are no resources assigned to the Model Instance");

					KpiResourceBlock block = new KpiResourceBlock();
					block.setD(this.parameters.getDateOfKPI());
					KpiLine line = getBlock(mI, null);				
					block.setRoot(line);
					block.setOptions(options);
					logger.debug("Setted the tree Root.");
					kpiRBlocks.add(block);
				} else {
					Iterator resourcesIt = this.resources.iterator();
					while (resourcesIt.hasNext()) {
						Resource r = (Resource) resourcesIt.next();
						logger.info("-------Resource: " + r.getName());
						KpiResourceBlock block = new KpiResourceBlock();
						block.setR(r);

				block.setD(parameters.getDateOfKPI());
						block.setOptions(options);
						KpiLine line = getBlock(mI, r);
						block.setRoot(line);
						logger.debug("Setted the tree Root.");
						kpiRBlocks.add(block);
					}
				}
			} catch (EMFInternalError e) {
				e.printStackTrace();
			}
		}else{//in case all the kpi children don't have to be calculated 
			try {
				KpiInstance kpiI = mI.getKpiInstanceAssociated();
				IDataSet dataSet = DAOFactory.getKpiDAO().getDsFromKpiId(kpiI.getKpi());
				KpiValue value = new KpiValue();
				if (this.resources == null || this.resources.isEmpty()) {
					logger.debug("There are no resources assigned to the Model Instance");
					logger.debug("Retrieved the Dataset to be calculated: " + dataSet.getId());
					value = computation.getNewKpiValue(dataSet, kpiI, null,mI.getModelInstanceNodeId(), null);
					logger.debug("New value calculated");
				} else {
					Iterator resourcesIt = this.resources.iterator();
					while (resourcesIt.hasNext()) {
						Resource r = (Resource) resourcesIt.next();
						logger.debug("Resource: " + r.getName());
						logger.debug("Retrieved the Dataset to be calculated: " + dataSet.getId());
						value = computation.getNewKpiValue(dataSet, kpiI, r,mI.getModelInstanceNodeId(), null);					
					}
				}
				logger.debug("New value calculated");
				// Insert new Value into the DB
				IKpiDAO dao=DAOFactory.getKpiDAO();
				dao.setUserProfile(data.getProfile());
				dao.insertKpiValue(value);
				logger.debug("New value inserted in the DB");		
				// Checks if the value is alarming (out of a certain range)
				// If the value is alarming a new line will be inserted in the sbi_alarm_event table and scheduled to be sent
				DAOFactory.getAlarmDAO().isAlarmingValue(value);   	
			} catch (EMFInternalError e) {
				e.printStackTrace();
			}
		}
		logger.debug("OUT");    	
	}


	/**
	 * Method used by basic execution and by the scheduler. Executes the document and populates the response.
	 * @param requestContainer
	 *                The <code>RequestContainer</code> object (the session
	 *                can be retrieved from this object)
	 * @param obj The <code>BIObject</code> representing the document to be executed
	 * @param response The response <code>SourceBean</code> to be populated
	 * @throws EMFUserError the EMF user error
	 */

	public void execute(RequestContainer requestContainer, BIObject obj, SourceBean response) throws EMFUserError {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("kpi.engines.SpagoBIKpiInternalEngine.execute");

		// AUDIT UPDATE
		Integer auditId = null;
		String auditIdStr = null;
		AuditManager auditManager = AuditManager.getInstance();
		
		if(requestContainer.getServiceRequest()!=null){
			auditIdStr = (String) requestContainer.getServiceRequest().getAttribute(AuditManager.AUDIT_ID);
			if (auditIdStr == null) {
			    logger.warn("Audit record id not specified! No operations will be performed");
			} else {
			    logger.debug("Audit id = [" + auditIdStr + "]");
			    auditId = new Integer(auditIdStr);
			}
			
			if (auditId != null) {
			    auditManager.updateAudit(auditId, new Long(System.currentTimeMillis()), null, "EXECUTION_STARTED", null,
				    null);
			}
		}
		
		ResponseContainer responseContainer = ResponseContainer.getResponseContainer();
		EMFErrorHandler errorHandler = responseContainer.getErrorHandler();
		//setting locale, formats, profile, parameters, startDate, endDate
		this.data = KpiEngineUtil.setGeneralVariables(requestContainer);

		if (obj == null) {
			logger.error("The input object is null.");
			throw new EMFUserError(EMFErrorSeverity.ERROR, "100", messageBundle);
		}
		if (!obj.getBiObjectTypeCode().equalsIgnoreCase("KPI")) {
			logger.error("The input object is not a KPI.");
			throw new EMFUserError(EMFErrorSeverity.ERROR, "1001", messageBundle);
		}
		String userId = null;

		if(data.getProfile()!=null){
			userId=(String) ((UserProfile) data.getProfile()).getUserId();
		}
		else{
			userId=userIdField;
		}

		String documentId = obj.getId().toString();
		logger.debug("Loaded documentId:" + documentId);		
		ModelInstanceNode mI = null;
		try {
			// **************get the template*****************		
			SourceBean content = KpiEngineUtil.getTemplate(documentId);
			logger.debug("Got the template.");

			// Date for which we want to see the KpiValues
			this.parameters.setDateOfKPI(new Date());
			this.parameters.setParametersObject(KpiEngineUtil.readParameters(obj.getBiObjectParameters(), this));
			addBIParameterDescriptions(obj, this.parameters.getParametersObject());
			
			if(!parameters.getParametersObject().containsKey("ParKpiDate")){
				String dateForDataset = KpiEngineUtil.getDateForDataset(parameters.getDateOfKPI());	
				parameters.getParametersObject().put("ParKpiDate", dateForDataset);
			}
			logger.debug("Got the date for which the KpiValues have to be calculated. Date:" + this.parameters.getDateOfKPI());

			// **************take informations on the modelInstance and its KpiValues*****************
			String modelNodeInstance = (String) content.getAttribute("model_node_instance");
			logger.info("ModelNodeInstance : " + modelNodeInstance);

			if (modelNodeInstance == null) {
				logger.error("The modelNodeInstance specified in the template is null");
				throw new EMFUserError(EMFErrorSeverity.ERROR, "10106", messageBundle);
			}
			String periodInstanceID = (String) content.getAttribute("periodicity_id");
			logger.debug("PeriodInstanceID : " + (periodInstanceID!=null ? periodInstanceID : "null"));

			if (periodInstanceID == null) {
				logger.debug("No periodInstID specified will use default one");
			}else{
				periodInstID = new Integer(periodInstanceID);
			}
			templateConfiguration.getSetConf(this, content);
			logger.debug("Setted the configuration of the template");

			List kpiRBlocks = new ArrayList();// List of KpiValues Trees for each Resource: it will be sent to the jsp

			// gets the ModelInstanceNode
			mI = DAOFactory.getModelInstanceDAO().loadModelInstanceByLabel(modelNodeInstance, this.parameters.getDateOfKPI());
			if (mI==null) {
				logger.error("MODEL INSTANCE IS NULL, CHECK model_node_instance IN DOCUMENT TEMPLATE.!!!!!!!!!!!!!!");
			}else {
				logger.debug("ModelInstanceNode, ID=" + (mI.getModelInstanceNodeId()!=null ? mI.getModelInstanceNodeId().toString():"null"));
				modelInstanceRootId = (mI.getModelInstanceNodeId()!=null ? mI.getModelInstanceNodeId() : null );
				logger.debug("Loaded the modelInstanceNode with LABEL " + modelNodeInstance);
			}

			if(templateConfiguration.isDataset_multires()){//if datasets return a value for each resource
				this.resources = mI.getResources(); //Set all the Resources for the Model Instance
				logger.info("Dataset multiresource");

				calculateAndInsertKpiValueWithResources(mI,this.resources);  	
				logger.info("Inserted all values!!");
				return;    	
			}

			// I set the list of resources of that specific ModelInstance
			if (this.resources == null || this.resources.isEmpty()) {
				this.resources = mI.getResources();
			}
			logger.debug("Setted the List of Resources related to the specified Model Instance");
			computation = new KpiValueComputation(this); 
			KpiLineVisibilityOptions options = KpiEngineUtil.setVisibilityOptions(this.templateConfiguration);

			//sets up register values
			//ModelInstanceNode modI = DAOFactory.getModelInstanceDAO().loadModelInstanceById(mI.getModelInstanceNodeId(), parameters.getDateOfKPI());

			logger.debug("Setted the List of Kpis that does not need to be persisted in db");
			if (this.resources == null || this.resources.isEmpty()) {
				logger.debug("There are no resources assigned to the Model Instance");
				KpiResourceBlock block = new KpiResourceBlock();
				block.setD(this.parameters.getDateOfKPI());
				block.setParMap(this.parameters.getParametersObject());
				KpiLine line = getBlock(mI, null);
				block.setRoot(line);
				block.setTitle(name);
				block.setSubtitle(subName);
				block.setOptions(options);
				logger.debug("Setted the tree Root.");
				kpiRBlocks.add(block);

			}else {
				Iterator resourcesIt = this.resources.iterator();
				while (resourcesIt.hasNext()) {
					Resource r = (Resource) resourcesIt.next();
					logger.info("Resource: " + r.getName());
					KpiResourceBlock block = new KpiResourceBlock();
					block.setR(r);
					block.setD(parameters.getDateOfKPI());
					block.setParMap(this.parameters.getParametersObject());
					KpiLine line = getBlock(mI, r);
					block.setRoot(line);
					block.setOptions(options);
					logger.debug("Setted the tree Root.");
					kpiRBlocks.add(block);
				}
			}

			try {
				logger.debug("Successfull kpis creation");
				String customChartName = templateConfiguration.getCustom_chart_name();
				if(customChartName != null){
					response.setAttribute("custom_chart_name", customChartName);
				}
				String tickInterval = templateConfiguration.getTickInterval();
				if(tickInterval != null){
					response.setAttribute("tickInterval", tickInterval);
				}

				response.setAttribute(ObjectsTreeConstants.SESSION_OBJ_ATTR, obj);
				response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, templateConfiguration.getPublisher_Name());
				response.setAttribute("metadata_publisher_Name", templateConfiguration.getMetadata_publisher_Name());
				response.setAttribute("trend_publisher_Name", templateConfiguration.getTrend_publisher_Name());

				if (name != null) {
					response.setAttribute("title", name);
				}
				if (styleTitle != null) {
					response.setAttribute("styleTitle", styleTitle);
				}				
				if (subName != null) {
					response.setAttribute("subName", subName);
				}
				if (styleSubTitle != null) {
					response.setAttribute("styleSubTitle", styleSubTitle);
				}				
				response.setAttribute("kpiRBlocks", kpiRBlocks);
				if (auditId!=null) response.setAttribute(AuditManager.AUDIT_ID, auditId);
				kpiResultsList = kpiRBlocks;
			} catch (Exception eex) {
				logger.error("Exception", eex);
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 10107);
				userError.setBundle("messages");
				throw userError;
			}
			logger.debug("OUT");
		} catch (EMFUserError e) {
			logger.error("User Error", e);
			errorHandler.addError(e);
			if(auditId!=null){
				auditManager.updateAudit(auditId, null, new Long(System.currentTimeMillis()), "EXECUTION_FAILED", e
					    .getMessage(), null);		
			   }
		} 	
		catch (Exception e) {
			EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 101);
			logger.error("Generic Error", e);
			errorHandler.addError(userError);	
			if(auditId!=null){
				auditManager.updateAudit(auditId, null, new Long(System.currentTimeMillis()), "EXECUTION_FAILED", e
					    .getMessage(), null);		
			   }

		} finally {
			monitor.stop();
		}
	}


	


	/**
	 * 
	 * @param 
	 * @param resources
	 * @throws EMFUserError
	 * @throws EMFInternalError
	 */
	public List<KpiResourceBlock> executeCode(RequestContainer requestContainer, BIObject obj, SourceBean response, String userId) throws EMFUserError {   
		logger.debug("IN");
		userIdField=userId;
		this.execute(requestContainer, obj, response);
		if(kpiResultsList==null){
			logger.error("error while executing KPI");
			return null;
		}
		else{
			logger.error("Kpi executed succesfully");
			logger.debug("OUT");
			return kpiResultsList;
		}

	}




	public void calculateAndInsertKpiValueWithResources(ModelInstanceNode modI,List resources)throws EMFUserError, EMFInternalError, SourceBeanException {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("kpi.engines.SpagoBIKpiInternalEngine.calculateAndInsertKpiValueWithResources");
		
		if (modI != null) {
			logger.info("Loaded Model Instance Node with id: " + modI.getModelInstanceNodeId());
		}

		List childrenIds = modI.getChildrenIds();
		if (!childrenIds.isEmpty()) {
			Iterator childrenIt = childrenIds.iterator();
			while (childrenIt.hasNext()) {
				Integer id = (Integer) childrenIt.next();	
				ModelInstanceNode modIF =  DAOFactory.getModelInstanceDAO().loadModelInstanceById(id, parameters.getDateOfKPI());
				calculateAndInsertKpiValueWithResources(modIF, resources);
			}
		}
		KpiInstance kpiI = modI.getKpiInstanceAssociated();
		if (kpiI != null) {
			KpiValue kVal = new KpiValue();
			logger.info("Got KpiInstance with ID: " + kpiI.getKpiInstanceId().toString());

			
			IDataSet dataSet = DAOFactory.getKpiDAO().getDsFromKpiId(kpiI.getKpi());	
			logger.info("Retrieved the Dataset to be calculated: " + (dataSet!=null ? dataSet.getId():"null"));
			Integer kpiInstanceID = kpiI.getKpiInstanceId();
			Date kpiInstBegDt = kpiI.getD();

			kVal = computation.setTimeAttributes(kVal, kpiI);		
			kVal.setKpiInstanceId(kpiInstanceID);
			logger.debug("Setted the KpiValue Instance ID:"+kpiInstanceID);	

			if ( (parameters.getDateOfKPI().after(kpiInstBegDt)||parameters.getDateOfKPI().equals(kpiInstBegDt))) {
				//kpiInstance doesn't change
			}else{
				KpiInstance tempKIn = DAOFactory.getKpiInstanceDAO().loadKpiInstanceByIdFromHistory(kpiInstanceID,parameters.getDateOfKPI());
				if(tempKIn==null){//kpiInstance doesn't change
				}else{
					// in case older thresholds have to be retrieved
					kpiI = tempKIn;
				}
			}
			Kpi kpi = DAOFactory.getKpiDAO().loadKpiById(kpiI.getKpi());
			
			kVal = computation.getFromKpiInstAndSetKpiValueAttributes(kpiI,kVal, kpi);

			// If it has to be calculated for a Resource. The resource will be set as parameter
			HashMap temp = (HashMap) this.parameters.getParametersObject().clone();
			temp.put("ParModelInstance", modI.getModelInstanceNodeId());	
			temp.put("ParKpiInstance", kpiInstanceID.toString());
			// If not, the dataset will be calculated without the parameter Resource
			// and the DataSet won't expect a parameter of type resource
			//if(dataSet.hasBehaviour( QuerableBehaviour.class.getName()) ) {
			if(dataSet!=null){
				
				//if parameter exists and OU is abilitaded for Model Instance, than calculate as dataset parameter
				String parKpiOuLabel = (String)this.parameters.getParametersObject().get("ParKpiOU");
				logger.info("Got ParKpiOU: " + parKpiOuLabel);
				
				String paramLabelHierarchy = (String)this.parameters.getParametersObject().get("ParKpiHierarchy");
				logger.info("Got ParKpiHierarchy: " + paramLabelHierarchy);
				setOUAbilitated(modI.getModelInstanceNodeId(), parKpiOuLabel, paramLabelHierarchy);
				
				if(ouList != null && !ouList.isEmpty()){
					if(templateConfiguration.isUse_ou()){
						for(int i = 0; i<ouList.size(); i++){
							OrganizationalUnitGrantNode grantNode = ouList.get(i);
							String ouLabel = grantNode.getOuNode().getOu().getLabel();
							String hierLabel = grantNode.getOuNode().getHierarchy().getLabel();
							if(parKpiOuLabel == null){
								this.parameters.getParametersObject().put("ParKpiOU", ouLabel);
								this.parameters.getParametersObject().put("ParKpiHierarchy", hierLabel);
							}

							kVal.setGrantNodeOU(grantNode);
							kVal = computation.recursiveGetKpiValueFromKpiRel(kpi,dataSet,temp,kVal,parameters.getDateOfKPI(),kVal.getEndDate(), modI.getModelInstanceNodeId());
							kVal = computation.getKpiValueFromDataset(dataSet,temp,kVal,parameters.getDateOfKPI(),kVal.getEndDate(), true, modI.getModelInstanceNodeId());
							if(ouWarning != null && kVal.getValue() == null){
								kVal.setValueDescr(ouWarning);		
							}
							if(parKpiOuLabel == null){
								this.parameters.getParametersObject().remove("ParKpiOU");
								this.parameters.getParametersObject().remove("ParKpiHierarchy");
							}
						}
					}else{
						kVal = computation.recursiveGetKpiValueFromKpiRel(kpi,dataSet,temp,kVal,parameters.getDateOfKPI(),kVal.getEndDate(), modI.getModelInstanceNodeId());
						kVal = computation.getKpiValueFromDataset(dataSet,temp,kVal,parameters.getDateOfKPI(),kVal.getEndDate(), true, modI.getModelInstanceNodeId());
					}
				}else{				
					if(templateConfiguration.isUse_ou()){		
						kVal = new KpiValue();
						kVal.setValueDescr(ouWarning);	
						kVal.setKpiInstanceId(kpiI.getKpiInstanceId());
			            if(parameters.getDateIntervalFrom()!=null && parameters.getDateIntervalTo()!=null){
			            	kVal.setBeginDate(parameters.getDateIntervalFrom());
			            	kVal.setEndDate(parameters.getDateIntervalTo());
		                } else{
		                	kVal.setBeginDate(parameters.getDateOfKPI());
		                	kVal.setEndDate(parameters.getDateOfKPI());						
		                }
						kVal.setValue(null);
					}else{
						kVal = computation.recursiveGetKpiValueFromKpiRel(kpi,dataSet,temp,kVal,parameters.getDateOfKPI(),kVal.getEndDate(), modI.getModelInstanceNodeId());
						kVal = computation.getKpiValueFromDataset(dataSet,temp,kVal,parameters.getDateOfKPI(),kVal.getEndDate(), true, modI.getModelInstanceNodeId());
						
					}	

				}

			}
		} 
		monitor.stop();
		logger.debug("OUT");
	}

	private void setKpiTrend(KpiLine kpiLine, ModelInstanceNode node){
		Monitor monitor = MonitorFactory.start("kpi.engines.SpagoBIKpiInternalEngine.setKpiTrend");

		KpiValue value = kpiLine.getValue();
		if (value == null ) return;
		Integer modelInstId = kpiLine.getModelInstanceNodeId();
		try {
			KpiInstance kpiInst= node.getKpiInstanceAssociated();
			if(kpiInst != null){
				Integer kpiInstId = kpiInst.getKpiInstanceId();
				Integer trend = DAOFactory.getKpiDAO().getKpiTrend(null, kpiInstId, value.getBeginDate());
				kpiLine.setTrend(trend);
				
			}
			
		} catch (Exception e) {
			logger.error("Error retrieving modelinstance "+modelInstId, e);
		}finally{
			monitor.stop();
		}
	}
	
	public boolean isVisible(KpiLine kpiLine, Model model){
		boolean visible = false;
		if(!this.parameters.getParametersObject().containsKey("visibilityParameter")){
			return true;
		}
		Integer modelInstId = kpiLine.getModelInstanceNodeId();

		List<UdpValue> udps = model.getUdpValues();
		if(udps != null){
			for(int i=0; i<udps.size(); i++){
				UdpValue udpVal = udps.get(i);
				String udpName = udpVal.getName();
				if(udpName.equals("VISIBILITY")){
					String val = udpVal.getValue();
					if(val != null && !val.equals("")){
						//can be multivalue with 'aa','bb','cc'...format
						String [] multival = val.split(",");
						if(multival.length != 0){
							for(int k = 0; k< multival.length; k++){
								String v = multival[k].replaceAll("'", "").trim();
								logger.debug(v+"-"+this.parameters.getVisibilityParameterValues());
								if(this.parameters.getVisibilityParameterValues().equals(v)){
									visible = true;
								}
							}
						}else{
							//single value
							if(this.parameters.getVisibilityParameterValues().equals(val)){
								visible = true;									
							}
						}
					}
				}

			}
			logger.debug("if udp is present passes a upd name = parameter name to dataset, by ading it to HashMap pars");
		}
				
		
		return visible;
	}
	
	private void setGUIInformation(KpiLine line,
					KpiInstance kpiI, 
					ModelInstanceNode node,
					Kpi k) throws EMFUserError{
		//add information needed by the new GUI 
		setKpiTrend(line,node);
		line.setKpi(k);
		line.setKpiInstId(kpiI.getKpiInstanceId());

	}
	private void setVisibilityInformation(KpiLine line,
			ModelInstanceNode modI) throws EMFUserError{
		Model model = DAOFactory.getModelDAO().loadModelOnlyPropertiesById(modI.getModelNodeId());
		
		boolean isVisible = isVisible(line, model);
		if(!isVisible){
			line.setVisible(false);				
		}
	}
	public KpiLine getBlock(ModelInstanceNode modI, Resource r) throws EMFUserError, EMFInternalError, SourceBeanException {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("kpi.engines.SpagoBIKpiInternalEngine.getBlock");
		KpiLine line = new KpiLine();
		
		if (modI != null) {
			logger.info("Loaded Model Instance Node with id: " + modI.getModelInstanceNodeId());
		}
		String modelNodeName = modI.getName();
		line.setModelNodeName(modelNodeName);
		line.setModelInstanceNodeId(modI.getModelNodeId());
		line.setModelInstanceCode(modI.getModelCode());
		setVisibilityInformation(line, modI);
		
		List<KpiLine> children = new ArrayList<KpiLine>();
		List childrenIds = modI.getChildrenIds();
		if (!childrenIds.isEmpty()) {
			Iterator childrenIt = childrenIds.iterator();
			while (childrenIt.hasNext()) {
				Integer id = (Integer) childrenIt.next();	
				ModelInstanceNode modIF = DAOFactory.getModelInstanceDAO().loadModelInstanceById(id, parameters.getDateOfKPI());
				KpiLine childrenLine = getBlock(modIF, r);
				if(childrenLine != null){
					children.add(childrenLine);
				}
			}
		}
		
		Comparator<KpiLine> comparator = new Comparator<KpiLine>() {
		    public int compare(KpiLine c1, KpiLine c2) {
		        return c1.getModelNodeName().compareToIgnoreCase(c2.getModelNodeName());
		    }
		};
		Collections.sort(children, comparator); // use the comparator as much as u want
		logger.debug("Nodes list succesfully ordered. Sorted nodes are..");
		for(KpiLine kl : children) {
			logger.debug(">>> Nodes [" + kl.getModelNodeName() + "]");
		}

		KpiInstance kpiI = modI.getKpiInstanceAssociated();
		//if true the kpi value will always use the display behaviour
		boolean alreadyExistent = false;

		if (kpiI == null && modI.getModelInstaceReferenceLabel() != null){
			ModelInstanceNode modelInstanceRefered = DAOFactory.getModelInstanceDAO().loadModelInstanceByLabel(modI.getModelInstaceReferenceLabel(), parameters.getDateOfKPI());
			alreadyExistent = true;
			if (modelInstanceRefered != null && modelInstanceRefered.getKpiInstanceAssociated() != null){
				kpiI = modelInstanceRefered.getKpiInstanceAssociated();
				modI.setKpiInstanceAssociated(kpiI);
			}
		}		

		line.setChildren(children);
		if (kpiI != null) {
			Integer kpiInstID = kpiI.getKpiInstanceId();
			logger.info("Got KpiInstance with ID: " + kpiInstID.toString());
			KpiValue value = null;
			Integer kpiId = kpiI.getKpi();
			Kpi k = DAOFactory.getKpiDAO().loadKpiById(kpiId);
			
			logger.debug("checks for udp related to kpi");
			ArrayList<UdpValue> udps = (ArrayList<UdpValue>)k.getUdpValues();
			if(udps != null){
				for(int i=0; i<udps.size(); i++){
					UdpValue udpVal = udps.get(i);
					String udpParameterNameForDataset = udpVal.getName();
					String udpParameterValueForDataset = udpVal.getValue();
					this.parameters.getParametersObject().put(udpParameterNameForDataset, udpParameterValueForDataset);
				}
				logger.debug("if udp is present passes a upd name = parameter name to dataset, by ading it to HashMap pars");
			}
			line = retrieveKpiLine(line, value, kpiI, modI.getModelInstanceNodeId(), r, alreadyExistent);

			setGUIInformation(line, kpiI, modI, k);
			
			logger.debug("Retrieved the kpi with id: " + kpiId.toString());
						
			if (k != null) {
				List docs = k.getSbiKpiDocuments();

				Iterator it = docs.iterator();
				List documents = new ArrayList();
				while(it.hasNext()){
					KpiDocuments doc = (KpiDocuments)it.next();
					String docLabel = doc.getBiObjLabel();
					if (docLabel != null && !docLabel.equals("")) {						
						logger.debug("Retrieved documents associated to the KPI");
						documents.add(docLabel);						
					}
				}
				line.setDocuments(documents);

			}
			if (templateConfiguration.isDisplay_alarm() && value!=null && value.getValue()!= null) {
				Boolean alarm = DAOFactory.getKpiInstanceDAO().isKpiInstUnderAlramControl(kpiInstID);
				logger.debug("KPI is under alarm control: " + alarm.toString());
				line.setAlarm(alarm);
			}

		}
		monitor.stop();
		logger.debug("OUT");
		return line;
	}
	private KpiLine retrieveKpiLine(KpiLine line,KpiValue value, KpiInstance kpiI, Integer miId, Resource r, boolean alreadyExistent) throws EMFUserError, EMFInternalError, SourceBeanException{
		//if parameter exists and OU is abilitaded for Model Instance, than calculate as dataset parameter
		
		Monitor monitor = MonitorFactory.start("kpi.engines.SpagoBIKpiInternalEngine.retrieveKpiLine");
		String parKpiOuLabel = (String)this.parameters.getParametersObject().get("ParKpiOU");
		logger.info("Got ParKpiOU: " + parKpiOuLabel);
		String paramLabelHierarchy = (String)this.parameters.getParametersObject().get("ParKpiHierarchy");
		logger.info("Got ParKpiHierarchy: " + paramLabelHierarchy);
		setOUAbilitated(miId, parKpiOuLabel, paramLabelHierarchy);
		

		if(ouList != null && !ouList.isEmpty()){
			if(templateConfiguration.isUse_ou()){
				for(int i = 0; i<ouList.size(); i++){
					OrganizationalUnitGrantNode grantNode = (OrganizationalUnitGrantNode)ouList.get(i);
					String ouLabel =grantNode.getOuNode().getOu().getLabel();
					String hierLabel =grantNode.getOuNode().getHierarchy().getLabel();
					if(parKpiOuLabel == null){
						this.parameters.getParametersObject().put("ParKpiOU", ouLabel);
						this.parameters.getParametersObject().put("ParKpiHierarchy", hierLabel);
					}
					value = getValueDependingOnBehaviour(kpiI, miId, r, alreadyExistent , grantNode);
					line.setValue(value);
					if(ouWarning != null && value.getValue() == null){
						value.setValueDescr(ouWarning);		
					}
					if(parKpiOuLabel == null){
						this.parameters.getParametersObject().remove("ParKpiOU");
					}
				}
			}else{
				value = getValueDependingOnBehaviour(kpiI, miId, r,alreadyExistent , null);//standard behaviour	
				line.setValue(value);
			}
		}else{				
			if(templateConfiguration.isUse_ou()){		
				value = new KpiValue();
				value.setValueDescr(ouWarning);	
				value.setKpiInstanceId(kpiI.getKpiInstanceId());
	            if(parameters.getDateIntervalFrom()!=null && parameters.getDateIntervalTo()!=null){
	            	value.setBeginDate(parameters.getDateIntervalFrom());
	            	value.setEndDate(parameters.getDateIntervalTo());
                } else{
					value.setBeginDate(parameters.getDateOfKPI());
					value.setEndDate(parameters.getDateOfKPI());						
                }
	            value.setValue(null);
			}else{
				value = getValueDependingOnBehaviour(kpiI, miId, r,alreadyExistent , null);//standard behaviour	
				
			}
			line.setValue(value);			

		}
		monitor.stop();
		return line;
	}
	/**Method created to fill ouList class attribute, each time a model instance node is examined.
	 * If execution modality is document execution: 
	 * * if node has grants but no document parameter ParKpiOU is passed, than warning (dataset will fail)
	 * * if node has grants but ParKpiOU = another organizational unit not granted for the node, than warning (dataset will fail)
	 * * if node has grants and ParKpiOU = granted organizational unit for the node, than add this org unit to the ouList
	 * * if node doesn't have grants, than standard flow no elements in ouList, no warnings
	 * 
	 * If execution modality is scheduled execution: 
	 * * if no ParKpiOU document parameter is passed, than all granted ou are added to ouList and kpi value will be calculated for each ou, generating a record on db
	 * * if ParKpiOU document parameter = one ou, than the behaviour is the same as document execution modality.
	 * @param miId
	 * @param paramLabelOU
	 */
	private void setOUAbilitated(Integer miId, String paramLabelOU, String paramLabelHierarchy){
		//if paramLabelOU doesn't exist, then scheduling mode
		//else document execution mode
		Monitor monitor = MonitorFactory.start("kpi.engines.SpagoBIKpiInternalEngine.setOUAbilitated");
		ouList = new ArrayList<OrganizationalUnitGrantNode>();
		ouWarning = null;
		//looks up for OU grants
		logger.debug("this is hierarchy:"+paramLabelHierarchy);
		List<OrganizationalUnitGrantNode> grants = DAOFactory.getOrganizationalUnitDAO().getGrantsValidByDate(miId, parameters.getDateOfKPI());
		if(grants != null){
			for(int i = 0; i<grants.size(); i++){				
				OrganizationalUnitGrantNode grantNode = grants.get(i);
				OrganizationalUnitGrant grant = grantNode.getGrant();				
				OrganizationalUnitHierarchy hier = grant.getHierarchy();
				if(paramLabelOU == null){
					//scheduling mode 
					if(data.isExecutionModalityScheduler()){
						//than all OU valid by date are filling
						if(hier.getLabel().equalsIgnoreCase(paramLabelHierarchy)){
							ouList.add(grantNode);
							ouWarning = null;
						}

					}else{
						ouWarning = "Warning: kpi needed ParKpiOU parameter.";
					}	
				}else{
					boolean found = behaveLikeDocumentExecution(paramLabelOU, paramLabelHierarchy, grantNode);
					if(!found){
						if(i == grants.size()-1){//last choice
							ouWarning = "Warning: wrong OU/Hierarchy passed as parameters.";
						}else{
							ouWarning = null;
						}
						
					}else{
						break;
					}
				}

			}
		}
		monitor.stop();
	}
	//if parameter is passed, than checks if it is granted for the node
	private boolean behaveLikeDocumentExecution(String paramLabelOU, String paramLabelHierarchy , OrganizationalUnitGrantNode grantNode) {
		boolean found = false;
		//document execution mode with parameter OU
		String hierarchyLabel = grantNode.getOuNode().getHierarchy().getLabel();
		String oulabel = grantNode.getOuNode().getOu().getLabel();
		if(oulabel.equals(paramLabelOU) && hierarchyLabel.equals(paramLabelHierarchy)){
			ouList.add(grantNode);
			ouWarning = null;
			found = false;
		}
		return found;
	}
	private KpiValue getValueDependingOnBehaviour(KpiInstance kpiI,Integer miId, Resource r, boolean alreadyExistent, OrganizationalUnitGrantNode grantNode) throws EMFUserError, EMFInternalError, SourceBeanException{
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("kpi.engines.SpagoBIKpiInternalEngine.getValueDependingOnBehaviour");
		KpiValue value = new KpiValue();
		boolean no_period_to_period = false;
		OrganizationalUnitGrantNode grantNodeToUse = null;
		if(grantNode != null && templateConfiguration.isUse_ou()){
			grantNodeToUse = grantNode;
		}
		if(alreadyExistent){//use diplay behaviour since value already exists
			logger.debug("Display behaviour");
			value = DAOFactory.getKpiDAO().getDisplayKpiValue(kpiI.getKpiInstanceId(), parameters.getDateOfKPI(), r, grantNodeToUse);
			Kpi kpi = DAOFactory.getKpiDAO().loadKpiById(kpiI.getKpi());			
			value = computation.getFromKpiInstAndSetKpiValueAttributes(kpiI, value, kpi);
			logger.debug("Old KpiValue retrieved it could be still valid or not");
		}else{	
			if(parameters.getBehaviour().equalsIgnoreCase("timeIntervalDefault")){
				if(parameters.getDateIntervalFrom()!=null && parameters.getDateIntervalTo()!=null){
					value = DAOFactory.getKpiDAO().getKpiValueFromInterval(kpiI.getKpiInstanceId(), parameters.getDateIntervalFrom(), parameters.getDateIntervalTo(), r, grantNodeToUse);
					if (value==null){
						IDataSet dataSet = DAOFactory.getKpiDAO().getDsFromKpiId(kpiI.getKpi());	
						logger.info("Retrieved the Dataset to be calculated: " + (dataSet!=null ? dataSet.getId():"null"));
						value = computation.getNewKpiValue(dataSet, kpiI, r, miId, grantNodeToUse);		
					}
				}else{
					value = null;
				}
			}else if(parameters.getBehaviour().equalsIgnoreCase("timeIntervalForceRecalculation")){
				if(parameters.getDateIntervalFrom()!=null && parameters.getDateIntervalTo()!=null){
					DAOFactory.getKpiDAO().deleteKpiValueFromInterval(kpiI.getKpiInstanceId(), parameters.getDateIntervalFrom(), parameters.getDateIntervalTo(), r, grantNodeToUse);
					IDataSet dataSet = DAOFactory.getKpiDAO().getDsFromKpiId(kpiI.getKpi());	
					logger.info("Retrieved the Dataset to be calculated: " + (dataSet!=null ? dataSet.getId():"null"));
					value = computation.getNewKpiValue(dataSet, kpiI, r, miId, grantNodeToUse);		
				}else{
					value = null;
				}
			}else{
				if(parameters.getBehaviour().equalsIgnoreCase("default") || //If the behaviour is default
						(parameters.getBehaviour().equalsIgnoreCase("recalculate") && kpiI.getPeriodicityId()!=null)){//or the behaviour is recalculate and the kpiinstance has a setted periodicity
					// the old value still valid is retrieved
					logger.debug("Trying to retrieve the old value still valid");
					value = DAOFactory.getKpiDAO().getKpiValue(kpiI.getKpiInstanceId(), parameters.getDateOfKPI(), r, grantNodeToUse);
					logger.debug("Old KpiValue retrieved");		
				}
				if(value!=null && value.getEndDate()!=null && kpiI.getPeriodicityId()!=null){
					GregorianCalendar c2 = new GregorianCalendar();		
					c2.setTime(value.getEndDate());
					int year = c2.get(1);
					if(year==9999){
						no_period_to_period = true;
						logger.debug("The periodicity was null and now exists");
					}
				}
				if (value==null || //If the retrieved value is null
						no_period_to_period || //or the periodicity was before null and now is setted
						parameters.getBehaviour().equalsIgnoreCase("force_recalculation") || //or the  behaviour is force_recalculation
						(parameters.getBehaviour().equalsIgnoreCase("recalculate") && kpiI.getPeriodicityId()==null) ) {// or the behaviour is recalculate and the kpiinstance hasn't a setted periodicity
					//a new value is calculated
					logger.debug("Old value not valid anymore or recalculation forced");
					IDataSet dataSet = DAOFactory.getKpiDAO().getDsFromKpiId(kpiI.getKpi());	
					logger.info("Retrieved the Dataset to be calculated: " + (dataSet!=null ? dataSet.getId():"null"));
					value = computation.getNewKpiValue(dataSet, kpiI, r, miId, grantNodeToUse);		

				}else if(parameters.getBehaviour().equalsIgnoreCase("display")){//diplay behaviour
					logger.debug("Display behaviour");
					value = DAOFactory.getKpiDAO().getDisplayKpiValue(kpiI.getKpiInstanceId(), parameters.getDateOfKPI(), r, grantNodeToUse);
					Kpi kpi = DAOFactory.getKpiDAO().loadKpiById(kpiI.getKpi());			
					value = computation.getFromKpiInstAndSetKpiValueAttributes(kpiI, value, kpi);
					logger.debug("Old KpiValue retrieved it could be still valid or not");
				} 
			}
		}
		monitor.stop();
		logger.debug("OUT");
		return value;
	}

	

	protected KpiValue setKpiValuesFromDataset(KpiValue kpiValueToReturn, List fields,IMetaData d, 
			Date begD, Date endDate, String datasetLabel,
			Integer modInstId, KpiValue kpiVal) throws EMFUserError, SourceBeanException{
		
		Monitor monitor = MonitorFactory.start("kpi.engines.SpagoBIKpiInternalEngine.setKpiValuesFromDataset");
		int length = fields.size();
		String xmlData = null;
		String tempXMLroot = "<XML_DATA></XML_DATA>";
		SourceBean dsValuesXml = SourceBean.fromXMLString(tempXMLroot);
		boolean valueFound = false;
		for(int fieldIndex =0; fieldIndex<length; fieldIndex++){

			IField f = (IField)fields.get(fieldIndex);			
			if (f != null) {
				if (f.getValue() != null) {
					String fieldName = d.getFieldAlias(fieldIndex);	  
					if (fieldName.equalsIgnoreCase("DESCR")){
						String descr = f.getValue().toString();
						kpiValueToReturn.setValueDescr(descr);
						logger.debug("Setted the kpiValue description:"+descr);
					}else if(fieldName.equalsIgnoreCase("END_DATE")){
						String endD = f.getValue().toString();
						String format = "dd/MM/yyyy hh:mm:ss";
						SimpleDateFormat form = new SimpleDateFormat();
						form.applyPattern(format);
						try {
							endDate = form.parse(endD);
						} catch (ParseException e) {
							e.printStackTrace();
						}
						if(endDate!=null && endDate.after(begD)) {				 
							kpiValueToReturn.setEndDate(endDate);
							logger.debug("Setted the new EndDate description:"+endD.toString());
						}
					}else if(fieldName.equalsIgnoreCase("VALUE")){
						String fieldValue = f.getValue().toString();
						kpiValueToReturn.setValue(fieldValue);
						logger.debug("Setted the kpiValue value:"+fieldValue);
						valueFound = true;
					}   
					else if(fieldName.equalsIgnoreCase("XML_DATA")){
						xmlData = f.getValue().toString();
						kpiValueToReturn.setValueXml(xmlData);
						logger.debug("Setted the kpiValue xmlData:"+xmlData);
					}   
					else if(fieldName.equalsIgnoreCase(RESOURCE)){
						String fieldValue = f.getValue().toString();
						if (fieldValue!=null){
							Resource rTemp = DAOFactory.getResourceDAO().loadResourcesByNameAndModelInst(fieldValue);
							kpiValueToReturn.setR(rTemp);
							logger.info("Setted the kpiValue Resource with resource name:"+fieldValue);
						}
					}/*else if(fieldName.equalsIgnoreCase("ORG_UNIT_ID")){
						String fieldValue = f.getValue().toString();
						if (fieldValue!=null){
							Resource rTemp = DAOFactory.getResourceDAO().loadResourcesByNameAndModelInst(fieldValue);
							kpiValueToReturn.setR(rTemp);
							logger.info("Setted the kpiValue Resource with resource name:"+fieldValue);
						}
					}*/ else{
						String fieldValue = f.getValue().toString();
						if (fieldValue!=null){
							dsValuesXml.setAttribute(fieldName, fieldValue);
						}
					}
				}
			}
		}
		if(xmlData==null && dsValuesXml!=null){
			xmlData = dsValuesXml.toXML(false);
			kpiValueToReturn.setValueXml(xmlData);
			logger.debug("Setted the kpiValue xmlData:"+xmlData);
		}

		if (kpiValueToReturn == null && valueFound == true){
			logger.error("cjheck dataset "+datasetLabel+ " because no value field for kpi was found");
			MissingKpiValueException missingKpiValueException = new MissingKpiValueException("cjheck dataset "+datasetLabel+ " because no value field for kpi was found");			
			
			IKpiErrorDAO dao= DAOFactory.getKpiErrorDAO();
			dao.setUserProfile(data.getProfile());
			dao.insertKpiError(
					missingKpiValueException, 
					modInstId, 
					kpiVal.getR() != null ? kpiVal.getR().getName() : null );
		}
		monitor.stop();
		return kpiValueToReturn;
	}


		/**
	 * The <code>SpagoBIDashboardInternalEngine</code> cannot manage
	 * subobjects so this method must not be invoked. 
	 * @param requestContainer
	 *                The <code>RequestContainer</code> object (the session
	 *                can be retrieved from this object)
	 * @param obj The <code>BIObject</code> representing the document
	 * @param response The response <code>SourceBean</code> to be populated
	 * @throws EMFUserError the EMF user error
	 */
	public void executeSubObject(RequestContainer requestContainer, BIObject obj, SourceBean response,
			Object subObjectInfo) throws EMFUserError {
		// it cannot be invoked
		logger.error("SpagoBIKpiInternalEngine cannot exec subobjects.");
		throw new EMFUserError(EMFErrorSeverity.ERROR, "101", messageBundle);
	}


	/**
	 * Function not implemented. Thid method should not be called
	 * @param requestContainer
	 *                The <code>RequestContainer</code> object (the session
	 *                can be retrieved from this object)
	 * @param response The response <code>SourceBean</code> to be populated
	 * @param obj the obj
	 * @throws InvalidOperationRequest the invalid operation request
	 * @throws EMFUserError
	 *                 the EMF user error
	 */
	public void handleNewDocumentTemplateCreation(RequestContainer requestContainer, BIObject obj, SourceBean response)
	throws EMFUserError, InvalidOperationRequest {
		logger.error("SpagoBIDashboardInternalEngine cannot build document template.");
		throw new InvalidOperationRequest();

	}

	/**
	 * Function not implemented. Thid method should not be called
	 * @param requestContainer
	 *                The <code>RequestContainer</code> object (the session
	 *                can be retrieved from this object)
	 * @param response The response <code>SourceBean</code> to be populated
	 * @param obj the obj
	 * @throws InvalidOperationRequest the invalid operation request
	 * @throws EMFUserError the EMF user error
	 */
	public void handleDocumentTemplateEdit(RequestContainer requestContainer, BIObject obj, SourceBean response)
	throws EMFUserError, InvalidOperationRequest {
		logger.error("SpagoBIDashboardInternalEngine cannot build document template.");
		throw new InvalidOperationRequest();
	}
}