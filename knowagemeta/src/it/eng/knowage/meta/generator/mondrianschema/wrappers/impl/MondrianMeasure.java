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

import it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianMeasure;
import it.eng.spagobi.meta.model.business.SimpleBusinessColumn;
import it.eng.spagobi.meta.model.olap.Measure;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class MondrianMeasure implements IMondrianMeasure {

	Measure measure;
	public static final String STRUCTURAL_AGGREGATION_TYPE = "structural.aggtype";
	public static final String STRUCTURAL_FORMAT_STRING = "structural.format";

	
	public MondrianMeasure(Measure measure){
		this.measure = measure;
	}
	
	
	/* (non-Javadoc)
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianMeasure#getName()
	 */
	@Override
	public String getName() {
		return measure.getName();
	}

	/* (non-Javadoc)
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianMeasure#getColumn()
	 */
	@Override
	public String getColumn() {
		if (measure.getColumn() instanceof SimpleBusinessColumn){
			return ((SimpleBusinessColumn)measure.getColumn()).getPhysicalColumn().getName();
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianMeasure#getAggregator()
	 */
	@Override
	public String getAggregator() {
		if (measure.getColumn().getProperties().get(STRUCTURAL_AGGREGATION_TYPE).getValue() != null){
			return measure.getColumn().getProperties().get(STRUCTURAL_AGGREGATION_TYPE).getValue().toLowerCase();
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianMeasure#getFormatString()
	 */
	@Override
	public String getFormatString() {
		if (measure.getColumn().getProperties().get(STRUCTURAL_FORMAT_STRING).getValue() != null){
			return measure.getColumn().getProperties().get(STRUCTURAL_FORMAT_STRING).getValue();
		}
		return null;
	}

}
