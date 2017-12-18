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
package it.eng.qbe.model.structure;

import java.util.LinkedList;

public class Hierarchy {
	private String name;
	private Boolean isDefault;
	private LinkedList<HierarchyLevel> levels;

	public Hierarchy(String name, Boolean isDefault) {
		setName(name);
		setIsDefault(isDefault);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the levels
	 */
	public LinkedList<HierarchyLevel> getLevels() {
		return levels;
	}

	/**
	 * @param levels
	 *            the levels to set
	 */
	public void setLevels(LinkedList<HierarchyLevel> levels) {
		this.levels = levels;
	}

	/**
	 * @return the isDefault
	 */
	public Boolean getIsDefault() {
		return isDefault;
	}

	/**
	 * @param isDefault
	 *            the isDefault to set
	 */
	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public String[] getAncestors(String levelColumn) {
		String[] ancestors = new String[0];
		for (int i = 1; i < levels.size(); i++) {
			if (levels.get(i).getColumn().equals(levelColumn)) {
				ancestors = new String[i];
				i--;
				for (; i >= 0; i--) {
					ancestors[i] = levels.get(i).getColumn();
				}
				break;
			}
		}
		return ancestors;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Hierarchy) {
			Hierarchy h = (Hierarchy) obj;
			if (h.getName().equals(getName())) {
				return true;
			}
		}
		return false;
	}

	public String getLevelByType(String levelType) {
		for (HierarchyLevel level : getLevels()) {
			if (level.getType().equals(levelType)) {
				return level.getColumn();
			}
		}
		return null;
	}
	
	public boolean contains(String name) {
		for (HierarchyLevel level : getLevels()) {
			if (level.getColumn().equals(name)) {
				return true;
			}
		}
		return false;
	}
}
