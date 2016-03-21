/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.knowage.meta.generator.jpamapping.wrappers.impl;

import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaColumn;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaSubEntity;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaTable;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaView;
import it.eng.spagobi.meta.model.business.BusinessColumnSet;
import it.eng.spagobi.meta.model.business.BusinessRelationship;
import it.eng.spagobi.meta.model.business.BusinessTable;
import it.eng.spagobi.meta.model.business.BusinessView;
import it.eng.spagobi.meta.model.physical.PhysicalTable;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class represent a sub entity that is an entity within a particular
 * joining path.
 *  
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JpaSubEntity implements IJpaSubEntity {
	
	public static final String DESTINATION_ROLE = "structural.destinationRole";
	public static final String FORCE_SUBENTITY_VISIBILITY = "structural.forceVisibilityAsSubentity";
	public static final String ENTITY_VISIBILITY = "structural.visible";



	/**
	 * First level entity = the root of the joining path. It can be a table or
	 * a view.
	 */
	Object root;
	/**
	 * The parent entity = the previous entity in the joining path. It's null if
	 * the previous entity is equal to the root entity
	 */
	JpaSubEntity parent; 
	/**
	 * It's the relationship that link this entity with the previous one (i.e. the parent)
	 * in the joining path
	 */
	BusinessRelationship relationship;

	/**
	 * All the sub entity that have this one as parent entity
	 */
	List<JpaSubEntity> children;

	
	private static Logger logger = LoggerFactory.getLogger(JpaSubEntity.class);

	protected JpaSubEntity(Object root, JpaSubEntity parent, BusinessRelationship relationship) {
		this.root = root;
		this.parent = parent;
		this.relationship = relationship;
		this.children = new ArrayList<JpaSubEntity>();
	}
	
	public static String nameToJavaVariableName(String name) {
		String varName;
		varName = name.replaceAll(" ", "_");
		return varName;
	}

	public JpaSubEntity getParent() {
		return parent;
	}
	
	public List<IJpaSubEntity> getChildren() {
		List<IJpaSubEntity> subEntities = new ArrayList<IJpaSubEntity>();
		
		for(BusinessRelationship r : relationship.getDestinationTable().getRelationships()) {
			if(r.getSourceTable() != relationship.getDestinationTable()) continue;
			
			JpaSubEntity subEntity = new JpaSubEntity(root, this, r);
			subEntities.add(subEntity);
		}
		return subEntities;
	}
	
	public IJpaTable getParentTable() {
		IJpaTable parentTable = null;
		BusinessColumnSet parentColumnSet = relationship.getSourceTable();
		if(parentColumnSet instanceof BusinessTable) {
			parentTable = new JpaTable((BusinessTable)parentColumnSet);
		} else if(parentColumnSet instanceof BusinessView) {
			BusinessView businessView = (BusinessView)parentColumnSet;
			PhysicalTable physicalTable = relationship.getSourceSimpleBusinessColumns().get(0).getPhysicalColumn().getTable();
			parentTable = new JpaViewInnerTable(businessView, physicalTable);
		}
		
		return parentTable;
	}
	
	public IJpaColumn getParentColumn() {
		JpaTable sourceTable = null;
		IJpaColumn sourceColumn = null;
		BusinessColumnSet businessColumnSet = relationship.getSourceColumns().get(0).getTable();
		if (businessColumnSet instanceof BusinessTable){
			sourceTable = new JpaTable( (BusinessTable)relationship.getSourceColumns().get(0).getTable() );
			sourceColumn = new JpaColumn(sourceTable, relationship.getSourceSimpleBusinessColumns().get(0));
		} else if (businessColumnSet instanceof BusinessView){
			//TODO: check this cases
			BusinessView businessView = (BusinessView)relationship.getSourceColumns().get(0).getTable() ;
			PhysicalTable physicalTable = relationship.getSourceSimpleBusinessColumns().get(0).getPhysicalColumn().getTable();
			JpaViewInnerTable sourceView = new JpaViewInnerTable(businessView, physicalTable);
			sourceColumn = new JpaColumn(sourceView, relationship.getSourceSimpleBusinessColumns().get(0));
			//***********
		}
		
		return sourceColumn;
	}
	
	public IJpaTable getTable() {
		IJpaTable table = null;
		BusinessColumnSet columnSet = relationship.getDestinationTable();
		if(columnSet instanceof BusinessTable) {
			table = new JpaTable((BusinessTable)columnSet);
		} else if(columnSet instanceof BusinessView) {
			BusinessView businessView = (BusinessView)columnSet;
			PhysicalTable physicalTable = relationship.getDestinationSimpleBusinessColumns().get(0).getPhysicalColumn().getTable();
			table = new JpaViewInnerTable(businessView, physicalTable);
		}
		
		return table;
	}
	
	public BusinessColumnSet getBusinessColumnSet(){
		BusinessColumnSet columnSet = relationship.getDestinationTable();
		return columnSet;
	}
	
	public String getRootQualifiedClassName() {
		String uniqueName;
		
		uniqueName = null;
		
		if(root instanceof BusinessTable) {
			IJpaTable rootTable = new JpaTable( (BusinessTable)root );
			uniqueName = rootTable.getQualifiedClassName();
		} else if(root instanceof BusinessView) {
			IJpaView rootView = new JpaView( (BusinessView)root );
			uniqueName = rootView.getQualifiedClassName();
		}
		
		return uniqueName;
	}
	
	
	public String getName() {
		String name;
		
		name = null;
		
		IJpaColumn jpaColumn = getParentColumn();
		if (jpaColumn!=null){
			//name = "rel_"+StringUtils.capitalizeFirstLetter(getParentColumn().getPropertyName());
			name = "rel_"+ getParentColumn().getPropertyName();
			name += "_in_" + relationship.getDestinationTable().getUniqueName();
			
			// back compatibility
			String role = name.toLowerCase(); // see ModelEntity getRole()
			name += "(" + role + ")";
							
			name = nameToJavaVariableName(name);
			//name = name.toLowerCase();
		}
		else {
			logger.debug("Cannot retrieve parent column of [{}]",this);
		}
		
		return name;
	}
	
	@Override
	public String getUniqueName() {
		String uniqueName;
		int counter = 0;
		uniqueName = "";
		
		JpaSubEntity targetEntity = this;
		
		IJpaTable table = getTable();
		if(table instanceof JpaViewInnerTable) {
			JpaViewInnerTable innerTable = (JpaViewInnerTable)table;
			JpaView jpaView = new JpaView(innerTable.getBusinessView());
			uniqueName = "/" + innerTable.getBusinessView().getUniqueName() + "/" + innerTable.getBusinessView().getUniqueName();
			targetEntity = targetEntity.getParent();
		}
		
		
		while(targetEntity != null) {
			uniqueName = "//" + targetEntity.getName() + uniqueName;
			targetEntity = targetEntity.getParent();
			counter++;
		}
		
		uniqueName = getRootQualifiedClassName() + uniqueName;
		
		//Important: for entity of level => 3 we use an uniqueName with single / instead of //
		if (counter > 1){
			uniqueName = uniqueName.replace("//", "/");
		}
		
		return uniqueName;
	}
	
	@Override
	public List<String> getColumnUniqueNames() {
		List<String> uniqueNames = new ArrayList<String>();
		
		IJpaTable table = getTable();
		for(IJpaColumn column : table.getColumns()) {
			uniqueNames.add(getUniqueName()  + "/" + column.getUnqualifiedUniqueName());
		}
		
		return uniqueNames;
	}
	
	public List<String> getColumnsNameWithPath()  {
		String subEntityUniqueName = this.getUniqueName();
		List<IJpaColumn> columns = getColumns();
		List<String> columnsNameWithPath = new ArrayList<String>();
		
		for(IJpaColumn column : columns) {
			columnsNameWithPath.add(subEntityUniqueName  + "/" + column.getUnqualifiedUniqueName());
		}
		
		return columnsNameWithPath;
	}
	
	@Override
	public List<String> getColumnNames() {
		List<String> columnsNames = new ArrayList<String>();
		
		IJpaTable table = getTable();
		for(IJpaColumn column : table.getColumns()) {
			columnsNames.add(column.getUnqualifiedUniqueName());
		}
		
		return columnsNames;
	}
	
	@Override
	public List<IJpaColumn> getColumns() {
		IJpaTable table = getTable();
		return table.getColumns();
	}
	
	public String getLabel() {
		return getAttribute("label");
	}
	
	public String getTooltip(){
		return getAttribute("tooltip");
	}
	
	@Override
	public String getAttribute(String name) {
		if ( (name.equals("label")) || (name.equals("tooltip")) ){
			return relationship.getProperties().get(DESTINATION_ROLE).getValue();
		}
		else if (name.equals(ENTITY_VISIBILITY)){
			return getVisibility();
		}
		else {
			return this.getTable().getAttribute(name);
		}

	}
	
	public String getVisibility(){
		String entityVisibility = this.getTable().getAttribute(ENTITY_VISIBILITY);
		if (entityVisibility.equals("true")){
			return entityVisibility;
		} else {
			return relationship.getProperties().get(FORCE_SUBENTITY_VISIBILITY).getValue();
		}
	}
}
