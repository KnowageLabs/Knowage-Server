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
import it.eng.spagobi.meta.model.business.BusinessColumn;
import it.eng.spagobi.meta.model.business.BusinessView;
import it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship;
import it.eng.spagobi.meta.model.business.SimpleBusinessColumn;
import it.eng.spagobi.meta.model.physical.PhysicalColumn;
import it.eng.spagobi.meta.model.physical.PhysicalTable;

import java.util.ArrayList;
import java.util.List;

public class JpaViewInnerJoinRelatioship {
	
	BusinessView businessView;
	BusinessViewInnerJoinRelationship joinRelationship;
	
	protected JpaViewInnerJoinRelatioship(BusinessView businessView, BusinessViewInnerJoinRelationship joinRelationship) {
		this.businessView = businessView;
		this.joinRelationship = joinRelationship;
	}
	
	public IJpaTable getSourceTable() {
		IJpaTable jpaTable;
		PhysicalTable viewInnerTable = joinRelationship.getSourceTable();
		jpaTable = new JpaViewInnerTable(businessView, viewInnerTable);
		
		return jpaTable;
	}
	
	public IJpaTable getDestinationTable() {
		IJpaTable jpaTable;
		PhysicalTable viewInnerTable = joinRelationship.getDestinationTable();
		jpaTable = new JpaViewInnerTable(businessView, viewInnerTable);
		
		return jpaTable;
	}
	
	public List<IJpaColumn> getSourceColumns() {
		List<IJpaColumn> sourceColumns;
		List<PhysicalColumn> columns;
		JpaViewInnerTable innerSourceTable;
		
		sourceColumns = new ArrayList<IJpaColumn>();
		columns = joinRelationship.getSourceColumns();
		innerSourceTable = (JpaViewInnerTable)getSourceTable();
		for(PhysicalColumn physicalColumn: columns) {
			BusinessColumn businessColumn = innerSourceTable.findColumnInBusinessView(physicalColumn);
			if(businessColumn != null){
				if (businessColumn instanceof SimpleBusinessColumn){
					JpaColumn jpaColumn = new JpaColumn(innerSourceTable, (SimpleBusinessColumn)businessColumn);
					sourceColumns.add( jpaColumn );
				}
			}
		}
		
		return sourceColumns;
	}
	
	public List<IJpaColumn> getDestinationColumns() {
		List<IJpaColumn> destinationColumns;
		List<PhysicalColumn> columns;
		JpaViewInnerTable innerDestinationTable;
		
		destinationColumns = new ArrayList<IJpaColumn>();
		columns = joinRelationship.getDestinationColumns();
		innerDestinationTable = (JpaViewInnerTable)getDestinationTable();
		for(PhysicalColumn physicalColumn: columns) {
			BusinessColumn businessColumn = innerDestinationTable.findColumnInBusinessView(physicalColumn);
			if(businessColumn != null){
				if (businessColumn instanceof SimpleBusinessColumn){
					JpaColumn jpaColumn = new JpaColumn(innerDestinationTable, (SimpleBusinessColumn)businessColumn);
					destinationColumns.add( jpaColumn );
				}
			} else {
				JpaFakeColumn fakeColumn = new JpaFakeColumn(innerDestinationTable, physicalColumn);
				destinationColumns.add( fakeColumn );
			}
		}
		
		return destinationColumns;
	}
}
