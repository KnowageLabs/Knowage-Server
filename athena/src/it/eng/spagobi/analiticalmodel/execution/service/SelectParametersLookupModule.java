/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.execution.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.dbaccess.Utils;
import it.eng.spago.dbaccess.sql.DataConnection;
import it.eng.spago.dbaccess.sql.DataRow;
import it.eng.spago.dbaccess.sql.SQLCommand;
import it.eng.spago.dbaccess.sql.result.DataResult;
import it.eng.spago.dbaccess.sql.result.ScrollableDataResult;
import it.eng.spago.dispatching.module.list.basic.AbstractBasicListModule;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spago.paginator.basic.PaginatorIFace;
import it.eng.spago.paginator.basic.impl.GenericList;
import it.eng.spago.paginator.basic.impl.GenericPaginator;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.FixedListDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.IJavaClassLov;
import it.eng.spagobi.behaviouralmodel.lov.bo.JavaClassDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.ScriptDetail;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.DelegatedBasicListService;
import it.eng.spagobi.commons.utilities.DataSourceUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ParameterValuesDecoder;
import it.eng.spagobi.commons.utilities.PortletUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * 
 * @author Angelo Bernabei - angelo.bernabei@eng.it
 * 
 * This module read the parameters values.
 * It is invoked by a lookup window from execution parameters form; the result is published into the same window.
 * 
 */

public class SelectParametersLookupModule extends AbstractBasicListModule {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(SelectParametersLookupModule.class);

	// define variable for value column name
    private String valColName = "";
    private List visibleColNames = new ArrayList();
    private String descriptionColName = "";
    
    private static final String RETURN_PARAM = "returnParam";
    
    private static final String RETURN_FIELD_NAME = "parameterFieldName";
    private boolean isChecklist = false ; 
    private boolean selectAll=false;
    /**
     * Class Constructor.
     */
    public SelectParametersLookupModule() {
	super();
    }

    /* (non-Javadoc)
     * @see it.eng.spago.dispatching.service.list.basic.IFaceBasicListService#getList(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
     */
    public ListIFace getList(SourceBean request, SourceBean response) throws Exception {
	logger.debug("IN");
	ListIFace list = null;
	// get role / par id / par field name name
	String roleName = (String) request.getAttribute("roleName");
	String parIdStr = (String) request.getAttribute("parameterId");
	String returnParam = (String) request.getAttribute(RETURN_PARAM);
	String selectAllS = (String) request.getAttribute("selectAll");
	if(selectAllS!=null && selectAllS.equalsIgnoreCase("TRUE")){
		selectAll=true;
	}
	else selectAll=false;
	
	logger.debug("roleName=" + roleName);
	logger.debug("parameterId=" + parIdStr);
	logger.debug("returnParam=" + returnParam);
	if (roleName == null)
	    logger.warn("roleName is null");
	if (parIdStr == null)
	    logger.warn("parameterId is null");

	Integer parId = new Integer(parIdStr);
	// check if the parameter use is manual input
	IParameterUseDAO parusedao = DAOFactory.getParameterUseDAO();
	ParameterUse paruse = parusedao.loadByParameterIdandRole(parId, roleName);
	//check if the parameter is a list or a check list
	String selectionType = paruse.getSelectionType();
	
	if (selectionType!= null && selectionType.equals("CHECK_LIST"))isChecklist = true;
	
	Integer manInp = paruse.getManualInput();
	if (manInp.intValue() == 1) {
	    String message = PortletUtilities.getMessage("scheduler.fillparmanually", "component_scheduler_messages");
	    response.setAttribute(SpagoBIConstants.MESSAGE_INFO, message);
	} else {
	    list = loadList(request, response, parId, roleName);
	}
	
	HashMap parametersMap = new HashMap();
	parametersMap.put("roleName", roleName);
	parametersMap.put("parameterId", parIdStr);
	parametersMap.put(RETURN_PARAM, returnParam);
	parametersMap.put("parameterFieldName", request.getAttribute("parameterFieldName"));
	parametersMap.put("objParId", request.getAttribute("objParId"));
	parametersMap.put("uuid", request.getAttribute("uuid"));
	
	// dependencies filter
	list = filterListForParametersCorrelation(paruse, request, list, parametersMap, this.getResponseContainer().getErrorHandler());
	
	// fill response
	response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "SelectParameterPublisher");
	
	// propagates all parameters during list navigation
	response.setAttribute("PARAMETERS_MAP", parametersMap);
	
	logger.debug("OUT");
	return list;
    }

    
    /**
     * Filters the list according to the parameters correlation
     * @param paruse The modality in use
     * @param request The SourceBean request
     * @param list The list to be filtered
     * @param parametersMap The map for the parameters to be propagated into the list
     * @return the filtered list according to the parameters correlation
     * @throws EMFUserError
     */
    public static ListIFace filterListForParametersCorrelation(ParameterUse paruse,
			SourceBean request, ListIFace list, HashMap parametersMap, EMFErrorHandler errorHandler) throws EMFUserError {
    	logger.debug("IN");
    	String objParIdStr = (String) request.getAttribute("objParId");
    	Integer objParId = new Integer(objParIdStr);
    	IObjParuseDAO objParuseDAO = DAOFactory.getObjParuseDAO();
    	List dependencies = objParuseDAO.loadObjParuse(objParId, paruse.getUseID());
    	if (dependencies != null && dependencies.size() > 0) {
			if (dependencies.size() == 1) {
				ObjParuse objparuse = (ObjParuse) dependencies.get(0);
				list = filterForCorrelation(list, objparuse, request, parametersMap, errorHandler);
			} else if (dependencies.size()==2) {
				ObjParuse objpuse1 = (ObjParuse) dependencies.get(0);
				ObjParuse objpuse2 = (ObjParuse) dependencies.get(1);
				list = evaluateSingleLogicOperation(objpuse1, objpuse2, list, request, parametersMap, errorHandler);
			} else {
				// build the expression
				int posinlist = 0;
				String expr = "";
				Iterator iterOps = dependencies.iterator();
				while(iterOps.hasNext())  {
					ObjParuse op = (ObjParuse) iterOps.next();
					expr += op.getPreCondition() + posinlist + op.getPostCondition() + op.getLogicOperator();
					posinlist ++;
				}
				expr = expr.trim();
				expr = "(" + expr;
				expr = expr + ")";
				list = evaluateExpression(expr, list, dependencies, request, parametersMap, errorHandler);
			}
    	}
    	logger.debug("OUT");
    	return list;
		
	}

	private static ListIFace evaluateExpression(String expr, ListIFace list, List ops, SourceBean request, HashMap parametersMap, EMFErrorHandler errorHandler) {
		ListIFace previusCalculated = list;
		try {
			// check number of left and right break, if numbers are different the expression is wrong
			int numberOfLeftRound = 0;
			String tmpExpr = expr;
			while(tmpExpr.indexOf("(")!=-1) {
				numberOfLeftRound ++;
				int indLR = tmpExpr.indexOf("(");
				tmpExpr = tmpExpr.substring(indLR+1);
			}
			int numberOfRightRound = 0;
			tmpExpr = expr;
			while(tmpExpr.indexOf(")")!=-1) {
				numberOfRightRound ++;
				int indRR = tmpExpr.indexOf(")");
				tmpExpr = tmpExpr.substring(indRR+1);
			}
			if(numberOfLeftRound!=numberOfRightRound) {
				logger.warn("Expression is wrong: number of left breaks is different from right breaks. Returning list without evaluating expression");
				return list;
			}
				
			//TODO make some more formal check on the expression before start to process it
			
			// calculate the list filtered based on each objparuse setting
			Map calculatedLists = new HashMap();
			int posinlist = 0;
			Iterator opsIter = ops.iterator();
			while(opsIter.hasNext()) {
				ObjParuse op = (ObjParuse)opsIter.next();
				ListIFace listop = filterForCorrelation(list, op, request, parametersMap, errorHandler);
				calculatedLists.put(String.valueOf(posinlist), listop);
				posinlist ++;
			}
			
			// generate final list evaluating expression
			
			while(expr.indexOf("(")!=-1) {
				int indLR = expr.indexOf("(");
				int indNextLR = expr.indexOf("(", indLR+1);
				int indNextRR = expr.indexOf(")", indLR+1);
				while( (indNextLR<indNextRR) && (indNextLR!=-1) ) {
					indLR = indNextLR;
					indNextLR = expr.indexOf("(", indLR+1);
					indNextRR = expr.indexOf(")", indLR+1);
				}
				int indRR = indNextRR;
				
				String exprPart = expr.substring(indLR, indRR+1);
				if(exprPart.indexOf("AND")!=-1) {
					int indexOper = exprPart.indexOf("AND");
					String firstListName = (exprPart.substring(1, indexOper)).replace("null", " ");
					String secondListName = (exprPart.substring(indexOper+3, exprPart.length()-1)).replace("null", " ");
					ListIFace firstList = null;
					if(!firstListName.trim().equals("previousList")) {
						firstList = (ListIFace)calculatedLists.get(firstListName.trim());
					} else {
						firstList = previusCalculated;
					}
					ListIFace secondList = null;
					if(!secondListName.trim().equals("previousList")) {
						secondList = (ListIFace)calculatedLists.get(secondListName.trim());
					} else {
						secondList = previusCalculated;
					}
					previusCalculated = intersectLists(firstList, secondList);
				} else if( exprPart.indexOf("OR")!=-1 ) {
					int indexOper = exprPart.indexOf("OR");
					String firstListName = (exprPart.substring(1, indexOper)).replace("null", " ");
					String secondListName = (exprPart.substring(indexOper+2, exprPart.length()-1)).replace("null", " ");
					ListIFace firstList = null;
					if(!firstListName.trim().equals("previousList")) {
						firstList = (ListIFace)calculatedLists.get(firstListName.trim());
					} else {
						firstList = previusCalculated;
					}
					ListIFace secondList = null;
					if(!secondListName.trim().equals("previousList")) {
						secondList = (ListIFace)calculatedLists.get(secondListName.trim());
					} else {
						secondList = previusCalculated;
					}
					previusCalculated = mergeLists(firstList, secondList);
				} else {
					// previousList remains the same as before
					logger.warn("A part of the Expression is wrong: inside a left break and right break there's no condition AND or OR");
				}
				expr = expr.substring(0, indLR) + "previousList" + expr.substring(indRR+1);
			}
		} catch (Exception e) {
			logger.warn("An error occurred while evaluating expression, return the complete list");
			return list;
		}
		return previusCalculated;
	}

	private static ListIFace evaluateSingleLogicOperation(ObjParuse obpuLeft, ObjParuse obpuRight, ListIFace list, SourceBean request, HashMap parametersMap, EMFErrorHandler errorHandler) {
		ListIFace listToReturn = list;
		ListIFace listLeft = filterForCorrelation(list, obpuLeft, request, parametersMap, errorHandler);
		String lo = obpuLeft.getLogicOperator();
		if(lo.equalsIgnoreCase("AND")) {
			listToReturn = filterForCorrelation(listLeft, obpuRight, request, parametersMap, errorHandler);
		} else if(lo.equalsIgnoreCase("OR")) {
			ListIFace listRight = filterForCorrelation(list, obpuRight, request, parametersMap, errorHandler);
			listToReturn = mergeLists(listLeft, listRight);
		} else {
			listToReturn = list;
		}
		return listToReturn;
	}

	protected static ListIFace mergeLists(ListIFace list1, ListIFace list2) {
		// transform all row sourcebean of the list 2 into strings and put them into a list
		PaginatorIFace pagLis2 = list2.getPaginator();
		SourceBean allRowsList2 = pagLis2.getAll();
		List rowsSBList2 = allRowsList2.getAttributeAsList("ROW"); 
		Iterator rowsSBList2Iter = rowsSBList2.iterator();
		List rowsList2 = new ArrayList();
		while(rowsSBList2Iter.hasNext()) {
			SourceBean rowSBList2 = (SourceBean)rowsSBList2Iter.next();
			String rowStrList2 = rowSBList2.toXML(false).toLowerCase();
			rowsList2.add(rowStrList2);
		}
		// if a row of the list one is not contained into list 2 then add it to the list 2
		SourceBean allRowsList1 = list1.getPaginator().getAll();
		List rowsSBList1 = allRowsList1.getAttributeAsList("ROW"); 
		Iterator rowsSBList1Iter = rowsSBList1.iterator();
		while(rowsSBList1Iter.hasNext()) {
			SourceBean rowSBList1 = (SourceBean)rowsSBList1Iter.next();
			String rowStrList1 = rowSBList1.toXML(false).toLowerCase();
			if(!rowsList2.contains(rowStrList1)) {
				pagLis2.addRow(rowSBList1);
			}
		}
		// return list 2
		list2.setPaginator(pagLis2);
		return list2;
	}
	
	protected static ListIFace intersectLists(ListIFace list1, ListIFace list2) {
		
		// transform all row sourcebean of the list 2 into strings and put them into a list
		PaginatorIFace pagLis2 = list2.getPaginator();
		SourceBean allRowsList2 = pagLis2.getAll();
		List rowsSBList2 = allRowsList2.getAttributeAsList("ROW"); 
		Iterator rowsSBList2Iter = rowsSBList2.iterator();
		List rowsList2 = new ArrayList();
		while(rowsSBList2Iter.hasNext()) {
			SourceBean rowSBList2 = (SourceBean)rowsSBList2Iter.next();
			String rowStrList2 = rowSBList2.toXML(false).toLowerCase();
			rowsList2.add(rowStrList2);
		}
		
		ListIFace newlist = new GenericList();	
		PaginatorIFace newpaginator = new GenericPaginator();
		newpaginator.setPageSize(pagLis2.getPageSize());
		
		
		// if a row of the list one is contained into list 2 then add it to the reulting list
		SourceBean allRowsList1 = list1.getPaginator().getAll();
		List rowsSBList1 = allRowsList1.getAttributeAsList("ROW"); 
		Iterator rowsSBList1Iter = rowsSBList1.iterator();
		while(rowsSBList1Iter.hasNext()) {
			SourceBean rowSBList1 = (SourceBean)rowsSBList1Iter.next();
			String rowStrList1 = rowSBList1.toXML(false).toLowerCase();
			if(rowsList2.contains(rowStrList1)) {
				newpaginator.addRow(rowSBList1);
			}
		}
		// return list 2
		newlist.setPaginator(newpaginator);
		return newlist;
	}
	
	private static ListIFace filterForCorrelation(ListIFace list, ObjParuse objParuse, SourceBean request, HashMap parametersMap, EMFErrorHandler errorHandler) {
		try {
			// get the id of the parent parameter
			Integer objParFatherId = objParuse.getObjParFatherId();
	        // find the bi parameter for the correlation (biparameter father)
			BIObjectParameter objParFather = DAOFactory.getBIObjectParameterDAO().loadForDetailByObjParId(objParFatherId);
	        // get the general parameter associated to the bi parameter father
	        IParameterDAO parameterDAO = DAOFactory.getParameterDAO();
	        Parameter parameter = parameterDAO.loadForDetailByParameterID(objParFather.getParID());
	        // get the type of the general parameter
	        String valueTypeFilter = parameter.getType();
			String valueFilter = "";
			// get the values of the father parameter
//			String valuesDecoded = (String) request.getAttribute(objParFather.getParameterUrlName());
//			// if the father parameter is no valued, returns the list unfiltered
//			if (valuesDecoded == null || valuesDecoded.trim().equals("")) 
//				return list;
//			ParameterValuesDecoder decoder = new ParameterValuesDecoder();
//			List valuesFilter = decoder.decode(valuesDecoded);
			String values = (String) request.getAttribute(objParFather.getParameterUrlName());
			// if the father parameter is no valued, returns the list unfiltered
			if (values == null || values.trim().equals("")) 
				return list;
			// values are separated by ";"
			String[] valuesArray = values.split(";");
			List valuesFilter = Arrays.asList(valuesArray);
			if (valuesFilter == null) 
				return list;
			
			// propagates the father parameter value
			parametersMap.put(objParFather.getParameterUrlName(), values);
			
	        // based on the values number do different filter operations
			switch (valuesFilter.size()) {
				case 0: return list;
				case 1: valueFilter = (String) valuesFilter.get(0);
						if (valueFilter != null && !valueFilter.equals(""))
							return DelegatedBasicListService.filterList(list, valueFilter, valueTypeFilter, 
								objParuse.getFilterColumn(), objParuse.getFilterOperation(), errorHandler);
						else return list;
				default: return DelegatedBasicListService.filterList(list, valuesFilter, valueTypeFilter, 
								objParuse.getFilterColumn(), objParuse.getFilterOperation(), errorHandler);
			}
		} catch (Exception e) {
			logger.error("Error while doing filter for corelation ", e);
			return list;
		}
	}
	
	
	private ListIFace loadList(SourceBean request, SourceBean response, Integer parId, String roleName)
	    throws Exception {
	logger.debug("IN");
	RequestContainer requestContainer = getRequestContainer();
	String idBiPar= (String)request.getAttribute("objParId");
	String parameterFieldName = (String) request.getAttribute("parameterFieldName");
	logger.debug("parameterFieldName=" + parameterFieldName);

	// define the spago paginator and list object
	PaginatorIFace paginator = new GenericPaginator();
	paginator.setPageSize(40);
	ListIFace list = new GenericList();



	// recover lov object
	IParameterDAO pardao = DAOFactory.getParameterDAO();
	Parameter par = pardao.loadForExecutionByParameterIDandRoleName(parId, roleName);
	ModalitiesValue modVal = par.getModalityValue();

	// get the lov provider
	String lovProvider = modVal.getLovProvider();

	// get from the request the type of lov
	String typeLov = LovDetailFactory.getLovTypeCode(lovProvider);

	// get the user profile
	SessionContainer permSession = this.getRequestContainer().getSessionContainer().getPermanentContainer();
	if (permSession == null)
	    logger.warn("Permanent session container is null!!!!");
	IEngUserProfile profile = (IEngUserProfile) permSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	if (profile == null)
	    logger.warn("IEngUserProfile is null!!!!");

	// read data
	SourceBean rowsSourceBean = null;
	if (typeLov.equalsIgnoreCase("QUERY")) {
	    rowsSourceBean = executeQuery(lovProvider, response, profile);
	    if (rowsSourceBean == null)
		return list;

	} else if (typeLov.equalsIgnoreCase("FIXED_LIST")) {
	    rowsSourceBean = executeFixedList(lovProvider, response, profile);
	    if (rowsSourceBean == null)
		return list;

	} else if (typeLov.equalsIgnoreCase("SCRIPT")) {
	    rowsSourceBean = executeScript(lovProvider, response, profile);
	    if (rowsSourceBean == null)
		return list;

	} else if (typeLov.equalsIgnoreCase("JAVA_CLASS")) {
	    rowsSourceBean = executeJavaClass(lovProvider, response, profile);
	    if (rowsSourceBean == null)
		return list;
	}
	logger.debug("valColName="+valColName);
	int rowSize = 0;
	// fill paginator
//	int count = 0;
	if (rowsSourceBean != null) {
	    List rows = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
	    rowSize = rows.size();
	    for (int i = 0; i < rows.size(); i++) {
		paginator.addRow(rows.get(i));
//		count++;
	    }
	}
	
	//in case it is a check list, sets all values in one page
	// if(isChecklist) paginator.setPageSize(rowSize*10);
	
	list.setPaginator(paginator);

	// get all the columns name
	rowsSourceBean = list.getPaginator().getAll();
	List colNames = new ArrayList();
	List rows = null;
	
	if (rowsSourceBean != null) {
	    rows = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
	    	
	    if ((rows != null) && (rows.size() != 0)) {
		SourceBean row = (SourceBean) rows.get(0);
		List rowAttrs = row.getContainedAttributes();
		Iterator rowAttrsIter = rowAttrs.iterator();
		while (rowAttrsIter.hasNext()) {
		    SourceBeanAttribute rowAttr = (SourceBeanAttribute) rowAttrsIter.next();
		    String rowKey = rowAttr.getKey();
		    
		    if (!visibleColNames.isEmpty()){
		    	Iterator iterateVisCol = visibleColNames.iterator(); 
		    	while(iterateVisCol.hasNext()){
		    		String visibleCol = (String)iterateVisCol.next();
		    		if (visibleCol.equalsIgnoreCase(rowKey)){
		    			colNames.add(rowKey);
		    		}
		    	}
		    }
		    
		    
		}
	    }
	}

	// build module configuration for the list
	StringBuffer moduleConfigStr = new StringBuffer("");
	moduleConfigStr.append("<CONFIG>");
	moduleConfigStr.append("	<QUERIES/>");
	moduleConfigStr.append("	<COLUMNS>");
	// if there's no colum name add a fake column to show that there's no
	// data
	if (colNames.size() == 0) {
	    moduleConfigStr.append("	<COLUMN name=\"No Result Found\" />");
	} else {
	    Iterator iterColNames = colNames.iterator();
	    while (iterColNames.hasNext()) {
		String colName = (String) iterColNames.next();
		moduleConfigStr.append("	<COLUMN name=\"" + colName + "\" />");
	    }
	}

	String uuid3 = (String) request.getAttribute("uuid");
    moduleConfigStr.append("	<COLUMN name=\"\" horizontal-align=\"right\" order_buttons=\"false\">");
	if(isChecklist){	
    moduleConfigStr.append("<BUTTONS>");
		moduleConfigStr.append("<SELECT_ALL confirm='TRUE' image='/img/expertok.gif' label='SBIDev.ListParam.selectAll'>");
		moduleConfigStr.append("			<ONCLICK>");
		moduleConfigStr.append("				<![CDATA[");
		// sets correlation flag and submits parameters form
		moduleConfigStr.append("parent.setRefreshCorrelationFlag" + uuid3 + "();");
		moduleConfigStr.append("parent.document.getElementById('messagedet" + uuid3+"').value='SELECT_ALL';");		
		moduleConfigStr.append("parent.document.getElementById('objParId" + uuid3+"').value='"+idBiPar+"';");		
		moduleConfigStr.append("parent.document.getElementById('parameterId" + uuid3+"').value='"+parId.toString()+"';");		
		moduleConfigStr.append("parent.document.getElementById('allSelectMode" + uuid3+"').name='allSelectMode';");		
		moduleConfigStr.append("parent.document.getElementById('allSelectMode" + uuid3+"').value='true';");		
		moduleConfigStr.append("parent.document.getElementById('parametersForm" + uuid3+"').submit();");
		//moduleConfigStr.append("parent.document.getElementById('allForm" +idBiPar+ uuid3 + parId.toString()+"').submit();");
		moduleConfigStr.append("				]]>");
		moduleConfigStr.append("			</ONCLICK>");
		moduleConfigStr.append("</SELECT_ALL>");
		moduleConfigStr.append("<DESELECT_ALL confirm='TRUE' image='/img/expertclose.gif' label='SBIDev.ListParam.deselectAll'>");
		moduleConfigStr.append("			<ONCLICK>");
		moduleConfigStr.append("				<![CDATA[");
		moduleConfigStr.append("parent.setRefreshCorrelationFlag" + uuid3 + "();");
		//moduleConfigStr.append("parent.document.getElementById('nooneForm" +idBiPar+ uuid3 + parId.toString()+"').submit();");
		moduleConfigStr.append("parent.document.getElementById('messagedet" + uuid3+"').value='DESELECT_ALL';");		
		moduleConfigStr.append("parent.document.getElementById('objParId" + uuid3+"').value='"+idBiPar+"';");		
		moduleConfigStr.append("parent.document.getElementById('parameterId" + uuid3+"').value='"+parId.toString()+"';");		
		moduleConfigStr.append("parent.document.getElementById('allSelectMode" + uuid3+"').name='allSelectMode';");		
		moduleConfigStr.append("parent.document.getElementById('allSelectMode" + uuid3+"').value='true';");		
		moduleConfigStr.append("parent.document.getElementById('parametersForm" + uuid3+"').submit();");
		moduleConfigStr.append("				]]>");
		moduleConfigStr.append("			</ONCLICK>");
		moduleConfigStr.append("</DESELECT_ALL>");		
	    moduleConfigStr.append("	</BUTTONS>");	
	}
	    moduleConfigStr.append("	</COLUMN>");
	moduleConfigStr.append("	</COLUMNS>");
	moduleConfigStr.append("	<CAPTIONS>");
	if(isChecklist){
		
		moduleConfigStr.append("		<SELECT_CAPTION  checkList=\"true\" confirm=\"FALSE\" label=\"SBIListLookPage.selectButton\">");
		moduleConfigStr.append("			<ONCLICK>");
		moduleConfigStr.append("				<![CDATA[");
		// sets value and its description on parameters form (that is on parent window)
		moduleConfigStr.append("				var valuesArrayStr = parent.document.getElementById('<PARAMETER name='" + RETURN_PARAM + "' scope='SERVICE_REQUEST'/>').value;");
		moduleConfigStr.append("				var selectedValue = '<PARAMETER name='" + valColName + "' scope='LOCAL'/>';");
		moduleConfigStr.append("				var valuesArray = valuesArrayStr.split(';');");
		moduleConfigStr.append("				if (valuesArray == null || (valuesArray.length == 1 && valuesArray[0] == '')) valuesArray = new Array();");
		
		moduleConfigStr.append("				var descriptionsArrayStr = parent.document.getElementById('<PARAMETER name='" + RETURN_PARAM + "' scope='SERVICE_REQUEST'/>Desc').value;");
		moduleConfigStr.append("				var selectedDescription = '<PARAMETER name='" + descriptionColName + "' scope='LOCAL'/>';");
		moduleConfigStr.append("				var descriptionsArray = descriptionsArrayStr.split(';');");
		moduleConfigStr.append("				if (descriptionsArray == null || (descriptionsArray.length == 1 && descriptionsArray[0] == '')) descriptionsArray = new Array();");
		
		moduleConfigStr.append("				if (valuesArray.contains(selectedValue)) {");
		moduleConfigStr.append("					valuesArray.removeFirst(selectedValue);");
		moduleConfigStr.append("					descriptionsArray.removeFirst(selectedDescription);");
		moduleConfigStr.append("				} else {");
		moduleConfigStr.append("					valuesArray.push(selectedValue);");
		moduleConfigStr.append("					descriptionsArray.push(selectedDescription);");
		moduleConfigStr.append("				}");
		
		moduleConfigStr.append("				parent.document.getElementById('<PARAMETER name='" + RETURN_PARAM + "' scope='SERVICE_REQUEST'/>').value = valuesArray.join(';');");
		moduleConfigStr.append("				parent.document.getElementById('<PARAMETER name='" + RETURN_PARAM + "' scope='SERVICE_REQUEST'/>Desc').value = descriptionsArray.join(';');");
		// is there any biparameter that depends on current biparameter? if it is the case, automatic form submit is performed (with correlation flag set)
		// get current biparameter id
		String objParIdStr = (String) request.getAttribute("objParId");
		Integer objParId = new Integer(objParIdStr);
		// search for biparameters that depend from the current one:
		List lblBiParamDependent = null;
		try {
		    lblBiParamDependent = DAOFactory.getObjParuseDAO().getDependencies(objParId);
		} catch (Exception e) {
		    logger.error("Error while recovering dependencies " + " for biparm id " + objParIdStr, e);
		    lblBiParamDependent = new ArrayList();
		}
		if (lblBiParamDependent != null && lblBiParamDependent.size() > 0) {
			// find parameters form uuid:
			String uuid = (String) request.getAttribute("uuid");
			 // sets correlation flag and submits parameters form
			if (mustRefresh(request)) {
				moduleConfigStr.append("parent.setRefreshCorrelationFlag" + uuid + "();");
				moduleConfigStr.append("parent.document.getElementById('parametersForm" + uuid + "').submit();");
			}
		}
		moduleConfigStr.append("				]]>");
		moduleConfigStr.append("			</ONCLICK>");
		
		moduleConfigStr.append("			<CLICKED>");
		moduleConfigStr.append("				<![CDATA[");
		// function that checks if the current row is already checked or not
		moduleConfigStr.append("				var parName = '<PARAMETER name='" + RETURN_PARAM + "' scope='SERVICE_REQUEST'/>';");
		moduleConfigStr.append("				var rowValue = '<PARAMETER name='" + valColName + "' scope='LOCAL'/>';");
		moduleConfigStr.append("				var alreadySelectedStr = parent.document.getElementById(parName).value;");
		moduleConfigStr.append("				var alreadySelectedValuesArray = alreadySelectedStr.split(';');");
		moduleConfigStr.append("				if (alreadySelectedValuesArray != null && alreadySelectedValuesArray.contains(rowValue)){");
		moduleConfigStr.append("				    document.getElementById('<PARAMETER name='" + valColName + "' scope='LOCAL'/>').checked = 'true' ;");
		moduleConfigStr.append("				}");
		moduleConfigStr.append("				]]>");
		moduleConfigStr.append("			</CLICKED>");
		
		moduleConfigStr.append("			<ROWVALUE>");
		moduleConfigStr.append("				<![CDATA[");
		// gets the value of the current row
		moduleConfigStr.append("				<PARAMETER name='" + valColName + "' scope='LOCAL'/>");
		moduleConfigStr.append("				]]>");
		moduleConfigStr.append("			</ROWVALUE>");
		
		moduleConfigStr.append("		</SELECT_CAPTION>");
		
	}else{
		
		moduleConfigStr.append("		<SELECT_CAPTION  confirm=\"FALSE\" image=\"/img/button_ok.gif\" label=\"SBIListLookPage.selectButton\">");
		moduleConfigStr.append("			<ONCLICK>");
		moduleConfigStr.append("				<![CDATA[");
		// sets value and its description on parameters form (that is on parent window)
		moduleConfigStr.append("				parent.document.getElementById('<PARAMETER name='" + RETURN_PARAM + "' scope='SERVICE_REQUEST'/>').value='<PARAMETER name='" + valColName + "' scope='LOCAL'/>';");
		moduleConfigStr.append("				parent.document.getElementById('<PARAMETER name='" + RETURN_PARAM + "' scope='SERVICE_REQUEST'/>Desc').value='<PARAMETER name='" + descriptionColName + "' scope='LOCAL'/>';");
		// hides this window 
		moduleConfigStr.append("				parent.win_<PARAMETER name='" + RETURN_FIELD_NAME + "' scope='SERVICE_REQUEST'/>.hide();");
		// is there any biparameter that depends on current biparameter? if it is the case, automatic form submit is performed (with correlation flag set)
		// get current biparameter id
		String objParIdStr = (String) request.getAttribute("objParId");
		Integer objParId = new Integer(objParIdStr);
		// search for biparameters that depend from the current one:
		List lblBiParamDependent = null;
		try {
		    lblBiParamDependent = DAOFactory.getObjParuseDAO().getDependencies(objParId);
		} catch (Exception e) {
		    logger.error("Error while recovering dependencies " + " for biparm id " + objParIdStr, e);
		    lblBiParamDependent = new ArrayList();
		}
		if (lblBiParamDependent != null && lblBiParamDependent.size() > 0) {
			if (mustRefresh(request)) {
				// find parameters form uuid:
				String uuid = (String) request.getAttribute("uuid");
				 // sets correlation flag and submits parameters form
				moduleConfigStr.append("parent.setRefreshCorrelationFlag" + uuid + "();");
				moduleConfigStr.append("parent.document.getElementById('parametersForm" + uuid + "').submit();");
			}
		}
		moduleConfigStr.append("				]]>");
		moduleConfigStr.append("			</ONCLICK>");
		moduleConfigStr.append("		</SELECT_CAPTION>");
		
	}
	moduleConfigStr.append("	</CAPTIONS>");
	moduleConfigStr.append("	<BUTTONS/> ");
	
	
	moduleConfigStr.append("</CONFIG>");
	SourceBean moduleConfig = SourceBean.fromXMLString(moduleConfigStr.toString());
	response.setAttribute(moduleConfig);

	// filter the list
	String valuefilter = (String) request.getAttribute(SpagoBIConstants.VALUE_FILTER);
	if (valuefilter != null) {
	    String columnfilter = (String) request.getAttribute(SpagoBIConstants.COLUMN_FILTER);
	    String typeFilter = (String) request.getAttribute(SpagoBIConstants.TYPE_FILTER);
	    String typeValueFilter = (String) request.getAttribute(SpagoBIConstants.TYPE_VALUE_FILTER);
	    list = DelegatedBasicListService.filterList(list, valuefilter, typeValueFilter, columnfilter, typeFilter,
		    getResponseContainer().getErrorHandler());
	}

	// fill response
	response.setAttribute(SpagoBIConstants.PARAMETER_FIELD_NAME, parameterFieldName);
	response.setAttribute(SpagoBIConstants.VALUE_COLUMN_NAME, valColName);
	logger.debug("OUT");
	return list;
    }

    
    private boolean mustRefresh(SourceBean request) {
		boolean toReturn = false;
		logger.debug("IN");
		String mustRefreshCorrelationFlag = (String) request.getAttribute("MUST_REFRESH_PAGE_FOR_CORRELATION");
		logger.debug("MUST_REFRESH_PAGE_FOR_CORRELATION flag in request = [" + mustRefreshCorrelationFlag + "]");
		toReturn = mustRefreshCorrelationFlag != null && mustRefreshCorrelationFlag.equalsIgnoreCase("true");
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
		
	}

	private SourceBean executeQuery(String lovProvider, SourceBean response, IEngUserProfile profile) throws Exception {
	logger.debug("IN");
	SourceBean result = null;
	logger.debug("lovProvider="+lovProvider);
	QueryDetail qd = QueryDetail.fromXML(lovProvider);
	/*
	if (qd.requireProfileAttributes()) {
	    String message = PortletUtilities.getMessage("scheduler.noProfileAttributesSupported",
		    "component_scheduler_messages");
	    response.setAttribute(SpagoBIConstants.MESSAGE_INFO, message);
	    return result;
	}
	*/
	valColName = qd.getValueColumnName();
	visibleColNames = qd.getVisibleColumnNames();
	
	logger.debug("valColName="+valColName);
	descriptionColName = qd.getDescriptionColumnName();
	logger.debug("descriptionColName="+descriptionColName);
	String datasource = qd.getDataSource();
	String statement = qd.getQueryDefinition();

	try {
	    statement = StringUtilities.substituteProfileAttributesInString(statement, profile);
	    result = (SourceBean) executeSelect(getRequestContainer(), getResponseContainer(), datasource, statement);
	} catch (Exception e) {
	    logger.error("Exception",e);
	    String stacktrace = e.toString();
	    response.setAttribute("stacktrace", stacktrace);
            response.setAttribute("errorMessage", cleanStackTrace(stacktrace));
	    response.setAttribute("testExecuted", "false");
	}
	logger.debug("OUT");
	return result;
    }

    private SourceBean executeFixedList(String lovProvider, SourceBean response, IEngUserProfile profile)
	    throws Exception {
	logger.debug("IN");
	logger.debug("lovProvider."+lovProvider);
	SourceBean resultSB = null;
	FixedListDetail fixlistDet = FixedListDetail.fromXML(lovProvider);
	if (fixlistDet.requireProfileAttributes()) {
	    String message = PortletUtilities.getMessage("scheduler.noProfileAttributesSupported",
		    "component_scheduler_messages");
	    response.setAttribute(SpagoBIConstants.MESSAGE_INFO, message);
	    return null;
	}
	valColName = fixlistDet.getValueColumnName();
	visibleColNames = fixlistDet.getVisibleColumnNames();
	logger.debug("valColName:"+valColName);
	descriptionColName = fixlistDet.getDescriptionColumnName();
	logger.debug("descriptionColName="+descriptionColName);
	
	try {
//	    String result = fixlistDet.getLovResult(profile);
//	    resultSB = SourceBean.fromXMLString(result);
	    if (!resultSB.getName().equalsIgnoreCase("ROWS")) {
		throw new Exception("The fix list is empty");
	    } else if (resultSB.getAttributeAsList(DataRow.ROW_TAG).size() == 0) {
		throw new Exception("The fix list is empty");
	    }
	} catch (Exception e) {
	    logger.error("Error while converting fix lov into spago list", e);
	    String stacktrace = e.toString();
	    response.setAttribute("stacktrace", stacktrace);
	    response.setAttribute("errorMessage", "Error while executing fix list lov");
	    response.setAttribute("testExecuted", "false");
	    return null;
	}
	logger.debug("OUT");
	return resultSB;
    }

    private SourceBean executeScript(String lovProvider, SourceBean response, IEngUserProfile profile) throws Exception {
	logger.debug("IN");
	SourceBean resultSB = null;
	ScriptDetail scriptDetail = ScriptDetail.fromXML(lovProvider);
	if (scriptDetail.requireProfileAttributes()) {
	    String message = PortletUtilities.getMessage("scheduler.noProfileAttributesSupported",
		    "component_scheduler_messages");
	    response.setAttribute(SpagoBIConstants.MESSAGE_INFO, message);
	    return null;
	}
	valColName = scriptDetail.getValueColumnName();
	visibleColNames = scriptDetail.getVisibleColumnNames();
	logger.debug("valColName="+valColName);
	descriptionColName = scriptDetail.getDescriptionColumnName();
	logger.debug("descriptionColName="+descriptionColName);
	try {
//	    String result = scriptDetail.getLovResult(profile);
//	    resultSB = SourceBean.fromXMLString(result);
	} catch (Exception e) {
	    logger.error("Error while executing the script lov", e);
	    String stacktrace = e.toString();
	    response.setAttribute("stacktrace", stacktrace);
	    response.setAttribute("errorMessage", "Error while executing script");
	    response.setAttribute("testExecuted", "false");
	    return null;
	}
	logger.debug("OUT");
	return resultSB;
    }

    private SourceBean executeJavaClass(String lovProvider, SourceBean response, IEngUserProfile profile)
	    throws Exception {
	logger.debug("IN");
	SourceBean resultSB = null;
	JavaClassDetail javaClassDetail = JavaClassDetail.fromXML(lovProvider);
	if (javaClassDetail.requireProfileAttributes()) {
	    String message = PortletUtilities.getMessage("scheduler.noProfileAttributesSupported",
		    "component_scheduler_messages");
	    response.setAttribute(SpagoBIConstants.MESSAGE_INFO, message);
	    return null;
	}
	valColName = javaClassDetail.getValueColumnName();
	visibleColNames = javaClassDetail.getVisibleColumnNames();
	logger.debug("valColName="+valColName);
	descriptionColName = javaClassDetail.getDescriptionColumnName();
	logger.debug("descriptionColName="+descriptionColName);
	try {
	    String javaClassName = javaClassDetail.getJavaClassName();
	    IJavaClassLov javaClassLov = (IJavaClassLov) Class.forName(javaClassName).newInstance();
//	    String result = javaClassLov.getValues(profile);
//	    resultSB = SourceBean.fromXMLString(result);
	} catch (Exception e) {
	    logger.error("Error while executing the java class lov", e);
	    String stacktrace = e.toString();
	    response.setAttribute("stacktrace", stacktrace);
	    response.setAttribute("errorMessage", "Error while executing java class");
	    response.setAttribute("testExecuted", "false");
	    return null;
	}
	logger.debug("OUT");
	return resultSB;
    }

    /**
     * Executes a select statement.
     * 
     * @param requestContainer The request container object
     * @param responseContainer The response container object
     * @param statement The statement definition string
     * @param datasource the datasource
     * 
     * @return A generic object containing the Execution results
     * 
     * @throws EMFInternalError the EMF internal error
     */
    public static Object executeSelect(RequestContainer requestContainer, ResponseContainer responseContainer,
	    String datasource, String statement) throws EMFInternalError {
	logger.debug("IN");
	Object result = null;
	DataConnection dataConnection = null;
	SQLCommand sqlCommand = null;
	DataResult dataResult = null;
	try {
	    DataSourceUtilities dsUtil = new DataSourceUtilities();
	    Connection conn = dsUtil.getConnection(requestContainer,datasource);
	    dataConnection = dsUtil.getDataConnection(conn);
	    sqlCommand = dataConnection.createSelectCommand(statement);
	    dataResult = sqlCommand.execute();
	    ScrollableDataResult scrollableDataResult = (ScrollableDataResult) dataResult.getDataObject();
	    result = scrollableDataResult.getSourceBean();
	} finally {
	    Utils.releaseResources(dataConnection, sqlCommand, dataResult);
	    logger.debug("OUT");
	}
	return result;
    }
    
    private String cleanStackTrace(String stacktrace) {

	int startIndex = stacktrace.indexOf("java.sql.");
	int endIndex = stacktrace.indexOf("\n\tat ", startIndex);
	if (endIndex == -1)
	    endIndex = stacktrace.indexOf(" at ", startIndex);
	if (startIndex != -1 && endIndex != -1)
	    return stacktrace.substring(startIndex, endIndex);
	return "";
    }

}
