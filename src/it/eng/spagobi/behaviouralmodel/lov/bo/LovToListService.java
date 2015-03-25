/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.lov.bo;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spago.paginator.basic.PaginatorIFace;
import it.eng.spago.paginator.basic.impl.GenericList;
import it.eng.spago.paginator.basic.impl.GenericPaginator;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.services.AbstractListLookupModule;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

import java.util.List;

public class LovToListService extends AbstractListLookupModule  {
	
	/**
	 * lov result string
	 */
	private String lovResult = null;
	
	private ILovDetail lovDetail = null;
	
	/**
	 * constructor.
	 * 
	 * @param lovDet the lov det
	 * @param profile the profile
	 */
	public LovToListService(ILovDetail lovDet, IEngUserProfile profile) {
		this.lovDetail = lovDet;
		try {
			this.lovResult = this.lovDetail.getLovResult(profile, null, null,null);
		} catch (Exception e) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(),
					            "LovToListService", "Error while getting lov result " + e);
		}
		
	}
	
	
	/**
	 * constructor.
	 * 
	 * @param lovRes the lov res
	 */
	public LovToListService(String lovRes) {
		this.lovResult = lovRes;
	}
	
		
	/**
	 * Gets the Spago List interface of the lov result.
	 * 
	 * @return the Spago List interface of the lov result
	 * 
	 * @throws Exception the exception
	 */
	public ListIFace getLovAsListService() throws Exception {
		ListIFace list = null;	
		LovResultHandler lovResultHandler = new LovResultHandler( getLovResult() );
		SourceBean lovResultSB = lovResultHandler.getLovResultSB();
		PaginatorIFace paginator = new GenericPaginator();
		
		int numRows = 10;
		try{
			SingletonConfig spagoconfig = SingletonConfig.getInstance();
			String lookupnumRows = spagoconfig.getConfigValue("SPAGOBI.LOOKUP.numberRows");
			if(lookupnumRows!=null) {
				numRows = Integer.parseInt(lookupnumRows);
			}
		} catch(Exception e) {
			numRows = 10;
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(),
					            "getListServiceBaseConfig", "Error while recovering number rows for " +
					            "lookup from configuration, usign default 10", e);
		}
		paginator.setPageSize(numRows);
		
		
		List rows = null;
		if (lovResultSB != null) {
			rows = lovResultSB.getAttributeAsList("ROW");
			for (int i = 0; i < rows.size(); i++)
				paginator.addRow(rows.get(i));
		}
		list = new GenericList();
		list.setPaginator(paginator);
		return list;
	}

	
	/**
	 * Create the configuration for the lov list.
	 * 
	 * @param title the title of the lov list
	 * 
	 * @return the sourcebean which contains the configuration for the lov list
	 * 
	 * @throws Exception the exception
	 */
	public SourceBean getListServiceBaseConfig(String title) throws Exception {
		SourceBean config = null;
		List visibleColumns = lovDetail.getVisibleColumnNames();				
		String valueColumn = lovDetail.getValueColumnName();
		String moduleConfigStr = "";
		moduleConfigStr += "<CONFIG rows=\"10\" title=\"" + title + "\">";
		moduleConfigStr += "	<KEYS>";
		moduleConfigStr += "		<OBJECT key='"+ valueColumn +"'/>";
		moduleConfigStr += "	</KEYS>";
		moduleConfigStr += "	<QUERIES/>";
		moduleConfigStr += "</CONFIG>";
		config = SourceBean.fromXMLString(moduleConfigStr);
		SourceBean columnsSB = createColumnsSB(visibleColumns);
		config.setAttribute(columnsSB);
		return config;
	}
	
	/**
	 * Creates the configuration for the columns of the lov list
	 * @param columns the list of columns name
	 * @return the sourcebean of the columns list configuration
	 * @throws SourceBeanException
	 */
	private SourceBean createColumnsSB(List columns) throws SourceBeanException {
		if (columns == null || columns.size() == 0) return new SourceBean("COLUMNS");
		String columnsStr = "<COLUMNS>";
		for (int i = 0; i < columns.size(); i++) {
			columnsStr += "	<COLUMN name=\"" + columns.get(i).toString() + "\" />";
		}
		columnsStr += "</COLUMNS>";
		SourceBean columnsSB = SourceBean.fromXMLString(columnsStr);
		return columnsSB;
	}
	
	/**
	 * Gets the lov result string.
	 * 
	 * @return lov result string
	 */
	public String getLovResult() {
		return lovResult;
	}

	/**
	 * Sets the lov result string.
	 * 
	 * @param lovResult the lov result string to set
	 */
	public void setLovResult(String lovResult) {
		this.lovResult = lovResult;
	}

	/**
	 * Gets the lov detail.
	 * 
	 * @return the lov detail
	 */
	public ILovDetail getLovDetail() {
		return lovDetail;
	}

	/**
	 * Sets the lov detail.
	 * 
	 * @param lovDetail the new lov detail
	 */
	public void setLovDetail(ILovDetail lovDetail) {
		this.lovDetail = lovDetail;
	}


	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.list.basic.IFaceBasicListService#getList(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public ListIFace getList(SourceBean arg0, SourceBean arg1) throws Exception {
		return getLovAsListService();
	}
	

}
