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

import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaColumn;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaTable;
import it.eng.knowage.meta.model.business.BusinessColumn;
import it.eng.knowage.meta.model.business.BusinessView;
import it.eng.knowage.meta.model.business.BusinessViewInnerJoinRelationship;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;
import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalTable;

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
