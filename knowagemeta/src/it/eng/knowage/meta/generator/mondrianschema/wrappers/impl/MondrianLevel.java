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

import it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianLevel;
import it.eng.spagobi.meta.model.business.SimpleBusinessColumn;
import it.eng.spagobi.meta.model.olap.Level;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 * 
 */
public class MondrianLevel implements IMondrianLevel {

	public static final String LEVEL_UNIQUE_MEMBERS = "structural.uniquemembers";
	public static final String LEVEL_TYPE = "structural.leveltype";

	Level level;

	public MondrianLevel(Level level) {
		this.level = level;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianLevel#getName()
	 */
	@Override
	public String getName() {
		return level.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianLevel#getColumn()
	 */
	@Override
	public String getColumn() {
		if (level.getColumn() instanceof SimpleBusinessColumn) {
			return ((SimpleBusinessColumn) level.getColumn()).getPhysicalColumn().getName();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianLevel#getNameColumn()
	 */
	@Override
	public String getNameColumn() {
		if (level.getNameColumn() != null) {
			if (level.getNameColumn() instanceof SimpleBusinessColumn) {
				return ((SimpleBusinessColumn) level.getNameColumn()).getPhysicalColumn().getName();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianLevel#getOrdinalColumn()
	 */
	@Override
	public String getOrdinalColumn() {
		if (level.getOrdinalColumn() != null) {
			if (level.getOrdinalColumn() instanceof SimpleBusinessColumn) {
				return ((SimpleBusinessColumn) level.getOrdinalColumn()).getPhysicalColumn().getName();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianLevel#getCaptionColumn()
	 */
	@Override
	public String getCaptionColumn() {
		if (level.getCaptionColumn() != null) {
			if (level.getCaptionColumn() instanceof SimpleBusinessColumn) {
				return ((SimpleBusinessColumn) level.getCaptionColumn()).getPhysicalColumn().getName();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianLevel#getUniqueMembers()
	 */
	@Override
	public String getUniqueMembers() {
		if (level.getProperties().get(LEVEL_UNIQUE_MEMBERS).getValue() != null) {
			return level.getProperties().get(LEVEL_UNIQUE_MEMBERS).getValue();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianLevel#getLevelType()
	 */
	@Override
	public String getLevelType() {
		if (level.getProperties().get(LEVEL_TYPE) != null) {
			if (level.getProperties().get(LEVEL_TYPE).getValue() != null) {
				return level.getProperties().get(LEVEL_TYPE).getValue();
			}
		}

		return "";
	}

}
