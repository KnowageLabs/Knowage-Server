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

import it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianCubeDimesion;
import it.eng.knowage.meta.model.business.BusinessColumn;
import it.eng.knowage.meta.model.business.BusinessRelationship;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;
import it.eng.knowage.meta.model.olap.Cube;
import it.eng.knowage.meta.model.olap.Dimension;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class MondrianCubeDimension implements IMondrianCubeDimesion {
	BusinessRelationship businessRelationship;
	Cube cube;
	Dimension dimension;
	
	public MondrianCubeDimension(BusinessRelationship businessRelationship,Cube cube,Dimension dimension ){
		this.businessRelationship = businessRelationship;
		this.cube = cube;
		this.dimension = dimension;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianCubeDimesion#getName()
	 */
	@Override
	public String getName() {
		return dimension.getName();
	}
	/* (non-Javadoc)
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianCubeDimesion#getSource()
	 */
	@Override
	public String getSource() {
		return dimension.getName();
	}
	/* (non-Javadoc)
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianCubeDimesion#getForeignKey()
	 */
	@Override
	public String getForeignKey() {
		if (businessRelationship.getSourceColumns().get(0) instanceof SimpleBusinessColumn){
			SimpleBusinessColumn businessColumn = (SimpleBusinessColumn)businessRelationship.getSourceColumns().get(0);
			return businessColumn.getPhysicalColumn().getName();
		}
		return null;
	}
	
}
