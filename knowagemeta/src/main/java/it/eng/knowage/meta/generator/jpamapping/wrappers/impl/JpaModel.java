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
