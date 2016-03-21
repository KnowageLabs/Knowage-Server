/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.knowage.meta.generator.jpamapping.wrappers.impl;

import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaColumn;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaTable;
import it.eng.knowage.meta.generator.utils.JavaKeywordsUtils;
import it.eng.spagobi.meta.model.business.BusinessColumn;
import it.eng.spagobi.meta.model.business.BusinessColumnSet;
import it.eng.spagobi.meta.model.business.BusinessRelationship;
import it.eng.spagobi.meta.model.business.BusinessTable;
import it.eng.spagobi.meta.model.business.BusinessView;
import it.eng.spagobi.meta.model.business.SimpleBusinessColumn;
import it.eng.spagobi.meta.model.physical.PhysicalColumn;
import it.eng.spagobi.meta.model.physical.PhysicalTable;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class JpaViewOuterRelationship {
	
	BusinessRelationship businessRelationship;
	JpaView jpaView;
	boolean isOutbound;
	private static Logger logger = LoggerFactory.getLogger(JpaViewOuterRelationship.class);
	boolean isSourceTableView = false;
	boolean isDestinationTableView = false;


	protected JpaViewOuterRelationship(JpaView jpaView, BusinessRelationship businessRelationship, boolean isOutbound){
		this.jpaView = jpaView;
		this.businessRelationship = businessRelationship;
		this.isOutbound = isOutbound;
	}

	public String getSourceTable() {
		IJpaTable jpaTable = null;
		String name = null;
		BusinessColumnSet businessColumnSet = businessRelationship.getSourceTable();
		if (businessColumnSet instanceof BusinessTable){
			jpaTable = new JpaTable((BusinessTable)businessColumnSet);
			name = jpaTable.getClassName();
		} else if (businessColumnSet instanceof BusinessView){
			BusinessView businessView = (BusinessView)businessColumnSet;
			name = JavaKeywordsUtils.transformToJavaClassName(businessView.getName());
			isSourceTableView = true;
		}
		return name;

	}
	
	public String getDestinationTable() {
		IJpaTable jpaTable = null;
		String name = null;
		BusinessColumnSet businessColumnSet = businessRelationship.getDestinationTable();
		if (businessColumnSet instanceof BusinessTable){		
			jpaTable = new JpaTable((BusinessTable)businessColumnSet);
			name = jpaTable.getClassName();
		} else if (businessColumnSet instanceof BusinessView){			
			BusinessView businessView = (BusinessView)businessColumnSet;
			name = JavaKeywordsUtils.transformToJavaClassName(businessView.getName());
			isDestinationTableView = true;
		}
		return name;
	}
	
	
	public List<String> getSourceColumns() {
		List<IJpaColumn> sourceColumns;
		List<PhysicalColumn> columns = new ArrayList<PhysicalColumn>();
		List<SimpleBusinessColumn> businessColumns;
		
 		List<String> columnsNames = new ArrayList<String>();
		
		sourceColumns = new ArrayList<IJpaColumn>();
		businessColumns = businessRelationship.getSourceSimpleBusinessColumns();
		for (SimpleBusinessColumn businessColumn : businessColumns){
			columns.add(businessColumn.getPhysicalColumn());
		}
		
		//check if is a outbound relationship
		if(isOutboundRelationship()){
			for (BusinessColumn businessColumn : businessColumns){
				columnsNames.add(JavaKeywordsUtils.transformToJavaPropertyName(businessColumn.getUniqueName()));
			}

		} else {
			//inbound relationship
			AbstractJpaTable jpaTable = null;
			if(businessRelationship.getSourceTable() instanceof BusinessTable) {
				jpaTable = new JpaTable((BusinessTable)businessRelationship.getSourceTable());
			}else if (businessRelationship.getSourceTable() instanceof BusinessView){
				PhysicalTable physicalTMP=findPhysicalTable((BusinessView)businessRelationship.getSourceTable(),businessRelationship.getSourceSimpleBusinessColumns());
				jpaTable = new JpaViewInnerTable((BusinessView)businessRelationship.getSourceTable(),physicalTMP); 
			}
			
			for(SimpleBusinessColumn businessColumn: businessColumns) {
				JpaColumn jpaColumn = new JpaColumn(jpaTable, businessColumn);
				if (!sourceColumns.contains(jpaColumn))
					sourceColumns.add( jpaColumn );
			}
			
			for (IJpaColumn sourceColumn:sourceColumns){
				String name;
				if (sourceColumn.getJpaTable().hasFakePrimaryKey()|| ( sourceColumn.getJpaTable().hasCompositeKey() && sourceColumn.isIdentifier())){
					name = sourceColumn.getJpaTable().getCompositeKeyPropertyName()+"."+sourceColumn.getPropertyName();
				}
				else {
					name = sourceColumn.getPropertyName();
				}
				columnsNames.add(name);
					
			}
		}

		return columnsNames;
	}
	
	
	public List<String> getDestinationColumns(){
		List<IJpaColumn> destinationColumns;
		List<PhysicalColumn> columns = new ArrayList();
		List<SimpleBusinessColumn> businessColumns;
		List<IJpaTable> innerTables;
 		List<String> columnsNames = new ArrayList<String>();

		
 		
		destinationColumns = new ArrayList<IJpaColumn>();
		businessColumns = businessRelationship.getDestinationSimpleBusinessColumns();
		for (SimpleBusinessColumn businessColumn : businessColumns){
			columns.add(businessColumn.getPhysicalColumn());
		}
		innerTables = jpaView.getInnerTables();
		
		//check if is a inbound relationship
		if(!isOutboundRelationship()){
			for (BusinessColumn businessColumn : businessColumns){
				columnsNames.add(JavaKeywordsUtils.transformToJavaPropertyName(businessColumn.getUniqueName()));
			}
		} else {
			//outbound relationship
			AbstractJpaTable jpaTable = null;
			if(businessRelationship.getDestinationTable() instanceof BusinessTable) {
				jpaTable = new JpaTable((BusinessTable)businessRelationship.getDestinationTable());
			}else if (businessRelationship.getDestinationTable() instanceof BusinessView){
				PhysicalTable physicalTMP=findPhysicalTable((BusinessView)businessRelationship.getDestinationTable(),businessRelationship.getDestinationSimpleBusinessColumns());
				jpaTable = new JpaViewInnerTable((BusinessView)businessRelationship.getDestinationTable(),physicalTMP); 
			}
			
			for(SimpleBusinessColumn businessColumn: businessColumns) {
				JpaColumn jpaColumn = new JpaColumn(jpaTable, businessColumn);
				if (!destinationColumns.contains(jpaColumn))
					destinationColumns.add( jpaColumn );
			}
			
			for (IJpaColumn destinationColumn:destinationColumns){
				String name;
				if (destinationColumn.getJpaTable().hasFakePrimaryKey()|| ( destinationColumn.getJpaTable().hasCompositeKey() && destinationColumn.isIdentifier())){
					name = destinationColumn.getJpaTable().getCompositeKeyPropertyName()+"."+destinationColumn.getPropertyName();
				}
				else {
					name = destinationColumn.getPropertyName();
				}
				columnsNames.add(name);
					
			}
		}

		return columnsNames;
	}
	
	
	public String getPackage(){
		return jpaView.getPackage();
	}
	
	//check if is a outbound relationship
	public boolean isOutboundRelationship(){
		return isOutbound;
	}
	
	/**
	 * @return the isSourceTableView
	 */
	public boolean isSourceTableView() {
		return isSourceTableView;
	}
	
	public String getSourceTableView(){
		return new Boolean(isSourceTableView).toString();
	}
	
	/**
	 * @return the isDestinationTableView
	 */
	public boolean isDestinationTableView() {
		return isDestinationTableView;
	}
	
	public String getDestinationTableView(){
		return new Boolean(isDestinationTableView).toString();
	}
	
	
	
	

	private PhysicalTable findPhysicalTable(BusinessView bv,List<SimpleBusinessColumn> columns){
		// the destination physical tables
		List<PhysicalTable> physicaltables=bv.getPhysicalTables();
		PhysicalTable result=null;
		for (PhysicalTable phyt : physicaltables){
			boolean found=false;
			for (SimpleBusinessColumn bc : columns){
				PhysicalColumn fc=findPhysicalColumn(phyt.getColumns(),bc);
				if (fc != null){
					logger.info("Physical Column FOUND "+bc.getName());
					found=true;
				}
					
			}
			if (found) result=phyt;
		}
		return result;
	}
	
	/**
	 * return true if the BC is included into the Physical column list
	 * @param phy
	 * @param column
	 * @return
	 */
	protected PhysicalColumn findPhysicalColumn (List<PhysicalColumn> fColumn,SimpleBusinessColumn bColumn){
		for (PhysicalColumn fc : fColumn){
			if (bColumn.getPhysicalColumn().getName().equals(fc.getName())){
				logger.info("FOUND the "+fc.getName()+" Physical Column");
				return fc;
			}
		}	
		logger.info("No Physical Column FOUND");
		return null;
	}

}
