/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it) 
 */
package it.eng.spagobi.engines.whatif.dimension;

import it.eng.spagobi.engines.whatif.hierarchy.SbiHierarchy;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olap4j.OlapException;
import org.olap4j.metadata.Dimension;

public class SbiDimension {

	static private Logger logger = Logger.getLogger(SbiDimension.class);

	private String name;
	private String uniqueName;
	private String caption;
	private List<SbiHierarchy> hierarchies;
	private String selectedHierarchyUniqueName;
	private int selectedHierarchyPosition;
	private int axis;
	private int measure;
	private final int positionInAxis;

	public SbiDimension(Dimension dimension, int axis, int positionInAxis) {
		super();
		this.name = dimension.getName();
		this.caption = dimension.getCaption();
		this.uniqueName = dimension.getUniqueName();
		this.axis = axis;
		this.hierarchies = new ArrayList<SbiHierarchy>();
		this.positionInAxis = positionInAxis;

		try {
			this.measure = dimension.getDimensionType().equals(Dimension.Type.MEASURE) ? 1 : 0;
		} catch (OlapException e) {
			logger.error("Error setting getting the type of the dimension", e);
		}
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

	public List<SbiHierarchy> getHierarchies() {
		return hierarchies;
	}

	public void setHierarchy(List<SbiHierarchy> hierarchies) {
		this.hierarchies = hierarchies;
	}

	public String getSelectedHierarchyUniqueName() {
		return selectedHierarchyUniqueName;
	}

	public void setSelectedHierarchyUniqueName(String selectedHierarchyUniqueName) {
		this.selectedHierarchyUniqueName = selectedHierarchyUniqueName;
	}

	public int getSelectedHierarchyPosition() {
		return selectedHierarchyPosition;
	}

	public void setSelectedHierarchyPosition(int selectedHierarchyPosition) {
		this.selectedHierarchyPosition = selectedHierarchyPosition;
	}

	public int getAxis() {
		return axis;
	}

	public void setAxis(int axis) {
		this.axis = axis;
	}

	public int getMeasure() {
		return measure;
	}

	public int getPositionInAxis() {
		return positionInAxis;
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
