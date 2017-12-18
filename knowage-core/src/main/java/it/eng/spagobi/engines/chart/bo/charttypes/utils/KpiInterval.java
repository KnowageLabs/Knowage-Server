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

package it.eng.spagobi.engines.chart.bo.charttypes.utils;

import java.awt.Color;

/** 
 *  * @author Giulio Gavardi
 *     giulio.gavardi@eng.it
 */

public class KpiInterval {

	private String label;
	private double min;
	private double max;
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
	public KpiInterval(double min, double max, Color color) {
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
	public double getMin() {
		return min;
	}
	
	/**
	 * Sets the min.
	 * 
	 * @param min the new min
	 */
	public void setMin(double min) {
		this.min = min;
	}
	
	/**
	 * Gets the max.
	 * 
	 * @return the max
	 */
	public double getMax() {
		return max;
	}
	
	/**
	 * Sets the max.
	 * 
	 * @param max the new max
	 */
	public void setMax(double max) {
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
