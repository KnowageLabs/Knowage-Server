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
package it.eng.knowage.meta.generator.mondrianschema.wrappers;

import java.util.ArrayList;
import java.util.List;


import it.eng.knowage.meta.generator.mondrianschema.wrappers.impl.MondrianCube;
import it.eng.knowage.meta.generator.mondrianschema.wrappers.impl.MondrianDimension;

import it.eng.spagobi.meta.model.olap.Cube;
import it.eng.spagobi.meta.model.olap.Dimension;
import it.eng.spagobi.meta.model.olap.OlapModel;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class MondrianModel {
	private OlapModel olapModel;
	
	private List<IMondrianCube> cubes;
	private List<IMondrianDimension> dimensions;
	
	public MondrianModel(OlapModel olapModel){
		this.olapModel = olapModel;
		this.cubes = wrapCubes(olapModel.getCubes());
		this.dimensions = wrapDimensions(olapModel.getDimensions());
	}

	/**
	 * @return the cubes
	 */
	public List<IMondrianCube> getCubes() {
		return cubes;
	}

	/**
	 * @param cubes the cubes to set
	 */
	public void setCubes(List<IMondrianCube> cubes) {
		this.cubes = cubes;
	}

	/**
	 * @return the dimensions
	 */
	public List<IMondrianDimension> getDimensions() {
		return dimensions;
	}

	/**
	 * @param dimensions the dimensions to set
	 */
	public void setDimensions(List<IMondrianDimension> dimensions) {
		this.dimensions = dimensions;
	}
	
	private List<IMondrianCube> wrapCubes(List<Cube> cubes){
		 List<IMondrianCube> mondrianCubes;
		 
			
		 mondrianCubes = new ArrayList<IMondrianCube>();
		 for(Cube cube : cubes) {
			 mondrianCubes.add( new MondrianCube(cube) );
		 }

		 return mondrianCubes;


	}
	
	private List<IMondrianDimension> wrapDimensions(List<Dimension> dimensions){
		 List<IMondrianDimension> mondrianDimensions;
		 
			
		 mondrianDimensions = new ArrayList<IMondrianDimension>();
		 for(Dimension dimension : dimensions) {
			 mondrianDimensions.add( new MondrianDimension(dimension) );
		 }

		 return mondrianDimensions;		
	}

}
