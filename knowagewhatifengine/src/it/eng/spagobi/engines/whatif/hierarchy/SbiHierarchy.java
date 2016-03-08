/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it) 
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
