/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
