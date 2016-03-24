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
package it.eng.knowage.meta.generator.mondrianschema.wrappers.impl;

import java.util.ArrayList;
import java.util.List;

import it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianCube;
import it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianCubeDimesion;
import it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianMeasure;
import it.eng.knowage.meta.model.business.BusinessColumnSet;
import it.eng.knowage.meta.model.business.BusinessRelationship;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.olap.Cube;
import it.eng.knowage.meta.model.olap.Dimension;
import it.eng.knowage.meta.model.olap.Measure;

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
