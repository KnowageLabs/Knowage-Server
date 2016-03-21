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
import it.eng.knowage.meta.initializer.properties.PhysicalModelPropertiesFromFileInitializer;
import it.eng.knowage.meta.model.ModelProperty;
import it.eng.knowage.meta.model.business.BusinessRelationship;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.business.BusinessView;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;
import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalModel;
import it.eng.knowage.meta.model.physical.PhysicalTable;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JpaRelationship extends AbstractJpaRelationship {
	public static final String RELATIONSHIP_CARDINALITY = "structural.cardinality";

	String quoteString;
	
	BusinessRelationship businessRelationship;

	private static Logger logger = LoggerFactory.getLogger(JpaRelationship.class);

	
	
	protected JpaRelationship(AbstractJpaTable jpaTable, BusinessRelationship businessRelationship) {
		
		Assert.assertNotNull("Parameter [jpaTable] cannot be null", jpaTable);
		Assert.assertNotNull("Parameter [businessRelationship] cannot be null", businessRelationship);
		Assert.assertNotNull("Parameter [businessRelationship] must have a source table", businessRelationship.getSourceTable());
		Assert.assertNotNull("Parameter [businessRelationship] must have a destination table", businessRelationship.getDestinationTable());
		
		this.jpaTable = jpaTable;
		this.businessRelationship = businessRelationship;
		
		
		
		PhysicalModel physicalModel = jpaTable.getModel().getPhysicalModel();
		ModelProperty modelProperty = physicalModel.getProperties().get(PhysicalModelPropertiesFromFileInitializer.CONNECTION_DATABASE_QUOTESTRING);
		if (modelProperty != null) {
			quoteString = modelProperty.getValue();
		} else {
			quoteString = "";
		}	
		
		/*
		if ( isSourceRole() ){
			this.cardinality = JpaRelationship.MANY_TO_ONE;
		} else if ( isDestinationRole() ){
			this.cardinality = JpaRelationship.ONE_TO_MANY;				
		}
		*/
		
		//Set Cardinality
		ModelProperty propertyCardinality = businessRelationship.getProperties().get(RELATIONSHIP_CARDINALITY);
		if (propertyCardinality != null){
			String cardinalityValue = propertyCardinality.getValue();
			
			if ( isSourceRole() ){
				this.cardinality = cardinalityValue;
			} else if ( isDestinationRole() ){
				if (cardinalityValue.equals(JpaRelationship.ONE_TO_MANY)){
					//setting inverse cardinality
					this.cardinality = JpaRelationship.MANY_TO_ONE;
				} else if (cardinalityValue.equals(JpaRelationship.MANY_TO_ONE)){
					//setting inverse cardinality
					this.cardinality = JpaRelationship.ONE_TO_MANY;
				} else if (cardinalityValue.equals(JpaRelationship.ONE_TO_ONE)){
					//setting inverse cardinality
					this.cardinality = JpaRelationship.ONE_TO_MANY;
				} else if (cardinalityValue.equals(JpaRelationship.OPTIONAL_ONE_TO_ONE)){
					//setting inverse cardinality
					this.cardinality = JpaRelationship.ONE_TO_OPTIONAL_ONE;
				} else if (cardinalityValue.equals(JpaRelationship.ONE_TO_OPTIONAL_ONE)){
					//setting inverse cardinality
					this.cardinality = JpaRelationship.OPTIONAL_ONE_TO_ONE;
				} else if (cardinalityValue.equals(JpaRelationship.OPTIONAL_ONE_TO_MANY)){
					//setting inverse cardinality
					this.cardinality = JpaRelationship.MANY_TO_OPTIONAL_ONE;
				} else if (cardinalityValue.equals(JpaRelationship.ONE_TO_OPTIONAL_MANY)){
					//setting inverse cardinality
					this.cardinality = JpaRelationship.OPTIONAL_MANY_TO_ONE;
				} else if (cardinalityValue.equals(JpaRelationship.OPTIONAL_MANY_TO_ONE)){
					//settare cardinalit� inversa
					this.cardinality = JpaRelationship.ONE_TO_OPTIONAL_MANY;
				}  else if (cardinalityValue.equals(JpaRelationship.MANY_TO_OPTIONAL_ONE)){
					//settare cardinalit� inversa
					this.cardinality = JpaRelationship.OPTIONAL_ONE_TO_MANY;
				}
				
			}
			
		} else {
			// for retrocompatibility (version older than 4.0)
			if ( isSourceRole() ){
				this.cardinality = JpaRelationship.MANY_TO_ONE;
			} else if ( isDestinationRole() ){
				this.cardinality = JpaRelationship.ONE_TO_MANY;				
			}
		}

		
		this.bidirectional = true;
	}
	
	

	public boolean isDestinationRole() {
		boolean isSourceRole;
		
		if(jpaTable instanceof JpaTable) {
			isSourceRole = businessRelationship.getDestinationTable().equals(((JpaTable)jpaTable).getBusinessTable());
		} else {
			isSourceRole = businessRelationship.getDestinationTable().equals( ((JpaViewInnerTable)jpaTable).getBusinessView() );
		}
		
		return isSourceRole;
	}
	
	public boolean isSourceRole() {
		boolean isSourceRole;
		
		if(jpaTable instanceof JpaTable) {
			isSourceRole = businessRelationship.getSourceTable().equals(((JpaTable)jpaTable).getBusinessTable());
		} else {
			isSourceRole = businessRelationship.getSourceTable().equals( ((JpaViewInnerTable)jpaTable).getBusinessView() );
		}
		
		return isSourceRole;
	}
	
	/**
	 * return the destination Physical table of the relationship
	 * @param bv the destination BV of the relationship
	 * @param columns ...
	 * @return
	 */
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

	public AbstractJpaTable getReferencedTable(){
		
		if ( isSourceRole() ) {
			
			if(businessRelationship.getDestinationTable() instanceof BusinessTable) {
				return new JpaTable((BusinessTable)businessRelationship.getDestinationTable());
			}else if (businessRelationship.getDestinationTable() instanceof BusinessView){
				PhysicalTable physicalTMP=findPhysicalTable((BusinessView)businessRelationship.getDestinationTable(),businessRelationship.getDestinationSimpleBusinessColumns());
				return new JpaViewInnerTable((BusinessView)businessRelationship.getDestinationTable(),physicalTMP); 
			}
		} else {
			if(businessRelationship.getSourceTable() instanceof BusinessTable) {
				return new JpaTable((BusinessTable)businessRelationship.getSourceTable());
			}else if (businessRelationship.getSourceTable() instanceof BusinessView){
				PhysicalTable physicalTMP=findPhysicalTable((BusinessView)businessRelationship.getSourceTable(),businessRelationship.getSourceSimpleBusinessColumns());
				return new JpaViewInnerTable((BusinessView)businessRelationship.getSourceTable(),physicalTMP); 

			}
		}
		return null;
		
	}
	

	public BusinessRelationship getBusinessRelationship() {
		return businessRelationship;
	}

	public String getPropertyName(){
		if (getBusinessRelationship().getSourceColumns()!=null){
			return JavaKeywordsUtils.transformToJavaPropertyName( "rel_"+getBusinessRelationship().getSourceColumns().get(0).getUniqueName()+"_in_"+getBusinessRelationship().getDestinationTable().getUniqueName());
		}
		else return "";
	}
	
	public String getOppositeRoleName(){
		return JavaKeywordsUtils.transformToJavaPropertyName( "rel_"+getBusinessRelationship().getSourceColumns().get(0).getUniqueName()+"_in_"+getBusinessRelationship().getDestinationTable().getUniqueName());	
	}
	
	public String getBidirectionalPropertyName(){
		return StringUtils.pluralise(JavaKeywordsUtils.transformToJavaPropertyName( getBusinessRelationship().getName()+"_"+getBusinessRelationship().getSourceTable().getName()));
	}	
	
	public List<IJpaColumn> getSourceColumns(){
		List<IJpaColumn> jpaSourceColumns = new ArrayList<IJpaColumn>();
		List<SimpleBusinessColumn> sourceColumns = getBusinessRelationship().getSourceSimpleBusinessColumns();
		for (SimpleBusinessColumn column : sourceColumns ){
			jpaSourceColumns.add( new JpaColumn(getJpaTable(), column) );
		}
		return jpaSourceColumns;
	}
	
	public List<IJpaColumn> getDestinationColumns(){
		List<IJpaColumn> jpaDestinationColumns = new ArrayList<IJpaColumn>();
		List<SimpleBusinessColumn> destinationColumns = getBusinessRelationship().getDestinationSimpleBusinessColumns();
		for (SimpleBusinessColumn column : destinationColumns ){
			jpaDestinationColumns.add( new JpaColumn(this.getReferencedTable(), column) );
		}
		return jpaDestinationColumns;
	}
	
	/**
	 * true if the relationship involve more than one column from the source table (and so also at the
	 * destination table because the number of source columns must be always equal to the number of
	 * destination columns
	 */
	public boolean isMultipleRelationship(){
		if (getBusinessRelationship().getSourceColumns().size() > 1){
			return true;
		} else {
			return false;
		}			
	}
	
	/**
	 * facility method. returns the sql name on the single source column involved in the relationship
	 */
	public String getSimpleSourceColumnName(){

		return StringUtils.doubleQuote(quoteString+getBusinessRelationship().getSourceSimpleBusinessColumns().get(0).getPhysicalColumn().getName()+quoteString);
	}
	
	public List<String> getSimpleSourceColumnsNames(){
		List<String> sourceColumnsNames = new ArrayList<String>();
		if (isMultipleRelationship()){
			List<SimpleBusinessColumn> sourceColumns = getBusinessRelationship().getSourceSimpleBusinessColumns();
			for (SimpleBusinessColumn column : sourceColumns ){
				sourceColumnsNames.add( StringUtils.doubleQuote(column.getPhysicalColumn().getName()));
			}
		}
		return sourceColumnsNames;
	}
	
	public List<String> getSimpleDestinationColumnsNames(){
		List<String> destinationColumnsNames = new ArrayList<String>();
		if (isMultipleRelationship()){
			List<SimpleBusinessColumn> destinationColumns = getBusinessRelationship().getDestinationSimpleBusinessColumns();
			for (SimpleBusinessColumn column : destinationColumns ){
				destinationColumnsNames.add( StringUtils.doubleQuote(column.getPhysicalColumn().getName()));
			}
		}
		return destinationColumnsNames;
	}
	
	public List<JpaRelationshipColumnsNames> getRelationshipColumnsNames(){
		
		List<JpaRelationshipColumnsNames> relationshipColumnsNames = new ArrayList<JpaRelationshipColumnsNames>();
		if (isMultipleRelationship()){
			List<SimpleBusinessColumn> destinationColumns = getBusinessRelationship().getDestinationSimpleBusinessColumns();
			List<SimpleBusinessColumn> sourceColumns = getBusinessRelationship().getSourceSimpleBusinessColumns();


			for (int i=0; i < sourceColumns.size(); i++){
				
				String sourceColumnName = StringUtils.doubleQuote(quoteString+sourceColumns.get(i).getPhysicalColumn().getName()+quoteString);
				String destinationColumnName =  StringUtils.doubleQuote(quoteString+destinationColumns.get(i).getPhysicalColumn().getName()+quoteString);
				JpaRelationshipColumnsNames relationshipNames = new JpaRelationshipColumnsNames(sourceColumnName,destinationColumnName );
				relationshipColumnsNames.add(relationshipNames);
			}
		}
		return relationshipColumnsNames;
	}
	
	
}
