/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.services;

import it.eng.spago.base.Constants;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.dbaccess.DataConnectionManager;
import it.eng.spago.dbaccess.Utils;
import it.eng.spago.dbaccess.sql.DataConnection;
import it.eng.spago.dbaccess.sql.DataRow;
import it.eng.spago.dbaccess.sql.SQLCommand;
import it.eng.spago.dbaccess.sql.result.DataResult;
import it.eng.spago.dbaccess.sql.result.ScrollableDataResult;
import it.eng.spago.dispatching.service.RequestContextIFace;
import it.eng.spago.dispatching.service.ServiceIFace;
import it.eng.spago.dispatching.service.list.basic.IFaceBasicListService;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.init.InitializerIFace;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spago.paginator.basic.PaginatorIFace;
import it.eng.spago.paginator.basic.impl.GenericList;
import it.eng.spago.paginator.basic.impl.GenericPaginator;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spago.util.QueryExecutor;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.PortletUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.assertion.Assert;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Provides all methods to handle a list of objects. Its methods are called from a list
 * module class to get the objects list.
 * 
 * @author sulis
 */
public class DelegatedBasicListService {
	public static final String LIST_PAGE = "LIST_PAGE";
	public static final String LIST_FIRST = "LIST_FIRST";
	public static final String LIST_PREV = "LIST_PREV";
	public static final String LIST_NEXT = "LIST_NEXT";
	public static final String LIST_LAST = "LIST_LAST";
	public static final String LIST_CURRENT = "LIST_CURRENT";
	public static final String LIST_NOCACHE = "LIST_NOCACHE";
	public static final String LIST_DELETE = "LIST_DELETE";

	/**
	 * Instantiates a new delegated basic list service.
	 */
	public DelegatedBasicListService() {
		super();
	} // private KFDelegatedBasicListService()
	
	/**
	 * The service method for this class.
	 * 
	 * @param service The service interface object
	 * @param request The request Source Bean
	 * @param response The response Source Bean
	 * 
	 * @throws Exception If any Exception occurred
	 */
	public static void service(ServiceIFace service, SourceBean request, SourceBean response) throws Exception {
		if ((service == null) || (request == null) || (response == null)) {
			TracerSingleton.log(
				Constants.NOME_MODULO,
				TracerSingleton.WARNING,
				"KFDelegatedBasicListService::service: parametri non validi");
			return;
		} 
		
		TracerSingleton.log(
			Constants.NOME_MODULO,
			TracerSingleton.DEBUG,
			"KFDelegatedBasicListService::service: request",
			request);
		
		String message = getMessage(request);
		if ((message == null) || message.equalsIgnoreCase("BEGIN"))
			message = LIST_FIRST;
		String list_nocache = (String) request.getAttribute(LIST_NOCACHE);
		if (list_nocache == null)
			list_nocache = "FALSE";
		IFaceBasicListService listService = (IFaceBasicListService) service;
		if (message.equalsIgnoreCase(LIST_DELETE)) {
			listService.delete(request, response);
			message = LIST_PAGE;
			list_nocache = "TRUE";
		} // if (message.equalsIgnoreCase(LIST_DELETE))
		if ((listService.getList() == null) || list_nocache.equalsIgnoreCase("TRUE"))
			listService.setList(listService.getList(request, response));
		if (listService.getList() == null) {
			TracerSingleton.log(
				Constants.NOME_MODULO,
				TracerSingleton.WARNING,
				"KFDelegatedBasicListService::service: _list nullo");
			return;
		} // if (listService.getList() == null)
		int pagedListNumber = 1;
		if (message.equalsIgnoreCase(LIST_PAGE)) {
			String list_page = (String) request.getAttribute(LIST_PAGE);
			if (list_page == null)
				list_page = "1";
			try {
				pagedListNumber = Integer.parseInt(list_page);
			} // try
			catch (Exception ex) {
				TracerSingleton.log(
					Constants.NOME_MODULO,
					TracerSingleton.CRITICAL,
					"KFDelegatedBasicListService::service: Integer.parseInt(list_page)",
					ex);
			} // catch (Exception ex) try
		} // if (message.equalsIgnoreCase(LIST_PAGE))
		else if (message.equalsIgnoreCase(LIST_FIRST))
			pagedListNumber = 1;
		else if (message.equalsIgnoreCase(LIST_PREV))
			pagedListNumber = listService.getList().getPrevPage();
		else if (message.equalsIgnoreCase(LIST_NEXT))
			pagedListNumber = listService.getList().getNextPage();
		else if (message.equalsIgnoreCase(LIST_LAST))
			pagedListNumber = listService.getList().pages();
		else if (message.equalsIgnoreCase(LIST_CURRENT))
			pagedListNumber = listService.getList().getCurrentPage();
		TracerSingleton.log(
			Constants.NOME_MODULO,
			TracerSingleton.DEBUG,
			"KFDelegatedBasicListService::service: pagedListNumber [" + pagedListNumber + "]");
		listService.getList().clearDynamicData();
		listService.callback(request, response, listService.getList(), pagedListNumber);
		SourceBean pagedList = listService.getList().getPagedList(pagedListNumber);
		if (pagedList == null) {
			TracerSingleton.log(
				Constants.NOME_MODULO,
				TracerSingleton.WARNING,
				"KFDelegatedBasicListService::service: pagedList nullo");
			return;
		} // if (pagedList == null)
		try {
			response.setAttribute(pagedList);
		} // try
		catch (SourceBeanException ex) {
			TracerSingleton.log(
				Constants.NOME_MODULO,
				TracerSingleton.CRITICAL,
				"KFDelegatedBasicListService::service: response.setAttribute(pagedList)",
				ex);
		} // catch (SourceBeanException ex) try
		TracerSingleton.log(
			Constants.NOME_MODULO,
			TracerSingleton.DEBUG,
			"KFDelegatedBasicListService::service: response",
			response);
	} // public static void service(ServiceIFace service, SourceBean request,

	// SourceBean response) throws Exception
	/**
	 * Gets the list for a particular SpagoBI object.
	 * 
	 * @param service The service interface object
	 * @param request The request Source Bean
	 * @param response The response Source Bean
	 * 
	 * @return the list
	 * 
	 * @throws Exception If any exception occurred
	 */
	public static ListIFace getList(ServiceIFace service, SourceBean request, SourceBean response) throws Exception {
		PaginatorIFace paginator = new GenericPaginator();
		InitializerIFace serviceInitializer = (InitializerIFace) service;
		RequestContextIFace serviceRequestContext = (RequestContextIFace) service;
		int pagedRows = 10;
		SourceBean rowsSourceBean = null;
		SourceBean config = serviceInitializer.getConfig();
		if (config != null) {
			pagedRows = Integer.parseInt((String) serviceInitializer.getConfig().getAttribute("ROWS"));
			paginator.setPageSize(pagedRows);
			String pool = (String) serviceInitializer.getConfig().getAttribute("POOL");
			SourceBean statement = (SourceBean) serviceInitializer.getConfig().getAttribute("QUERIES.SELECT_QUERY");
			rowsSourceBean =
				(SourceBean) QueryExecutor.executeQuery(
					serviceRequestContext.getRequestContainer(),
					serviceRequestContext.getResponseContainer(),
					pool,
					statement,
					"SELECT");
		} else {
			// in case the query is dinamically created, the config SourceBean is in response
			config = (SourceBean) response.getAttribute("CONFIG");
			pagedRows = Integer.parseInt((String) config.getAttribute("ROWS"));
			paginator.setPageSize(pagedRows);
			String pool = (String) config.getAttribute("POOL");
			String statement = (String) ((SourceBean) config.getAttribute("QUERIES.SELECT_QUERY")).getAttribute("statement");
			rowsSourceBean = (SourceBean) executeSelect(serviceRequestContext
					.getRequestContainer(), serviceRequestContext
					.getResponseContainer(), pool, statement);
		}

		List rowsVector = null;
		if (rowsSourceBean != null)
			rowsVector = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
		if ((rowsSourceBean == null)) {//|| (rowsVector.size() == 0)) {
			EMFErrorHandler engErrorHandler = serviceRequestContext.getErrorHandler();
			engErrorHandler.addError(new EMFUserError(EMFErrorSeverity.INFORMATION, 10001));
		} // if ((rowsSourceBean == null) || (rowsVector.size() == 0))
		else
			for (int i = 0; i < rowsVector.size(); i++)
				paginator.addRow(rowsVector.get(i));
		
				
		
        // chiamo la funzione di ricerca nelle colonne (se non vi sono dati di ricerca la funzione
		// non esegue nessuna operazione) 
		// SearchTextInList.search(request, response, pagedRows, rowsVector);
		
				
		
		ListIFace list = new GenericList();
		list.setPaginator(paginator);
		// list.addStaticData(firstData);
		
		// filter the list 
		String valuefilter = (String) request.getAttribute(SpagoBIConstants.VALUE_FILTER);
//		if((valuefilter!=null) && !(valuefilter.trim().equals(""))) {
//			list = filterList(list, valuefilter, request);
//		}
		if (valuefilter != null) {
			String columnfilter = (String) request
					.getAttribute(SpagoBIConstants.COLUMN_FILTER);
			String typeFilter = (String) request
					.getAttribute(SpagoBIConstants.TYPE_FILTER);
			String typeValueFilter = (String) request
					.getAttribute(SpagoBIConstants.TYPE_VALUE_FILTER);
			list = filterList(list, valuefilter, typeValueFilter, columnfilter, typeFilter, serviceRequestContext.getErrorHandler());
		}
		
		return list;
	} // public static ListIFace getList(ServiceIFace service, SourceBean

	
	/**
	 * Filters the list with a list of filtering values.
	 * 
	 * @param list The list to be filtered
	 * @param valuesfilter The list of filtering values
	 * @param valuetypefilter The type of the value of the filter (STRING/NUM/DATE)
	 * @param columnfilter The column to be filtered
	 * @param typeFilter The type of the filter
	 * @param errorHandler The EMFErrorHandler object, in which errors are stored if they occurs
	 * 
	 * @return the filtered list
	 */
	public static ListIFace filterList(ListIFace list, List valuesfilter, String valuetypefilter, String columnfilter, 
						String typeFilter, EMFErrorHandler errorHandler) {
		if ((valuesfilter == null) || (valuesfilter.size() ==0)) {
			return list;
		}
		if ((columnfilter == null) || (columnfilter.trim().equals(""))) {
			return list;
		}
		if ((typeFilter == null) || (typeFilter.trim().equals(""))) {
			return list;
		}
		if ((valuetypefilter == null) || (valuetypefilter.trim().equals(""))) {
			return list;
		}
		if (typeFilter.equalsIgnoreCase(SpagoBIConstants.LESS_FILTER)
				|| typeFilter.equalsIgnoreCase(SpagoBIConstants.LESS_OR_EQUAL_FILTER)
				|| typeFilter.equalsIgnoreCase(SpagoBIConstants.GREATER_FILTER)
				|| typeFilter.equalsIgnoreCase(SpagoBIConstants.GREATER_OR_EQUAL_FILTER)) {
			TracerSingleton.log(
					Constants.NOME_MODULO,
					TracerSingleton.WARNING,
					"DelegatedBasicListService::filterList with a list of filtering values: the filter type " + typeFilter + " is not applicable for multi-values filtering.");
			String labelTypeFilter = "";
			if (typeFilter.equalsIgnoreCase(SpagoBIConstants.LESS_FILTER))
				labelTypeFilter = PortletUtilities.getMessage("SBIListLookPage.isLessThan", "messages");
			else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.LESS_OR_EQUAL_FILTER))
				labelTypeFilter = PortletUtilities.getMessage("SBIListLookPage.isLessOrEqualThan", "messages");
			else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.GREATER_FILTER))
				labelTypeFilter = PortletUtilities.getMessage("SBIListLookPage.isGreaterThan", "messages");
			else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.GREATER_OR_EQUAL_FILTER))
				labelTypeFilter = PortletUtilities.getMessage("SBIListLookPage.isGreaterOrEqualThan", "messages");
			HashMap params = new HashMap();
			params.put(Constants.NOME_MODULO,
					"DelegatedBasicListService::filterList with a list of filtering values");
			Vector v = new Vector();
			v.add(labelTypeFilter);
			EMFValidationError error = new EMFValidationError(EMFErrorSeverity.WARNING, SpagoBIConstants.TYPE_FILTER, "1069", v, params);
			errorHandler.addError(error);
			return list;
		}

		// controls the correctness of the filtering conditions
		boolean filterConditionsAreCorrect = verifyFilterConditions(valuetypefilter, typeFilter, errorHandler);
		if (!filterConditionsAreCorrect) return list;

		PaginatorIFace newPaginator = new GenericPaginator();
		newPaginator.setPageSize(list.getPaginator().getPageSize());
		SourceBean allrowsSB = list.getPaginator().getAll();
		List rows = allrowsSB.getAttributeAsList("ROW");
		Iterator iterRow = rows.iterator();
		while (iterRow.hasNext()) {
			SourceBean row = (SourceBean) iterRow.next();
			boolean doesRowSatisfyCondition = false;
			Iterator valuesfilterIt = valuesfilter.iterator();
			while (valuesfilterIt.hasNext()) {
				String valuefilter = (String) valuesfilterIt.next();
				try {
					if (valuefilter != null && !valuefilter.equals(""))
						doesRowSatisfyCondition = 
							doesRowSatisfyCondition(row, valuefilter, valuetypefilter, columnfilter, typeFilter);
					else doesRowSatisfyCondition = true;
				} catch (EMFValidationError error) {
					errorHandler.addError(error);
					return list;
				}
				if (doesRowSatisfyCondition) break;
			}
			if (doesRowSatisfyCondition) newPaginator.addRow(row);
		}
		ListIFace newList = new GenericList();
		newList.setPaginator(newPaginator);
		return newList;
	}
	
	/**
	 * Filters the list with a unique value filter.
	 * 
	 * @param list The list to be filtered
	 * @param valuefilter The value of the filter
	 * @param valuetypefilter The type of the value of the filter (STRING/NUM/DATE)
	 * @param columnfilter The column to be filtered
	 * @param typeFilter The type of the filter
	 * @param errorHandler The EMFErrorHandler object, in which errors are stored if they occurs
	 * 
	 * @return the filtered list
	 */
	public static SourceBean filterList(SourceBean allrowsSB, String valuefilter, String valuetypefilter, String columnfilter, 
						String typeFilter, EMFErrorHandler errorHandler) {
		if ((valuefilter == null) || (valuefilter.equals(""))) {
			TracerSingleton.log(
					Constants.NOME_MODULO,
					TracerSingleton.WARNING,
					"DelegatedBasicListService::filterList: the value filter is not set.");
			HashMap params = new HashMap();
			params.put(Constants.NOME_MODULO,
					"DelegatedBasicListService::filterList");
			EMFValidationError error = new EMFValidationError(EMFErrorSeverity.WARNING, SpagoBIConstants.VALUE_FILTER, "1070", null, params);
			if(errorHandler!=null) {
				errorHandler.addError(error);
			}
			return allrowsSB;
		}
		if ((columnfilter == null) || (columnfilter.trim().equals(""))) {
			return allrowsSB;
		}
		if ((typeFilter == null) || (typeFilter.trim().equals(""))) {
			return allrowsSB;
		}
		if ((valuetypefilter == null) || (valuetypefilter.trim().equals(""))) {
			return allrowsSB;
		}
		// controls the correctness of the filtering conditions
		boolean filterConditionsAreCorrect = verifyFilterConditions(valuetypefilter, typeFilter, errorHandler);
		if (!filterConditionsAreCorrect) return allrowsSB;

		PaginatorIFace newPaginator = new GenericPaginator();
		
		List rows = allrowsSB.getAttributeAsList("ROW");
		Iterator iterRow = rows.iterator();
		while (iterRow.hasNext()) {
			SourceBean row = (SourceBean) iterRow.next();
			boolean doesRowSatisfyCondition = false;
			try {
				doesRowSatisfyCondition = doesRowSatisfyCondition(row, valuefilter, valuetypefilter, columnfilter, typeFilter);
			} catch (EMFValidationError error) {
				if(errorHandler!=null) {
					errorHandler.addError(error);
				}
				return allrowsSB;
			}
			if (doesRowSatisfyCondition) newPaginator.addRow(row);
		}
		ListIFace newList = new GenericList();
		newList.setPaginator(newPaginator);
		return newPaginator.getAll();
	}
	
	/**
	 * Filters the list with a unique value filter.
	 * 
	 * @param list The list to be filtered
	 * @param valuefilter The value of the filter
	 * @param valuetypefilter The type of the value of the filter (STRING/NUM/DATE)
	 * @param columnfilter The column to be filtered
	 * @param typeFilter The type of the filter
	 * @param errorHandler The EMFErrorHandler object, in which errors are stored if they occurs
	 * 
	 * @return the filtered list
	 */
	public static ListIFace filterList(ListIFace list, String valuefilter, String valuetypefilter, String columnfilter, 
						String typeFilter, EMFErrorHandler errorHandler) {
		if ((valuefilter == null) || (valuefilter.equals(""))) {
			TracerSingleton.log(
					Constants.NOME_MODULO,
					TracerSingleton.WARNING,
					"DelegatedBasicListService::filterList: the value filter is not set.");
			HashMap params = new HashMap();
			params.put(Constants.NOME_MODULO,
					"DelegatedBasicListService::filterList");
			EMFValidationError error = new EMFValidationError(EMFErrorSeverity.WARNING, SpagoBIConstants.VALUE_FILTER, "1070", null, params);
			if(errorHandler!=null) {
				errorHandler.addError(error);
			}
			return list;
		}
		if ((columnfilter == null) || (columnfilter.trim().equals(""))) {
			return list;
		}
		if ((typeFilter == null) || (typeFilter.trim().equals(""))) {
			return list;
		}
		if ((valuetypefilter == null) || (valuetypefilter.trim().equals(""))) {
			return list;
		}
		// controls the correctness of the filtering conditions
		boolean filterConditionsAreCorrect = verifyFilterConditions(valuetypefilter, typeFilter, errorHandler);
		if (!filterConditionsAreCorrect) return list;

		PaginatorIFace newPaginator = new GenericPaginator();
		newPaginator.setPageSize(list.getPaginator().getPageSize());
		SourceBean allrowsSB = list.getPaginator().getAll();
		List rows = allrowsSB.getAttributeAsList("ROW");
		Iterator iterRow = rows.iterator();
		while (iterRow.hasNext()) {
			SourceBean row = (SourceBean) iterRow.next();
			boolean doesRowSatisfyCondition = false;
			try {
				doesRowSatisfyCondition = doesRowSatisfyCondition(row, valuefilter, valuetypefilter, columnfilter, typeFilter);
			} catch (EMFValidationError error) {
				if(errorHandler!=null) {
					errorHandler.addError(error);
				}
				return list;
			}
			if (doesRowSatisfyCondition) newPaginator.addRow(row);
		}
		ListIFace newList = new GenericList();
		newList.setPaginator(newPaginator);
		return newList;
	}
	
	private static boolean verifyFilterConditions(String valuetypefilter, String typeFilter, EMFErrorHandler errorHandler) {
		// case of number filtering
		if (valuetypefilter.equalsIgnoreCase(SpagoBIConstants.NUMBER_TYPE_FILTER)) {
			if (typeFilter.equalsIgnoreCase(SpagoBIConstants.START_FILTER)
					|| typeFilter.equalsIgnoreCase(SpagoBIConstants.END_FILTER)
					|| typeFilter.equalsIgnoreCase(SpagoBIConstants.CONTAIN_FILTER)) {
				TracerSingleton.log(
						Constants.NOME_MODULO,
						TracerSingleton.WARNING,
						"DelegatedBasicListService::filterList: the filter type " + typeFilter + " is not applicable to numbers.");
				String labelTypeFilter = "";
				if (typeFilter.equalsIgnoreCase(SpagoBIConstants.START_FILTER))
					labelTypeFilter = PortletUtilities.getMessage("SBIListLookPage.startWith", "messages");
				else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.END_FILTER))
					labelTypeFilter = PortletUtilities.getMessage("SBIListLookPage.endWith", "messages");
				else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.CONTAIN_FILTER))
					labelTypeFilter = PortletUtilities.getMessage("SBIListLookPage.contains", "messages");
				HashMap params = new HashMap();
				params.put(Constants.NOME_MODULO,
						"DelegatedBasicListService::filterList");
				Vector v = new Vector();
				v.add(labelTypeFilter);
				EMFValidationError error = new EMFValidationError(EMFErrorSeverity.WARNING, SpagoBIConstants.TYPE_FILTER, "1050", v, params);
				errorHandler.addError(error);
				return false;
			} else return true;
		}
		// case of date filtering
		if (valuetypefilter.equalsIgnoreCase(SpagoBIConstants.DATE_TYPE_FILTER)) {
			if (typeFilter.equalsIgnoreCase(SpagoBIConstants.START_FILTER)
					|| typeFilter.equalsIgnoreCase(SpagoBIConstants.END_FILTER)
					|| typeFilter.equalsIgnoreCase(SpagoBIConstants.CONTAIN_FILTER)) {
				TracerSingleton.log(
						Constants.NOME_MODULO,
						TracerSingleton.WARNING,
						"DelegatedBasicListService::filterList: the filter type " + typeFilter + " is not applicable to date.");
				String labelTypeFilter = "";
				if (typeFilter.equalsIgnoreCase(SpagoBIConstants.START_FILTER))
					labelTypeFilter = PortletUtilities.getMessage("SBIListLookPage.startWith", "messages");
				else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.END_FILTER))
					labelTypeFilter = PortletUtilities.getMessage("SBIListLookPage.endWith", "messages");
				else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.CONTAIN_FILTER))
					labelTypeFilter = PortletUtilities.getMessage("SBIListLookPage.contains", "messages");
				HashMap params = new HashMap();
				params.put(Constants.NOME_MODULO,
						"DelegatedBasicListService::filterList");
				Vector v = new Vector();
				v.add(labelTypeFilter);
				EMFValidationError error = new EMFValidationError(EMFErrorSeverity.WARNING, SpagoBIConstants.TYPE_FILTER, "1053", v, params);
				errorHandler.addError(error);
				return false;
			} else return true;
		} else return true;
	}
	
	
	/**
	 * Filters the list with a list of filtering values.
	 * 
	 * @param list The list to be filtered
	 * @param valuesfilter The list of filtering values
	 * @param valuetypefilter The type of the value of the filter (STRING/NUM/DATE)
	 * @param columnfilter The column to be filtered
	 * @param typeFilter The type of the filter
	 * @param errorHandler The EMFErrorHandler object, in which errors are stored if they occurs
	 * 
	 * @return the filtered list
	 */
	public static List filterList(List list, String[] valuesfilter, String valuetypefilter, String columnfilter, 
						String typeFilter) {
		
		
		List newList = new ArrayList();
		
		if ((valuesfilter == null) || (valuesfilter.length ==0)) {
			return list;
		}
		
		if (StringUtilities.isEmpty(columnfilter)) {
			return list;
		}
		if (StringUtilities.isEmpty(typeFilter)) {
			return list;
		}
		
		if (StringUtilities.isEmpty(valuetypefilter)) {
			return list;
		}
		
		if (typeFilter.equalsIgnoreCase(SpagoBIConstants.LESS_FILTER)
				|| typeFilter.equalsIgnoreCase(SpagoBIConstants.LESS_OR_EQUAL_FILTER)
				|| typeFilter.equalsIgnoreCase(SpagoBIConstants.GREATER_FILTER)
				|| typeFilter.equalsIgnoreCase(SpagoBIConstants.GREATER_OR_EQUAL_FILTER)) {

			Assert.assertUnreachable("filterList with a list of filtering values: the filter type " + typeFilter + " is not applicable for multi-values filtering.");
		}

		// controls the correctness of the filtering conditions
		//boolean filterConditionsAreCorrect = verifyFilterConditions(valuetypefilter, typeFilter, errorHandler);
		//if (!filterConditionsAreCorrect) return list;

		
		Iterator iterRow = list.iterator();
		while (iterRow.hasNext()) {
			SourceBean row = (SourceBean) iterRow.next();
			boolean doesRowSatisfyCondition = false;
			for(int i = 0; i < valuesfilter.length; i++) {
				String valuefilter = valuesfilter[i];
				try {
					if (valuefilter != null && !valuefilter.equals(""))
						doesRowSatisfyCondition = 
							doesRowSatisfyCondition(row, valuefilter, valuetypefilter, columnfilter, typeFilter);
					else doesRowSatisfyCondition = true;
				} catch (EMFValidationError error) {
					error.printStackTrace();
					return list;
				}
				if (doesRowSatisfyCondition) break;
			}
			if (doesRowSatisfyCondition) newList.add(row);
		}
	
		return newList;
	}
	
	
	public static List filterList(List list, String valuefilter,
			String valuetypefilter, String columnfilter, String typeFilter) {

		List newList = new ArrayList();

		Assert.assertTrue(!StringUtilities.isEmpty(valuefilter),
				"the value filter is not set");

		if (StringUtilities.isEmpty(columnfilter)) {
			return list;
		}
		if (StringUtilities.isEmpty(typeFilter)) {
			return list;
		}

		if (StringUtilities.isEmpty(valuetypefilter)) {
			return list;
		}

		// controls the correctness of the filtering conditions
		// boolean filterConditionsAreCorrect =
		// verifyFilterConditions(valuetypefilter, typeFilter, errorHandler);
		// if (!filterConditionsAreCorrect) return list;

		Iterator iterRow = list.iterator();
		while (iterRow.hasNext()) {
			SourceBean row = (SourceBean) iterRow.next();
			boolean doesRowSatisfyCondition = false;
			try {
				doesRowSatisfyCondition = doesRowSatisfyCondition(row,
						valuefilter, valuetypefilter, columnfilter, typeFilter);
			} catch (EMFValidationError error) {
				error.printStackTrace();
				return list;
			}
			if (doesRowSatisfyCondition)
				newList.add(row);
		}

		return newList;
	}
	
	private static boolean doesRowSatisfyCondition(SourceBean row, String valuefilter, String valuetypefilter, String columnfilter, 
			String typeFilter) throws EMFValidationError {
		Object attribute = row.getAttribute(columnfilter);
		if (attribute == null) return false;
		String value = attribute.toString();
		if (value == null)
			value = "";
		// case of string filtering
		if (valuetypefilter.equalsIgnoreCase(SpagoBIConstants.STRING_TYPE_FILTER)) {
			valuefilter = valuefilter.toUpperCase();
			value = value.toUpperCase();
			if (typeFilter.equalsIgnoreCase(SpagoBIConstants.START_FILTER)) {
				return value.trim().startsWith(valuefilter);
			} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.END_FILTER)) {
				return value.trim().endsWith(valuefilter);
			} else if (typeFilter
					.equalsIgnoreCase(SpagoBIConstants.CONTAIN_FILTER)) {
				return value.indexOf(valuefilter) != -1;
			} else if (typeFilter
					.equalsIgnoreCase(SpagoBIConstants.EQUAL_FILTER)) {
				return value.equals(valuefilter)
						|| value.trim().equals(valuefilter);
			} else if (typeFilter
					.equalsIgnoreCase(SpagoBIConstants.LESS_FILTER)) {
				return value.trim().compareToIgnoreCase(valuefilter) < 0; 
			} else if (typeFilter
					.equalsIgnoreCase(SpagoBIConstants.LESS_OR_EQUAL_FILTER)) {
				return value.trim().compareToIgnoreCase(valuefilter) <= 0;
			} else if (typeFilter
					.equalsIgnoreCase(SpagoBIConstants.GREATER_FILTER)) {
				return value.trim().compareToIgnoreCase(valuefilter) > 0;
			} else if (typeFilter
					.equalsIgnoreCase(SpagoBIConstants.GREATER_OR_EQUAL_FILTER)) {
				return value.trim().compareToIgnoreCase(valuefilter) >= 0;
			} else {
				TracerSingleton.log(
						Constants.NOME_MODULO,
						TracerSingleton.WARNING,
						"DelegatedBasicListService::filterList: the filter type '" + typeFilter + "' is not a valid filter type");
				HashMap params = new HashMap();
				params.put(Constants.NOME_MODULO,
						"DelegatedBasicListService::filterList: the filter type '" + typeFilter + "' is not a valid filter type");
				EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, SpagoBIConstants.TYPE_FILTER, "100", null, params);
				throw error;
			}
		}
		// case of number filtering
		else if (valuetypefilter.equalsIgnoreCase(SpagoBIConstants.NUMBER_TYPE_FILTER)) {
			Double valueDouble = null;
			Double valueFilterDouble = null;
			try {
				valueDouble = new Double(value);
			} catch (Exception e) {
				TracerSingleton.log(
						Constants.NOME_MODULO,
						TracerSingleton.WARNING,
						"DelegatedBasicListService::filterList: the string value is not a recognizable number representations: value to be filtered = " + value, e);
				HashMap params = new HashMap();
				params.put(Constants.NOME_MODULO,
						"DelegatedBasicListService::filterList");
				Vector v = new Vector();
				v.add(value);
				EMFValidationError error = new EMFValidationError(EMFErrorSeverity.WARNING, SpagoBIConstants.TYPE_VALUE_FILTER, "1051", v, params);
				throw error;
			}
			try {
				valueFilterDouble = new Double(valuefilter);
			} catch (Exception e) {
				TracerSingleton.log(
						Constants.NOME_MODULO,
						TracerSingleton.WARNING,
						"DelegatedBasicListService::filterList: input string value is not a recognizable number representations: filter value = " + valuefilter, e);
				HashMap params = new HashMap();
				params.put(Constants.NOME_MODULO,
						"DelegatedBasicListService::filterList");
				Vector v = new Vector();
				v.add(valuefilter);
				EMFValidationError error = new EMFValidationError(EMFErrorSeverity.WARNING, SpagoBIConstants.VALUE_FILTER, "1052", v, params);
				throw error;
			}
			
			//if (valueDouble == null || valueFilterDouble == null) return list;
			
			if (typeFilter
					.equalsIgnoreCase(SpagoBIConstants.EQUAL_FILTER)) {
				return valueDouble.doubleValue() == valueFilterDouble.doubleValue();
			} else if (typeFilter
					.equalsIgnoreCase(SpagoBIConstants.LESS_FILTER)) {
				return valueDouble.doubleValue() < valueFilterDouble.doubleValue();
			} else if (typeFilter
					.equalsIgnoreCase(SpagoBIConstants.LESS_OR_EQUAL_FILTER)) {
				return valueDouble.doubleValue() <= valueFilterDouble.doubleValue();
			} else if (typeFilter
					.equalsIgnoreCase(SpagoBIConstants.GREATER_FILTER)) {
				return valueDouble.doubleValue() > valueFilterDouble.doubleValue();
			} else if (typeFilter
					.equalsIgnoreCase(SpagoBIConstants.GREATER_OR_EQUAL_FILTER)) {
				return valueDouble.doubleValue() >= valueFilterDouble.doubleValue();
			} else {
				TracerSingleton.log(
						Constants.NOME_MODULO,
						TracerSingleton.WARNING,
						"DelegatedBasicListService::filterList: the filter type '" + typeFilter + "' is not a valid filter type");
				HashMap params = new HashMap();
				params.put(Constants.NOME_MODULO,
						"DelegatedBasicListService::filterList: the filter type '" + typeFilter + "' is not a valid filter type");
				EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, SpagoBIConstants.TYPE_FILTER, "100", null, params);
				throw error;
			}
		}
		// case of date filtering
		else if (valuetypefilter.equalsIgnoreCase(SpagoBIConstants.DATE_TYPE_FILTER)) {

		    String format = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format");
		    TracerSingleton.log(
					Constants.NOME_MODULO,
					TracerSingleton.WARNING,
					"DelegatedBasicListService::filterList: applying date format " + format + " for filtering.");
//		    format = format.replaceAll("D", "d");
//		    format = format.replaceAll("m", "M");
//		    format = format.replaceAll("Y", "y");
	        Date valueDate = null;
	        Date valueFilterDate = null;
			try {
				valueDate = toDate(value, format);
	        } catch (Exception e) { 
				TracerSingleton.log(
						Constants.NOME_MODULO,
						TracerSingleton.WARNING,
						"DelegatedBasicListService::filterList: the string value is not a valid date representation according to the format " + format + ": value to be filtered = " + value, e);
				HashMap params = new HashMap();
				params.put(Constants.NOME_MODULO,
						"DelegatedBasicListService::filterList");
				Vector v = new Vector();
				v.add(value);
				v.add(format);
				EMFValidationError error = new EMFValidationError(EMFErrorSeverity.WARNING, SpagoBIConstants.TYPE_VALUE_FILTER, "1054", v, params);
				throw error;
	        }
			try {
				valueFilterDate = toDate(valuefilter, format);
	        } catch (Exception e) { 
				TracerSingleton.log(
						Constants.NOME_MODULO,
						TracerSingleton.WARNING,
						"DelegatedBasicListService::filterList: input string is not a valid date representation according to the format " + format + ": filter value = " + valuefilter, e);
				HashMap params = new HashMap();
				params.put(Constants.NOME_MODULO,
						"DelegatedBasicListService::filterList");
				Vector v = new Vector();
				v.add(valuefilter);
				v.add(format);
				EMFValidationError error = new EMFValidationError(EMFErrorSeverity.WARNING, SpagoBIConstants.VALUE_FILTER, "1055", v, params);
				throw error;
	        }
	        
	        //if (valueDate == null || valueFilterDate == null) return list;
	        
			if (typeFilter
					.equalsIgnoreCase(SpagoBIConstants.EQUAL_FILTER)) {
				return valueDate.compareTo(valueFilterDate) == 0;
			} else if (typeFilter
					.equalsIgnoreCase(SpagoBIConstants.LESS_FILTER)) {
				return valueDate.compareTo(valueFilterDate) < 0;
			} else if (typeFilter
					.equalsIgnoreCase(SpagoBIConstants.LESS_OR_EQUAL_FILTER)) {
				return valueDate.compareTo(valueFilterDate) <= 0;
			} else if (typeFilter
					.equalsIgnoreCase(SpagoBIConstants.GREATER_FILTER)) {
				return valueDate.compareTo(valueFilterDate) > 0;
			} else if (typeFilter
					.equalsIgnoreCase(SpagoBIConstants.GREATER_OR_EQUAL_FILTER)) {
				return valueDate.compareTo(valueFilterDate) >= 0;
			} else {
				TracerSingleton.log(
						Constants.NOME_MODULO,
						TracerSingleton.WARNING,
						"DelegatedBasicListService::filterList: the filter type '" + typeFilter + "' is not a valid filter type");
				HashMap params = new HashMap();
				params.put(Constants.NOME_MODULO,
						"DelegatedBasicListService::filterList: the filter type '" + typeFilter + "' is not a valid filter type");
				EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, SpagoBIConstants.TYPE_FILTER, "100", null, params);
				throw error;
			}
		}
		else {
			TracerSingleton.log(
					Constants.NOME_MODULO,
					TracerSingleton.WARNING,
					"DelegatedBasicListService::filterList: the filter value type '" + valuetypefilter + "' is not a valid filter value type");
			HashMap params = new HashMap();
			params.put(Constants.NOME_MODULO,
					"DelegatedBasicListService::filterList: the filter value type '" + valuetypefilter + "' is not a valid filter value type");
			EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, SpagoBIConstants.TYPE_FILTER, "100", null, params);
			throw error;
		}
	}
	
	
	/**
	 * Converts a String representing a date into a Date object, given the date format.
	 * 
	 * @param dateStr The String representing the date
	 * @param format The date format
	 * 
	 * @return the relevant Date object
	 * 
	 * @throws Exception if any parsing exception occurs
	 */
	public static Date toDate(String dateStr, String format) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat();
        Date date = null;
		try {
            dateFormat.applyPattern(format);
            dateFormat.setLenient(false);
            date = dateFormat.parse(dateStr);
        } catch (Exception e) { 
        	throw e;
        }
        return date;
	}
	
	/**
	 * Executes a select statement.
	 * 
	 * @param requestContainer The request container object
	 * @param responseContainer The response container object
	 * @param pool The pool definition string
	 * @param statement The statement definition string
	 * 
	 * @return A generic object containing the Execution results
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	 public static Object executeSelect(RequestContainer requestContainer,
			ResponseContainer responseContainer, String pool, String statement) throws EMFInternalError {
		Object result = null;
		DataConnectionManager dataConnectionManager = null;
		DataConnection dataConnection = null;
		SQLCommand sqlCommand = null;
		DataResult dataResult = null;
		try {
			dataConnectionManager = DataConnectionManager.getInstance();
			dataConnection = dataConnectionManager.getConnection(pool);
			sqlCommand = dataConnection.createSelectCommand(statement);
			dataResult = sqlCommand.execute();
			ScrollableDataResult scrollableDataResult = (ScrollableDataResult) dataResult
					.getDataObject();
			result = scrollableDataResult.getSourceBean();
		} catch (Exception ex) {
			TracerSingleton.log(Constants.NOME_MODULO,
					TracerSingleton.CRITICAL, "executeSelect:", ex);
		} finally {
			Utils.releaseResources(dataConnection, sqlCommand, dataResult);
		}
		return result;
	}
	
	/**
	 * Function that controls if the deletion of a row in a DB table has success or not.
	 * 
	 * @param service The service interface object
	 * @param request The request Source Bean
	 * @param response The response SourceBean
	 * 
	 * @return Boolean true (succeeded) or false (not succeeded)
	 */
	public static boolean delete(ServiceIFace service, SourceBean request, SourceBean response) {
		InitializerIFace serviceInitializer = (InitializerIFace) service;
		String pool = (String) serviceInitializer.getConfig().getAttribute("POOL");
		SourceBean statement = (SourceBean) serviceInitializer.getConfig().getAttribute("QUERIES.DELETE_QUERY");
		RequestContextIFace serviceRequestContext = (RequestContextIFace) service;
		Boolean isOK =
			(Boolean) QueryExecutor.executeQuery(
				serviceRequestContext.getRequestContainer(),
				serviceRequestContext.getResponseContainer(),
				pool,
				statement,
				"DELETE");
		EMFErrorHandler engErrorHandler = serviceRequestContext.getErrorHandler();
		if ((isOK != null) && isOK.booleanValue()) {
			engErrorHandler.addError(new EMFUserError(EMFErrorSeverity.INFORMATION, 10002));
			return true;
		} // if ((isOK != null) && isOK.booleanValue())
		TracerSingleton.log(
			Constants.NOME_MODULO,
			TracerSingleton.MAJOR,
			"KFDelegatedBasicListService::delete: errore cancellazione riga");
		engErrorHandler.addError(new EMFUserError(EMFErrorSeverity.WARNING, 10003));
		return false;
	} // public static boolean delete( ServiceIFace service, SourceBean request,

	// SourceBean response)
	/**
	 * Gets the information contained in a Source Bean attribute
	 * identified by the key "MESSAGE".
	 * 
	 * @param request The input Source Bean
	 * 
	 * @return the message
	 */
	public static String getMessage(SourceBean request) {
		return (String) request.getAttribute("MESSAGE");
	} // public static String getMessage(SourceBean request)
} // public class KFDelegatedBasicListService
