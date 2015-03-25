/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.kpi.utils;

import java.awt.Color;

/** 
 *  * @author Giulio Gavardi
 *     giulio.gavardi@eng.it
 */

public class KpiInterval {

	private String label;
	private Double min;
	private Double max;
	private Color color;
	
	
	/**
	 * Instantiates a new kpi interval.
	 */
	public KpiInterval() {
		super();
	}

	/**
	 * Instantiates a new kpi interval.
	 * 
	 * @param min the min
	 * @param max the max
	 * @param color the color
	 */
	public KpiInterval(Double min, Double max, Color color) {
		super();
		this.min = min;
		this.max = max;
		this.color = color;
	}
	
	/**
	 * Gets the min.
	 * 
	 * @return the min
	 */
	public Double getMin() {
		return min;
	}
	
	/**
	 * Sets the min.
	 * 
	 * @param min the new min
	 */
	public void setMin(Double min) {
		this.min = min;
	}
	
	/**
	 * Gets the max.
	 * 
	 * @return the max
	 */
	public Double getMax() {
		return max;
	}
	
	/**
	 * Sets the max.
	 * 
	 * @param max the new max
	 */
	public void setMax(Double max) {
		this.max = max;
	}
	
	/**
	 * Gets the color.
	 * 
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}
	
	/**
	 * Sets the color.
	 * 
	 * @param color the new color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Gets the label.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 * 
	 * @param label the new label
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	
}
