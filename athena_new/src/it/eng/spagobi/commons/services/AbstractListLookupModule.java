/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.services;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.module.list.basic.AbstractBasicListModule;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spago.paginator.basic.PaginatorIFace;
import it.eng.spago.paginator.basic.impl.GenericList;
import it.eng.spago.paginator.basic.impl.GenericPaginator;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.container.ContextManager;
import it.eng.spagobi.container.CoreContextManager;
import it.eng.spagobi.container.SpagoBISessionContainer;
import it.eng.spagobi.container.strategy.LightNavigatorContextRetrieverStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractListLookupModule extends AbstractBasicListModule {

	private EMFErrorHandler errorHand = null;
	CoreContextManager contextManager = null;
	
	protected void getErroHandler() {
		ResponseContainer respCont = getResponseContainer();
		errorHand = respCont.getErrorHandler();
	}
	
	
	/**
	 * Filter list for correlated param.
	 * 
	 * @param request the request
	 * @param list the list
	 * 
	 * @return the list i face
	 * 
	 * @throws Exception the exception
	 */
	public ListIFace filterListForCorrelatedParam(SourceBean request, ListIFace list) throws Exception {
		// get error handler
		getErroHandler();
		// get biobject from the session
		ExecutionInstance instance = contextManager.getExecutionInstance(ExecutionInstance.class.getName());
		BIObject obj = instance.getBIObject();
		// get the id of the lookup parameter
		String objParIdStr = (String) request.getAttribute("LOOKUP_PARAMETER_ID");
//		if(objParIdStr==null) 
//			objParIdStr = (String)getSession(request).getAttribute("LOOKUP_PARAMETER_ID");
		Integer objParId = Integer.valueOf(objParIdStr);
		// get the id of the paruse correlated 
		Integer correlatedParuseId = Integer.valueOf((String) request.getAttribute("correlated_paruse_id"));
		// get dao of objparuse (correlation)
		IObjParuseDAO objParuseDAO = DAOFactory.getObjParuseDAO();
		// get all the objparuse associated to the parameter and paruse
		List ops = objParuseDAO.loadObjParuse(objParId, correlatedParuseId);
		if( (ops!=null) && (ops.size()!=0) ) {
			if(ops.size()==1) {
				ObjParuse objpuse = (ObjParuse)ops.get(0);
				list = filterForCorrelation(list, objpuse, obj);
			} else if (ops.size()==2) {
				ObjParuse objpuse1 = (ObjParuse)ops.get(0);
				ObjParuse objpuse2 = (ObjParuse)ops.get(1);
				list = evaluateSingleLogicOperation(objpuse1,objpuse2,list, obj);
			} else {
				// build the expression
				int posinlist = 0;
				String expr = "";
				Iterator iterOps = ops.iterator();
				while(iterOps.hasNext())  {
					ObjParuse op = (ObjParuse)iterOps.next();
					expr += op.getPreCondition() + posinlist + op.getPostCondition() + op.getLogicOperator();
					posinlist ++;
				}
				expr = expr.trim();
				expr = "(" + expr;
				expr = expr + ")";
				list = evaluateExpression(expr, list, ops, obj);
			}
		}
		return list;
	}
	
	/**
	 * Filter list for correlated param.
	 * 
	 * @param request the request
	 * @param list the list
	 * @param httpRequest the http request
	 * 
	 * @return the list i face
	 * 
	 * @throws Exception the exception
	 */
	public ListIFace filterListForCorrelatedParam(SourceBean request, ListIFace list, HttpServletRequest httpRequest) throws Exception {
		RequestContainer reqCont = ChannelUtilities.getRequestContainer(httpRequest);
		ResponseContainer respCont = ChannelUtilities.getResponseContainer(httpRequest);
		errorHand = respCont.getErrorHandler();
		SessionContainer sessionCont = reqCont.getSessionContainer();
		contextManager = new CoreContextManager(new SpagoBISessionContainer(sessionCont), 
				new LightNavigatorContextRetrieverStrategy(request));
		// get biobject from the session
		ExecutionInstance instance = contextManager.getExecutionInstance(ExecutionInstance.class.getName());
		BIObject obj = instance.getBIObject();
		// get the id of the lookup parameter
		String objParIdStr = (String) request.getAttribute("LOOKUP_PARAMETER_ID");
//		if(objParIdStr==null) 
//			objParIdStr = (String)getSession(request).getAttribute("LOOKUP_PARAMETER_ID");
		Integer objParId = Integer.valueOf(objParIdStr);
		// get the id of the paruse correlated 
		Integer correlatedParuseId = Integer.valueOf((String) request.getAttribute("correlated_paruse_id"));
		// get dao of objparuse (correlation)
		IObjParuseDAO objParuseDAO = DAOFactory.getObjParuseDAO();
		// get all the objparuse associated to the parameter and paruse
		List ops = objParuseDAO.loadObjParuse(objParId, correlatedParuseId);
		if( (ops!=null) && (ops.size()!=0) ) {
			if(ops.size()==1) {
				ObjParuse objpuse = (ObjParuse)ops.get(0);
				list = filterForCorrelation(list, objpuse, obj);
			} else if (ops.size()==2) {
				ObjParuse objpuse1 = (ObjParuse)ops.get(0);
				ObjParuse objpuse2 = (ObjParuse)ops.get(1);
				list = evaluateSingleLogicOperation(objpuse1,objpuse2,list, obj);
			} else {
				// build the expression
				int posinlist = 0;
				String expr = "";
				Iterator iterOps = ops.iterator();
				while(iterOps.hasNext())  {
					ObjParuse op = (ObjParuse)iterOps.next();
					expr += op.getPreCondition() + posinlist + op.getPostCondition() + op.getLogicOperator();
					posinlist ++;
				}
				expr = expr.trim();
				expr = "(" + expr;
				expr = expr + ")";
				list = evaluateExpression(expr, list, ops, obj);
			}
		}
		return list;
	}
	
	
	private ListIFace evaluateExpression(String expr, ListIFace list, List ops, BIObject obj) {
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
				SpagoBITracer.warning(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
						            "evaluateExpression", "Expression is wrong: number of left breaks is" +
						            "different from right breaks. Returning list without evaluating expression");
				return list;
			}
			
			//TODO make some more formal check on the expression before start to process it
			
			// calculate the list filtered based on each objparuse setting
			Map calculatedLists = new HashMap();
			int posinlist = 0;
			Iterator opsIter = ops.iterator();
			while(opsIter.hasNext()) {
				ObjParuse op = (ObjParuse)opsIter.next();
				ListIFace listop = filterForCorrelation(list, op, obj);
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
					SpagoBITracer.warning(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
						            "evaluateExpression", "A part of the Expression is wrong: inside a " +
						            " left break and right break there's no condition AND or OR");
				}
				expr = expr.substring(0, indLR) + "previousList" + expr.substring(indRR+1);
			}
		} catch (Exception e) {
			SpagoBITracer.warning(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
		            			"evaluateExpression", "An error occurred while evaluating expression, " +
		            			"return the complete list");
			return list;
		}
		return previusCalculated;
	}
	

	private ListIFace evaluateSingleLogicOperation(ObjParuse obpuLeft, ObjParuse obpuRight, ListIFace list, BIObject obj) {
		ListIFace listToReturn = list;
		ListIFace listLeft = filterForCorrelation(list, obpuLeft, obj);
		String lo = obpuLeft.getLogicOperator();
		if(lo.equalsIgnoreCase("AND")) {
			listToReturn = filterForCorrelation(listLeft, obpuRight, obj);
		} else if(lo.equalsIgnoreCase("OR")) {
			ListIFace listRight = filterForCorrelation(list, obpuRight, obj);
			listToReturn = mergeLists(listLeft, listRight);
		} else {
			listToReturn = list;
		}
		return listToReturn;
	}
	
	
	
	
	
	protected ListIFace mergeLists(ListIFace list1, ListIFace list2) {
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
	
	
	
	protected ListIFace intersectLists(ListIFace list1, ListIFace list2) {
		
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
	
	
	
	
	private ListIFace filterForCorrelation(ListIFace list, ObjParuse objParuse, BIObject obj) {
		try {
			// get the id of the parent parameter
			Integer objParFatherId = objParuse.getObjParFatherId();
	        // find the bi parameter for the correlation (biparameter father)
			List biparams = obj.getBiObjectParameters();
			BIObjectParameter objParFather = null;
	        Iterator iterParams = biparams.iterator();
	        while (iterParams.hasNext()) {
	        	BIObjectParameter aBIObjectParameter = (BIObjectParameter) iterParams.next();
	        	if (aBIObjectParameter.getId().equals(objParFatherId)) {
	        		objParFather = aBIObjectParameter;
	        		break;
	        	}
	        }
	        // get the general parameter associated to the bi parameter father
	        IParameterDAO parameterDAO = DAOFactory.getParameterDAO();
	        Parameter parameter = parameterDAO.loadForDetailByParameterID(objParFather.getParID());
	        // get the type of the general parameter
	        String valueTypeFilter = parameter.getType();
			String valueFilter = "";
			// get the values of the father parameter
			List valuesFilter = objParFather.getParameterValues();
			if (valuesFilter == null) 
				return list;
	        // based on the values number do different filter operations
			switch (valuesFilter.size()) {
				case 0: return list;
				case 1: valueFilter = (String) valuesFilter.get(0);
						if (valueFilter != null && !valueFilter.equals(""))
							return DelegatedBasicListService.filterList(list, valueFilter, valueTypeFilter, 
								objParuse.getFilterColumn(), objParuse.getFilterOperation(), errorHand);
						else return list;
				default: return DelegatedBasicListService.filterList(list, valuesFilter, valueTypeFilter, 
								objParuse.getFilterColumn(), objParuse.getFilterOperation(), errorHand);
			}
		} catch (Exception e) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
					            "filterForCorrelation", "Error while doing filter for corelation ", e);
			return list;
		}
	}
	
	
	
	
}
