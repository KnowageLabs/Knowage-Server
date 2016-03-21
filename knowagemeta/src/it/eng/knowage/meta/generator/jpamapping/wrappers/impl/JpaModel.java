/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.knowage.meta.generator.jpamapping.wrappers.impl;

import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaTable;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaView;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.business.BusinessView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JpaModel {
	
	private BusinessModel businessModel;
	
	private List<IJpaTable> tables;
	private List<IJpaView> views;
	private String persistenceUnitName;
	
	public JpaModel(BusinessModel businessModel) {
		this.businessModel = businessModel;
		this.tables = wrapTables(businessModel.getBusinessTables());
		this.views = wrapViews(businessModel.getBusinessViews());
	}
	
	public List<IJpaTable> getTables() {
		return tables;
	}
	
	public List<IJpaView> getViews() {
		return views;
	}
	
	public String getName() {
		return businessModel.getName();
	}
	
	public String getDescription() {
		return businessModel.getName();
	}
	
	public String getPersistenceUnitName() {
		return persistenceUnitName == null? getName(): persistenceUnitName;
	}
	
	public void setPersistenceUnitName(String persistenceUnitName) {
		this.persistenceUnitName = persistenceUnitName;
	}
	
	private List<IJpaTable> wrapTables(List<BusinessTable> tables) {
		List<IJpaTable> jpaTables;
		
		jpaTables = new ArrayList<IJpaTable>();
		for(BusinessTable table : tables) {
			if (table.getPhysicalTable()!=null)
				jpaTables.add( new JpaTable(table) );
		}
		
		return jpaTables;
	}
	
	private List<IJpaView> wrapViews(List<BusinessView> views) {
		List<IJpaView> jpaViews;
		
		jpaViews = new ArrayList<IJpaView>();
		for(BusinessView view : views) {
			jpaViews.add(  new JpaView(view) );
		}
		
		return jpaViews;
	}
	
}
