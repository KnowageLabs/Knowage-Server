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

import it.eng.knowage.meta.generator.jpamapping.wrappers.JpaProperties;
import it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianDimension;
import it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianHierarchy;
import it.eng.knowage.meta.generator.utils.JavaKeywordsUtils;
import it.eng.spagobi.meta.model.ModelProperty;
import it.eng.spagobi.meta.model.business.BusinessColumnSet;
import it.eng.spagobi.meta.model.business.BusinessModel;
import it.eng.spagobi.meta.model.olap.Dimension;
import it.eng.spagobi.meta.model.olap.Hierarchy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 * 
 */
public class MondrianDimension implements IMondrianDimension {

	Dimension dimension;

	/**
	 * @param dimension
	 */
	public MondrianDimension(Dimension dimension) {
		this.dimension = dimension;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianDimension#getName()
	 */
	@Override
	public String getName() {
		return dimension.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianDimension#getHierarchies()
	 */
	@Override
	public List<IMondrianHierarchy> getHierarchies() {
		List<IMondrianHierarchy> hierarchies = new ArrayList<IMondrianHierarchy>();
		for (Hierarchy hierarchy : dimension.getHierarchies()) {
			MondrianHierarchy mondrianHierarchy = new MondrianHierarchy(hierarchy);
			hierarchies.add(mondrianHierarchy);
		}
		return hierarchies;
	}

	/**
	 * Check if the dimension is not of a specific type (temporal, time, etc...)
	 * 
	 * @return true if the dimension is not of a specific type
	 */
	@Override
	public boolean isSimpleDimension() {
		BusinessColumnSet businessColumnSet = dimension.getTable();
		if (businessColumnSet.getProperties().get("structural.tabletype") != null) {
			String tableType = businessColumnSet.getProperties().get("structural.tabletype").getValue();
			if (tableType.equals("dimension")) {
				return true;
			}
		}
		return false;

	}

	/**
	 * @returns the generated java class name (not qualified).
	 */

	public String getClassName() {
		String name;
		name = JavaKeywordsUtils.transformToJavaClassName(dimension.getTable().getUniqueName());
		return name;
	}

	@Override
	public String getQualifiedClassName() {
		return getPackage() + "." + getClassName();
	}

	public String getPackage() {
		String packageName;
		ModelProperty packageProperty;

		BusinessModel model = dimension.getTable().getModel();

		packageProperty = model.getProperties().get(JpaProperties.MODEL_PACKAGE);

		// check if property is setted, else get default value
		if (packageProperty.getValue() != null) {
			packageName = packageProperty.getValue();
		} else {
			packageName = packageProperty.getPropertyType().getDefaultValue();
		}

		return packageName;
	}

}
