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
package it.eng.spagobi.engines.whatif.hierarchy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;

public class SbiHierarchy {
	private String name;
	private String uniqueName;
	private String caption;
	private int position;
	private List<Map<String, String>> slicers;
	private final List<String> levelNames;

	public SbiHierarchy(Hierarchy hierarchy, int position) {
		super();
		this.name = hierarchy.getName();
		this.uniqueName = hierarchy.getUniqueName();
		this.caption = hierarchy.getCaption();
		this.position = position;
		slicers = new ArrayList<Map<String, String>>();
		levelNames = new ArrayList<String>();
		List<Level> levels = hierarchy.getLevels();
		if (levels != null) {
			for (int i = 0; i < levels.size(); i++) {
				levelNames.add(levels.get(i).getName());
			}
		}
	}

	public List<Map<String, String>> getSlicers() {
		return slicers;
	}

	public void setSlicers(List<Map<String, String>> slicers) {
		this.slicers = slicers;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public List<String> getLevelNames() {
		return levelNames;
	}

	public String getCaption() {
		if (caption != null) {
			return caption;
		}
		return this.name;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

}
