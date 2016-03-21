/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.knowage.meta.generator.mondrianschema.wrappers.impl;

import java.util.ArrayList;
import java.util.List;

import it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianCube;
import it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianCubeDimesion;
import it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianMeasure;
import it.eng.spagobi.meta.model.business.BusinessColumnSet;
import it.eng.spagobi.meta.model.business.BusinessRelationship;
import it.eng.spagobi.meta.model.business.BusinessTable;
import it.eng.spagobi.meta.model.olap.Cube;
import it.eng.spagobi.meta.model.olap.Dimension;
import it.eng.spagobi.meta.model.olap.Measure;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class MondrianCube implements IMondrianCube {
	
	Cube cube;
	
	/**
	 * @param cube
	 */
	public MondrianCube(Cube cube) {
		this.cube = cube;
	}

	/* (non-Javadoc)
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianCube#getCubeName()
	 */
	@Override
	public String getName() {
		return cube.getName();
	}

	/* (non-Javadoc)
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianCube#getTableName()
	 */
	@Override
	public String getTableName() {
		String tableName = null;
		if (cube.getTable() instanceof BusinessTable){
			BusinessTable businessColumnSet = (BusinessTable)cube.getTable();
			tableName = businessColumnSet.getPhysicalTable().getName();
		}
		
		return tableName;
	}

	/* (non-Javadoc)
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianCube#getCubeDimensions()
	 */
	@Override
	public List<IMondrianCubeDimesion> getCubeDimensions() {
		
		List<IMondrianCubeDimesion> cubeDimensions = new ArrayList<IMondrianCubeDimesion>();
		//Get all the dimension linked to the cube
		List<Dimension> dimensions = cube.getDimensions();
		//Get all the relationships of the table corresponding to the cube
		List<BusinessRelationship> businessRelationships = cube.getTable().getRelationships();
		
		for (Dimension dimension : dimensions){
			BusinessColumnSet dimensionTable = dimension.getTable();
			for (BusinessRelationship businessRelationship : businessRelationships){
				if (businessRelationship.getDestinationTable().equals(dimensionTable)){
					//Find relationship to table corresponding to the Dimension
					MondrianCubeDimension cubeDimension = new MondrianCubeDimension(businessRelationship,cube,dimension);
					cubeDimensions.add(cubeDimension);
				}
			}
		}
		
		
		
		return cubeDimensions;
	}

	/* (non-Javadoc)
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianCube#getMeasures()
	 */
	@Override
	public List<IMondrianMeasure> getMeasures() {
		List<IMondrianMeasure> mondrianMeasures = new ArrayList<IMondrianMeasure>();
		for (Measure measure: cube.getMeasures()){
			MondrianMeasure mondrianMeasure = new MondrianMeasure(measure);
			mondrianMeasures.add(mondrianMeasure);
		}
		
		return mondrianMeasures;
	}

}
