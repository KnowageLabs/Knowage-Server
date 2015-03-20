/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.kpi.bo;

public class KpiLineVisibilityOptions {
	
	private Boolean closed_tree = true;
	private Boolean display_bullet_chart = true;
	private Boolean display_threshold_image = true;
	private Boolean display_alarm = true;
	private Boolean display_semaphore = true;
	private Boolean display_weight = true;
	private Boolean show_axis = true;
	private Boolean weighted_values = false;
	private String model_title = "MODEL";
	private String threshold_image_title = null;
	private String bullet_chart_title = null;
	private String kpi_title = null;
	private String weight_title = null;
	private String value_title = null;
	
	
	public KpiLineVisibilityOptions(Boolean closed_tree,
			Boolean display_bullet_chart, Boolean display_threshold_image,
			Boolean display_alarm, Boolean display_semaphore,
			Boolean display_weight, Boolean show_axis, Boolean weighted_values) {
		super();
		this.closed_tree = closed_tree;
		this.display_bullet_chart = display_bullet_chart;
		this.display_threshold_image = display_threshold_image;
		this.display_alarm = display_alarm;
		this.display_semaphore = display_semaphore;
		this.display_weight = display_weight;
		this.show_axis = show_axis;
		this.weighted_values = weighted_values;
	}
	
	public KpiLineVisibilityOptions() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Boolean getClosed_tree() {
		return closed_tree;
	}
	public void setClosed_tree(Boolean closed_tree) {
		this.closed_tree = closed_tree;
	}
	public Boolean getDisplay_bullet_chart() {
		return display_bullet_chart;
	}
	public void setDisplay_bullet_chart(Boolean display_bullet_chart) {
		this.display_bullet_chart = display_bullet_chart;
	}
	public Boolean getDisplay_threshold_image() {
		return display_threshold_image;
	}
	public void setDisplay_threshold_image(Boolean display_threshold_image) {
		this.display_threshold_image = display_threshold_image;
	}
	public Boolean getDisplay_alarm() {
		return display_alarm;
	}
	public void setDisplay_alarm(Boolean display_alarm) {
		this.display_alarm = display_alarm;
	}
	public Boolean getDisplay_semaphore() {
		return display_semaphore;
	}
	public void setDisplay_semaphore(Boolean display_semaphore) {
		this.display_semaphore = display_semaphore;
	}
	public Boolean getDisplay_weight() {
		return display_weight;
	}
	public void setDisplay_weight(Boolean display_weight) {
		this.display_weight = display_weight;
	}
	public Boolean getShow_axis() {
		return show_axis;
	}
	public void setShow_axis(Boolean show_axis) {
		this.show_axis = show_axis;
	}
	public Boolean getWeighted_values() {
		return weighted_values;
	}
	public void setWeighted_values(Boolean weighted_values) {
		this.weighted_values = weighted_values;
	}

	public String getModel_title() {
		return model_title;
	}

	public void setModel_title(String model_title) {
		this.model_title = model_title;
	}

	public String getThreshold_image_title() {
		return threshold_image_title;
	}

	public void setThreshold_image_title(String threshold_image_title) {
		this.threshold_image_title = threshold_image_title;
	}

	public String getBullet_chart_title() {
		return bullet_chart_title;
	}

	public void setBullet_chart_title(String bullet_chart_title) {
		this.bullet_chart_title = bullet_chart_title;
	}

	public String getKpi_title() {
		return kpi_title;
	}

	public void setKpi_title(String kpi_title) {
		this.kpi_title = kpi_title;
	}

	public String getWeight_title() {
		return weight_title;
	}

	public void setWeight_title(String weight_title) {
		this.weight_title = weight_title;
	}

	public String getValue_title() {
		return value_title;
	}

	public void setValue_title(String value_title) {
		this.value_title = value_title;
	}



}
