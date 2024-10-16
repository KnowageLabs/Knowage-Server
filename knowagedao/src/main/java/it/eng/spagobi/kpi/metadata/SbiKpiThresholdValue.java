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
package it.eng.spagobi.kpi.metadata;

// Generated 19-feb-2016 16.57.14 by Hibernate Tools 3.6.0

import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

/**
 * SbiKpiThresholdValue generated by hbm2java
 */
public class SbiKpiThresholdValue extends SbiHibernateModel implements java.io.Serializable {

	private Integer id;
	private int position;
	private String label;
	private Double minValue;
	private Character includeMin;
	private Double maxValue;
	private Character includeMax;
	private String color;
	private SbiDomains severity;
	private SbiKpiThreshold sbiKpiThreshold;

	public SbiKpiThresholdValue() {
	}
	
	public SbiKpiThresholdValue(Integer id) {
		super();
		this.setId(id);
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	private void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the minValue
	 */
	public Double getMinValue() {
		return minValue;
	}

	/**
	 * @param minValue
	 *            the minValue to set
	 */
	public void setMinValue(Double minValue) {
		this.minValue = minValue;
	}

	/**
	 * @return the maxValue
	 */
	public Double getMaxValue() {
		return maxValue;
	}

	/**
	 * @param maxValue
	 *            the maxValue to set
	 */
	public void setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
	}

	/**
	 * @return the color
	 */
	public String getColor() {
		return color;
	}

	/**
	 * @param color
	 *            the color to set
	 */
	public void setColor(String color) {
		this.color = color;
	}

	/**
	 * @return the severity
	 */
	public SbiDomains getSeverity() {
		return severity;
	}

	/**
	 * @param severity
	 *            the severity to set
	 */
	public void setSeverity(SbiDomains severity) {
		this.severity = severity;
	}

	/**
	 * @return the sbiKpiThreshold
	 */
	public SbiKpiThreshold getSbiKpiThreshold() {
		return sbiKpiThreshold;
	}

	/**
	 * @param sbiKpiThreshold
	 *            the sbiKpiThreshold to set
	 */
	public void setSbiKpiThreshold(SbiKpiThreshold sbiKpiThreshold) {
		this.sbiKpiThreshold = sbiKpiThreshold;
	}

	/**
	 * @return the includeMin
	 */
	public Character getIncludeMin() {
		return includeMin;
	}

	/**
	 * @param includeMin
	 *            the includeMin to set
	 */
	public void setIncludeMin(Character includeMin) {
		this.includeMin = includeMin;
	}

	/**
	 * @return the includeMax
	 */
	public Character getIncludeMax() {
		return includeMax;
	}

	/**
	 * @param includeMax
	 *            the includeMax to set
	 */
	public void setIncludeMax(Character includeMax) {
		this.includeMax = includeMax;
	}

}
