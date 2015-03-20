/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.module.list.basic.AbstractBasicListModule;
import it.eng.spago.navigation.LightNavigationManager;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spago.paginator.basic.PaginatorIFace;
import it.eng.spago.paginator.basic.impl.GenericList;
import it.eng.spago.paginator.basic.impl.GenericPaginator;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.functionalitytree.service.TreeObjectsModule;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.DelegatedBasicListService;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

public class ListBIObjectsModule extends AbstractBasicListModule {
    private static transient Logger logger = Logger.getLogger(ListBIObjectsModule.class);
    protected IEngUserProfile profile = null;
    protected String initialPath = null;
    protected Locale locale=null;

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.spago.dispatching.service.list.basic.IFaceBasicListService#getList(it.eng.spago.base.SourceBean,
     *      it.eng.spago.base.SourceBean)
     */
    public ListIFace getList(SourceBean request, SourceBean response) throws Exception {
	logger.debug("IN");
	RequestContainer requestContainer = this.getRequestContainer();
	SessionContainer sessionContainer = requestContainer.getSessionContainer();
	SessionContainer permanentSession = sessionContainer.getPermanentContainer();
	profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	initialPath = (String) request.getAttribute(TreeObjectsModule.PATH_SUBTREE);

	String language=(String)permanentSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
	String country=(String)permanentSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
	if(language!=null && country!=null)locale=new Locale(language,country,"");
	else locale=GeneralUtilities.getDefaultLocale();
	
	String currentFieldOrder = (request.getAttribute("FIELD_ORDER") == null || ((String) request
		.getAttribute("FIELD_ORDER")).equals("")) ? "" : (String) request.getAttribute("FIELD_ORDER");
	if (currentFieldOrder.equals("")) {
	    currentFieldOrder = "DESCR";
	    response.delAttribute("FIELD_ORDER");
	    response.setAttribute("FIELD_ORDER", currentFieldOrder);
	}
	response.delAttribute("PREC_FIELD_ORDER");
	response.setAttribute("PREC_FIELD_ORDER", currentFieldOrder);

	String currentTypOrder = (request.getAttribute("TYPE_ORDER") == null || ((String) request
		.getAttribute("TYPE_ORDER")).equals("")) ? "" : (String) request.getAttribute("TYPE_ORDER");
	if (currentTypOrder.equals("")) {
	    currentTypOrder = " ASC";
	    response.delAttribute("TYPE_ORDER");
	    response.setAttribute("TYPE_ORDER", currentTypOrder);
	}
	response.delAttribute("PREC_TYPE_ORDER");
	response.setAttribute("PREC_TYPE_ORDER", currentTypOrder);

	// special cases for relations to others objects
	if (currentFieldOrder.equalsIgnoreCase("ENGINE"))
	    currentFieldOrder = "sbiEngines.label " + currentTypOrder.toLowerCase();
	else
	    currentFieldOrder = currentFieldOrder.toLowerCase() + currentTypOrder.toLowerCase();

	SourceBean moduleConfig = makeListConfiguration(profile);
	response.setAttribute(moduleConfig);

	PaginatorIFace paginator = new GenericPaginator();

	int numRows = 10;
	try {
	    SingletonConfig spagoconfig = SingletonConfig.getInstance();
	    String lookupnumRows = spagoconfig.getConfigValue("SPAGOBI.LOOKUP.numberRows");
	    if (lookupnumRows != null) {
		numRows = Integer.parseInt(lookupnumRows);
	    }
	} catch (Exception e) {
	    numRows = 10;
	    logger.error("Error while recovering number rows for " + "lookup from configuration, usign default 10", e);
	}
	paginator.setPageSize(numRows);
	logger.debug("setPageSize="+numRows);
	IBIObjectDAO objDAO = DAOFactory.getBIObjectDAO();
	List objectsList = null;
	logger.debug("Loading the documents list");
	if (initialPath != null && !initialPath.trim().equals("")) {
	    objectsList = objDAO.loadAllBIObjectsFromInitialPath(initialPath, currentFieldOrder);
	} else {
	    objectsList = objDAO.loadAllBIObjects(currentFieldOrder);
	}

	for (Iterator it = objectsList.iterator(); it.hasNext();) {
	    BIObject obj = (BIObject) it.next();
	    SourceBean rowSB = makeListRow(profile, obj);
	    if (rowSB != null)
		paginator.addRow(rowSB);
	}
	ListIFace list = new GenericList();
	list.setPaginator(paginator);
	// filter the list
	String valuefilter = (String) request.getAttribute(SpagoBIConstants.VALUE_FILTER);
	if (valuefilter != null) {
	    String columnfilter = (String) request.getAttribute(SpagoBIConstants.COLUMN_FILTER);
	    String typeFilter = (String) request.getAttribute(SpagoBIConstants.TYPE_FILTER);
	    String typeValueFilter = (String) request.getAttribute(SpagoBIConstants.TYPE_VALUE_FILTER);
	    list = DelegatedBasicListService.filterList(list, valuefilter, typeValueFilter, columnfilter, typeFilter,
		    getResponseContainer().getErrorHandler());
	}

	HashMap parametersMap = new HashMap();
	parametersMap.put(SpagoBIConstants.OBJECTS_VIEW, SpagoBIConstants.VIEW_OBJECTS_AS_LIST);
	response.setAttribute("PARAMETERS_MAP", parametersMap);
	logger.debug("OUT");
	return list;
    }

    /**
     * 
     * @param profile
     * @param obj
     * @return
     * @throws Exception
     */
    private SourceBean makeListRow(IEngUserProfile profile, BIObject obj) throws Exception {
	logger.debug("IN");
	String rowSBStr = "<ROW ";

	Integer visible = obj.getVisible();

	List functionalities = obj.getFunctionalities();
	boolean stateUp = false;
	boolean stateDown = false;
	boolean canExec = false;
	boolean canDev = false;
	boolean canTest = false;
	String objectStateCD = obj.getStateCode();
	logger.debug("Object State = "+objectStateCD);
	int visibleInstances = 0;	
	List lowFunct = new ArrayList();
	if (!functionalities.isEmpty()){
		lowFunct = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityList(functionalities);
	
	logger.debug("Loaded List of all LowFunctionalities related to the BIObject");
       
    if ((profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN))) {
    	logger.debug("Profile logged in has ADMINISTRATOR RIGHTS");
    	
		canDev = true;
		canExec = true;
		canTest = true;
		logger.debug("Profile CAN_EXECUTE,TEST,DEV the document");
		visibleInstances = ObjectsAccessVerifier.getVisibleInstances(initialPath, lowFunct);
		logger.debug("Got number of visibleInstances fo this document:"+visibleInstances);
		
		if (objectStateCD.equalsIgnoreCase("REL")) {
		    stateUp = false;
		    stateDown = true;	   
		} else if (objectStateCD.equalsIgnoreCase("DEV")) {
		    stateUp = true;
		    stateDown = false;
		} else if (objectStateCD.equalsIgnoreCase("TEST")) {
		    stateUp = true;
		    stateDown = true;
		}
		/*
		if (visible != null && visible.intValue() == 0) {
			logger.debug("Document not Visible");
			return null;
		}*/
	} 
	
    else if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV)) {
    	logger.debug("Profile logged in has DEVELOPER RIGHTS");
		
	    canExec = ObjectsAccessVerifier.canExec(objectStateCD, lowFunct, profile);
	    if (canExec)logger.debug("Profile CAN_EXECUTE the document");
	    else logger.debug("Profile CAN'T_EXECUTE the document");
	    visibleInstances = ObjectsAccessVerifier.getVisibleInstances(initialPath, lowFunct); 
	    logger.debug("Got number of visibleInstances fo this document:"+visibleInstances);
	    
	    if (objectStateCD.equalsIgnoreCase("REL")) {
		canDev = false;
	    } else if (objectStateCD.equalsIgnoreCase("TEST")) {
		canDev = false;
	    } else
	    if (obj.getStateCode().equalsIgnoreCase("DEV")) {
	    	canDev = ObjectsAccessVerifier.canDev(objectStateCD, lowFunct, profile);
		    stateUp = true;
		    stateDown = false;
	    }
	    if (visible != null && visible.intValue() == 0) {
			logger.debug("Document not Visible");
			return null;
		 }
	}

    else if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_TEST)) {
    	logger.debug("Profile logged in has TESTING RIGHTS");
    	
	    canExec = ObjectsAccessVerifier.canExec(objectStateCD, lowFunct, profile);
	    if (canExec)logger.debug("Profile CAN_EXECUTE the document");
	    else logger.debug("Profile CAN'T_EXECUTE the document");
	    if (canTest){
	    	canExec = canTest;
	    	logger.debug("Profile CAN_TEST the document");
	    }
	    visibleInstances = ObjectsAccessVerifier.getVisibleInstances(initialPath, lowFunct); 
	    logger.debug("Got number of visibleInstances fo this document:"+visibleInstances);

		if (objectStateCD.equalsIgnoreCase("REL")) {
			canTest = false;
		    stateUp = false;
		    stateDown = false;
		} else if (objectStateCD.equalsIgnoreCase("TEST")) {
			canTest = ObjectsAccessVerifier.canTest(objectStateCD, lowFunct, profile);
			stateUp = true;
			stateDown = true;
		} else if (objectStateCD.equalsIgnoreCase("DEV")) {
			canTest = false;
			stateUp = false;
			stateDown = false;
		}
		if (visible != null && visible.intValue() == 0) {
			logger.debug("Document not Visible");
			return null;
		}
	}
    
    else if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_USER)){
    	logger.debug("Profile logged in has EXECUTION RIGHTS");
    	
    	 canExec = ObjectsAccessVerifier.canExec(objectStateCD, lowFunct, profile);
    	 if (canExec) {
    		 logger.debug("Profile has the execution permission for the document [" + obj.getLabel() + "].");
    		 canExec = ObjectsAccessVerifier.checkProfileVisibility(obj, profile);
    		 if (canExec) {
    			 logger.debug("Profile satisfies profiled visiblity constraints for the document [" + obj.getLabel() + "].");
    		 } else {
    			 logger.debug("Profile DOES NOT satisfy profiled visiblity constraints for the document [" + obj.getLabel() + "].");
    		 }
    	 }
    	 
    	 if (canExec) logger.debug("Profile CAN_EXECUTE the document");
 	     else logger.debug("Profile CAN'T_EXECUTE the document");
    	 visibleInstances = ObjectsAccessVerifier.getVisibleInstances(initialPath, lowFunct); 
    	 logger.debug("Got number of visibleInstances fo this document:"+visibleInstances);
 			
    	stateUp = false;
	    stateDown = false;
	    
	    if (visible != null && visible.intValue() == 0) {
			logger.debug("Document not Visible");
			return null;
		}
    }
    
	
	if (canExec == false && canDev == false && canTest==false){
		logger.debug("Document never Executable for this user");
		return null;		
	}

	IMessageBuilder msgBuilder=MessageBuilderFactory.getMessageBuilder();
	//String localizedName=msgBuilder.getUserMessage(obj.getName(), SpagoBIConstants.DEFAULT_USER_BUNDLE, locale);
	// new localization in table I18NMessage
	String localizedName=msgBuilder.getI18nMessage(locale, obj.getName());	
	
	 rowSBStr += "		canExec=\"" + canExec + "\"";   
	 rowSBStr += "		canDev=\"" + canDev + "\"";
	rowSBStr += "		stateUp=\"" + stateUp + "\"";
	rowSBStr += "		stateDown=\"" + stateDown + "\"";
	rowSBStr += "		OBJECT_ID=\"" + obj.getId() + "\"";
	rowSBStr += "		LABEL=\"" + obj.getLabel() + "\"";
	rowSBStr += "		NAME=\"" + localizedName + "\"";
	rowSBStr += "		DESCR=\"" + (obj.getDescription() != null ? obj.getDescription() : "") + "\"";
	rowSBStr += "		ENGINE=\"" + obj.getEngine().getName() + "\"";
	rowSBStr += "		STATE=\"" + objectStateCD + "\"";
	rowSBStr += "		INSTANCES=\"" + visibleInstances + "\"";
	rowSBStr += " 		/>";
	}else{
		rowSBStr += " 		/>";
	}

	SourceBean rowSB = SourceBean.fromXMLString(rowSBStr);
	logger.debug("OUT");
	return rowSB;
    }

    private SourceBean makeListConfiguration(IEngUserProfile profile) throws Exception {
	logger.debug("IN");
	String moduleConfigStr = "";
	moduleConfigStr += "<CONFIG rows=\"20\" title=\"SBISet.objects.titleList\">";
	moduleConfigStr += "	<QUERIES/>";
	moduleConfigStr += "	<COLUMNS>";
	moduleConfigStr += "		<COLUMN label=\"ID\" name=\"OBJECT_ID\" hidden=\"true\" />";
	if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
	    moduleConfigStr += "		<COLUMN label=\"canExec\" name=\"canExec\" hidden=\"true\" />";
	}
	if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV)) {
	    moduleConfigStr += "		<COLUMN label=\"canDev\" name=\"canDev\" hidden=\"true\" />";
	}
	moduleConfigStr += "		<COLUMN label=\"SBISet.objects.columnLabel\" name=\"LABEL\" />";
	moduleConfigStr += "		<COLUMN label=\"SBISet.objects.columnName\" name=\"NAME\" />";
	// moduleConfigStr += " <COLUMN label=\"SBISet.objects.columnDescr\"
	// name=\"DESCR\" />";
	moduleConfigStr += "		<COLUMN label=\"SBISet.objects.columnEngine\" name=\"ENGINE\" />";
	moduleConfigStr += "		<COLUMN label=\"SBISet.objects.columnState\" name=\"STATE\" />";
	moduleConfigStr += "		<COLUMN label=\"SBISet.objects.instancesNumber\" name=\"INSTANCES\" horizontal-align=\"center\"/>";
	moduleConfigStr += "	</COLUMNS>";
	moduleConfigStr += "	<CAPTIONS>";
	moduleConfigStr += "	<EXEC_CAPTION  confirm=\"FALSE\" image=\"/img/execObject.gif\" label=\"SBISet.objects.captionExecute\">"
//		+ "		<PARAMETER name=\""
//		+ ObjectsTreeConstants.PAGE
//		+ "\" scope=\"\" type=\"ABSOLUTE\" value=\""
//		+ ExecuteBIObjectModule.MODULE_PAGE
//		+ "\"/> "
		// call new Action
		+ "		<PARAMETER name=\""
		+ ObjectsTreeConstants.ACTION
		+ "\" scope=\"\" type=\"ABSOLUTE\" value=\""
		+ SpagoBIConstants.EXECUTE_DOCUMENT_ACTION
		+ "\"/> "		
		+ "		<PARAMETER name=\""
		+ ObjectsTreeConstants.BIOBJECT_TREE_LIST
		+ "\" scope=\"\" type=\"ABSOLUTE\" value=\""
		+ ObjectsTreeConstants.BIOBJECT_TREE_LIST
		+ "\"/> "
		+ "		<PARAMETER name=\""
		+ SpagoBIConstants.MESSAGEDET
		+ "\" scope=\"\" type=\"ABSOLUTE\" value=\""
		+ ObjectsTreeConstants.EXEC_PHASE_CREATE_PAGE
		+ "\"/> "
		+ "		<PARAMETER name=\""
		+ ObjectsTreeConstants.OBJECT_ID
		+ "\" scope=\"LOCAL\" type=\"RELATIVE\" value=\"OBJECT_ID\"/> " + "	</EXEC_CAPTION>";
	/*if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_METADATA_MANAGEMENT)) {
	    moduleConfigStr += "	<METADATA_CAPTION popup=\"TRUE\" popupH=\"350\" popupW=\"800\" confirm=\"FALSE\" image=\"/img/editTemplate.jpg\" label=\"SBISet.objects.captionMetadata\">"
		    + "		<PARAMETER name=\""
		    + ObjectsTreeConstants.PAGE
		    + "\" scope=\"\" type=\"ABSOLUTE\" value=\""
		    + MetadataBIObjectModule.MODULE_PAGE
		    + "\"/> "
		    + "		<PARAMETER name=\""
		    + ObjectsTreeConstants.MESSAGE_DETAIL
		    + "\" scope=\"\" type=\"ABSOLUTE\" value=\""
		    + ObjectsTreeConstants.METADATA_SELECT
		    + "\"/> "
		    + "		<PARAMETER name=\""
		    + ObjectsTreeConstants.OBJECT_ID
		    + "\" scope=\"LOCAL\" type=\"RELATIVE\" value=\"OBJECT_ID\"/> "
		    + "		<PARAMETER name=\""
		    + LightNavigationManager.LIGHT_NAVIGATOR_DISABLED
		    + "\" scope=\"\" type=\"ABSOLUTE\" value=\"TRUE\"/> "
		    + "	</METADATA_CAPTION>";
	}*/
	if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_DETAIL_MANAGEMENT)) {
	    moduleConfigStr += "	<DETAIL_CAPTION  confirm=\"FALSE\" image=\"/img/detail.gif\" label=\"SBISet.objects.captionDetail\">"
		    + "		<PARAMETER name=\""
		    + ObjectsTreeConstants.PAGE
		    + "\" scope=\"\" type=\"ABSOLUTE\" value=\""
		    + DetailBIObjectModule.MODULE_PAGE
		    + "\"/> "
		    + "		<PARAMETER name=\""
		    + ObjectsTreeConstants.MESSAGE_DETAIL
		    + "\" scope=\"\" type=\"ABSOLUTE\" value=\""
		    + ObjectsTreeConstants.DETAIL_SELECT
		    + "\"/> "
		    + "		<PARAMETER name=\""
		    + ObjectsTreeConstants.OBJECT_ID
		    + "\" scope=\"LOCAL\" type=\"RELATIVE\" value=\"OBJECT_ID\"/> "
		    + "		<CONDITIONS>"
		    + "			<PARAMETER name=\"canDev\" scope='LOCAL' value='true' operator='EQUAL_TO' />"
		    + "		</CONDITIONS>" + "	</DETAIL_CAPTION>";
	}
	if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_DELETE_MANAGEMENT)) {
	    moduleConfigStr += "	<DELETE_CAPTION  confirm=\"TRUE\" image=\"/img/erase.gif\" label=\"SBISet.objects.captionErase\">"
		    + "		<PARAMETER name=\""
		    + ObjectsTreeConstants.PAGE
		    + "\" scope=\"\" type=\"ABSOLUTE\" value=\""
		    + DetailBIObjectModule.MODULE_PAGE
		    + "\"/> "
		    + "		<PARAMETER name=\""
		    + ObjectsTreeConstants.MESSAGE_DETAIL
		    + "\" scope=\"\" type=\"ABSOLUTE\" value=\""
		    + ObjectsTreeConstants.DETAIL_DEL
		    + "\"/> "
		    + "		<PARAMETER name=\""
		    + ObjectsTreeConstants.OBJECT_ID
		    + "\" scope=\"LOCAL\" type=\"RELATIVE\" value=\"OBJECT_ID\"/> "
		    + "		<CONDITIONS>"
		    + "			<PARAMETER name=\"canDev\" scope='LOCAL' value='true' operator='EQUAL_TO' />"
		    + "		</CONDITIONS>" + "	</DELETE_CAPTION>";
	}
	if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MOVE_DOWN_STATE)) {
	    // if
	    // (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)){
	    moduleConfigStr += "	<MOVEDOWN_CAPTION  confirm=\"TRUE\" image=\"/img/ArrowDown1.gif\" label='SBISet.objects.captionMoveDown'>"
		    + "		<PARAMETER name=\""
		    + ObjectsTreeConstants.PAGE
		    + "\" scope=\"\" type=\"ABSOLUTE\" value=\"UpdateBIObjectStatePage\"/> "
		    + "		<PARAMETER name=\""
		    + ObjectsTreeConstants.MESSAGE_DETAIL
		    + "\" scope=\"\" type=\"ABSOLUTE\" value=\""
		    + ObjectsTreeConstants.MOVE_STATE_DOWN
		    + "\"/> "
		    + "		<PARAMETER name=\""
		    + ObjectsTreeConstants.OBJECT_ID
		    + "\" scope=\"LOCAL\" type=\"RELATIVE\" value=\"OBJECT_ID\"/> "
		    + "		<PARAMETER name=\"LIGHT_NAVIGATOR_DISABLED\" scope=\"\" type=\"ABSOLUTE\" value=\"true\"/> "
		    + "		<CONDITIONS>"
		    + "			<PARAMETER name=\"stateDown\" scope='LOCAL' value='true' operator='EQUAL_TO' />"
		    + "		</CONDITIONS>" + "	</MOVEDOWN_CAPTION>";

	}
	if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MOVE_UP_STATE)) {

	    moduleConfigStr += "	<MOVEUP_CAPTION  confirm=\"TRUE\" image=\"/img/ArrowUp1.gif\" label='SBISet.objects.captionMoveUp'>"
		    + "		<PARAMETER name=\""
		    + ObjectsTreeConstants.PAGE
		    + "\" scope=\"\" type=\"ABSOLUTE\" value=\"UpdateBIObjectStatePage\"/> "
		    + "		<PARAMETER name=\""
		    + ObjectsTreeConstants.MESSAGE_DETAIL
		    + "\" scope=\"\" type=\"ABSOLUTE\" value=\""
		    + ObjectsTreeConstants.MOVE_STATE_UP
		    + "\"/> "
		    + "		<PARAMETER name=\""
		    + ObjectsTreeConstants.OBJECT_ID
		    + "\" scope=\"LOCAL\" type=\"RELATIVE\" value=\"OBJECT_ID\"/> "
		    + "		<PARAMETER name=\"LIGHT_NAVIGATOR_DISABLED\" scope=\"\" type=\"ABSOLUTE\" value=\"true\"/> "
		    + "		<CONDITIONS>"
		    + "			<PARAMETER name=\"stateUp\" scope='LOCAL' value='true' operator='EQUAL_TO' />"
		    + "		</CONDITIONS>" + "	</MOVEUP_CAPTION>";

	}
	moduleConfigStr += "	</CAPTIONS>";
	moduleConfigStr += "	<BUTTONS>";

	if ((profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN) && ChannelUtilities
		.isWebRunning())
		|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV)) {
	    moduleConfigStr += "		<INSERT_BUTTON confirm=\"FALSE\" image=\"/img/new.png\" label=\"SBISet.devObjects.newObjButt\"> "
		    + "			<PARAMETER name=\""
		    + ObjectsTreeConstants.PAGE
		    + "\" scope=\"\" type=\"ABSOLUTE\" value=\""
		    + DetailBIObjectModule.MODULE_PAGE
		    + "\"/> "
		    + "			<PARAMETER name=\""
		    + ObjectsTreeConstants.MESSAGE_DETAIL
		    + "\" scope=\"\" type=\"ABSOLUTE\" value=\""
		    + ObjectsTreeConstants.DETAIL_NEW + "\"/> " + "		</INSERT_BUTTON>";
	}
	moduleConfigStr += "		<CHANGE_VIEW_BUTTON confirm=\"FALSE\" image=\"/img/treeView.png\" label=\"SBISet.objects.treeViewButt\"> "
		+ "			<PARAMETER name=\"PAGE\" scope=\"\" type=\"ABSOLUTE\" value=\"BIObjectsPage\"/> "
		+ "			<PARAMETER name=\""
		+ SpagoBIConstants.OBJECTS_VIEW
		+ "\" scope=\"\" type=\"ABSOLUTE\" value=\""
		+ SpagoBIConstants.VIEW_OBJECTS_AS_TREE + "\"/> " + "		</CHANGE_VIEW_BUTTON>";
	moduleConfigStr += "		<BACK_BUTTON confirm=\"FALSE\" image=\"/img/back.png\" label=\"SBISet.objects.backButt\"  onlyPortletRunning=\"true\"> "
		+ "			<PARAMETER name=\"ACTION_NAME\" scope=\"\" type=\"ABSOLUTE\" value=\"START_ACTION\"/> "
		+ "			<PARAMETER name=\"PUBLISHER_NAME\" scope=\"\" type=\"ABSOLUTE\" value=\"LoginSBIAnaliticalModelPublisher\"/> "
		+ "			<PARAMETER name=\""
		+ LightNavigationManager.LIGHT_NAVIGATOR_RESET
		+ "\" scope=\"\" type=\"ABSOLUTE\" value=\"true\"/> " + "		</BACK_BUTTON>";
	moduleConfigStr += "	</BUTTONS>";
	moduleConfigStr += "</CONFIG>";
	SourceBean moduleConfig = SourceBean.fromXMLString(moduleConfigStr);
	logger.debug("OUT");
	return moduleConfig;
    }
}
