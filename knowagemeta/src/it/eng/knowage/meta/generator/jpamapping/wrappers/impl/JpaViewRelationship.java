/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.knowage.meta.generator.jpamapping.wrappers.impl;

import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaColumn;
import it.eng.knowage.meta.generator.utils.JavaKeywordsUtils;
import it.eng.knowage.meta.generator.utils.StringUtils;
import it.eng.knowage.meta.model.business.BusinessView;
import it.eng.knowage.meta.model.business.BusinessViewInnerJoinRelationship;
import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalTable;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @deprecated until further testing
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JpaViewRelationship extends AbstractJpaRelationship {

	BusinessViewInnerJoinRelationship businessInnerRelationship;
	
	private static Logger logger = LoggerFactory.getLogger(JpaViewRelationship.class);
	
	protected JpaViewRelationship(JpaViewInnerTable jpaInnerTable, BusinessViewInnerJoinRelationship businessInnerRelationship) {
		this.jpaTable = jpaInnerTable;
		this.businessInnerRelationship = businessInnerRelationship;
		
		if ( isSourceRole() ){
			this.cardinality = JpaRelationship.MANY_TO_ONE;
		} else if ( isDestinationRole() ){
			this.cardinality = JpaRelationship.ONE_TO_MANY;				
		}
		
		this.bidirectional = true;
	}
	
	public boolean isSourceRole() {
		return businessInnerRelationship.getSourceTable().equals( jpaTable.getPhysicalTable() );
	}
	
	public boolean isDestinationRole() {
		return businessInnerRelationship.getDestinationTable().equals( jpaTable.getPhysicalTable() );
	}

	/**
	 * return true if the BC is included into the Physical column list
	 * @param phy
	 * @param column
	 * @return
	 */
	protected PhysicalColumn findPhysicalColumn (List<PhysicalColumn> fColumn,PhysicalColumn bColumn){
		for (PhysicalColumn fc : fColumn){
			if (bColumn.getName().equals(fc.getName())){
				logger.info("FOUND the "+fc.getName()+" Physical Column");
				return fc;
			}
		}	
		logger.info("No Physical Column FOUND");
		return null;
	}
	
	public BusinessViewInnerJoinRelationship getBusinessInnerRelationship() {
		return businessInnerRelationship;
	}
	public void setBusinessInnerRelationship(
			BusinessViewInnerJoinRelationship businessInnerRelationship) {
		this.businessInnerRelationship = businessInnerRelationship;
	}
	/**
	 * return the destination Physical table of the relationship
	 * @param bv the destination BV of the relationship
	 * @param columns ...
	 * @return
	 */
	private PhysicalTable findPhysicalTable(BusinessView bv,List<PhysicalColumn> columns){
		// the destination physical tables
		List<PhysicalTable> physicaltables=bv.getPhysicalTables();
		PhysicalTable result=null;
		for (PhysicalTable phyt : physicaltables){
			boolean found=false;
			for (PhysicalColumn bc : columns){
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
	 * The inner relationship can have join reference between PhisicalTable only
	 */
	public AbstractJpaTable getReferencedTable(){
		logger.debug("IN");
		
		BusinessView businessView = ((JpaViewInnerTable)jpaTable).getBusinessView();
		
		if ( isSourceRole() ) {			
			if (businessInnerRelationship.getDestinationTable() instanceof PhysicalTable){
				return new JpaViewInnerTable(businessView,(PhysicalTable)businessInnerRelationship.getDestinationTable()); 
			}else {
				logger.error("businessInnerRelationship.getDestinationTable() IS not a PhysicalTable......");
			}
		} else {
			if (businessInnerRelationship.getSourceTable() instanceof PhysicalTable){
				return new JpaViewInnerTable(businessView,(PhysicalTable)businessInnerRelationship.getSourceTable()); 
			}else {
				logger.error("businessInnerRelationship.getSourceTable() IS not a PhysicalTable......");
			}
		}
		logger.error("getReferencedTable() return null......");
		return null;
	
	}
	
	@Override
	public String getSimpleSourceColumnName(){
		return StringUtils.doubleQuote(businessInnerRelationship.getSourceColumns().get(0).getName());
	}
	@Override
	public String getPropertyName(){
		if (businessInnerRelationship.getSourceColumns()!=null){
			return JavaKeywordsUtils.transformToJavaPropertyName( businessInnerRelationship.getSourceColumns().get(0).getName());
		}
		else return "";
	}
	@Override
	public String getOppositeRoleName(){
		return JavaKeywordsUtils.transformToJavaPropertyName( businessInnerRelationship.getSourceColumns().get(0).getName());	
	}
	@Override
	public String getBidirectionalPropertyName(){
		if (businessInnerRelationship.getName() != null) 
			return StringUtils.pluralise(JavaKeywordsUtils.transformToJavaPropertyName( businessInnerRelationship.getName()));
		else 
			return StringUtils.pluralise(JavaKeywordsUtils.transformToJavaPropertyName("innerJoin"));
	}

	/* (non-Javadoc)
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaRelationship#isMultipleRelationship()
	 */
	@Override
	public boolean isMultipleRelationship() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaRelationship#getRelationshipColumnsNames()
	 */
	@Override
	public List<JpaRelationshipColumnsNames> getRelationshipColumnsNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IJpaColumn> getSourceColumns() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IJpaColumn> getDestinationColumns() {
		// TODO Auto-generated method stub
		return null;
	}	
}
