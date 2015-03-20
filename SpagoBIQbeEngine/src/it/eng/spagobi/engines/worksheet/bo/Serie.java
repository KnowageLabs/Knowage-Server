/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.bo;


public class Serie extends Measure {
	String serieName;
	String color;
	Boolean showComma;
	Integer precision;
	String suffix;
	public Serie(String entityId, String alias, String iconCls, String nature, String function, String serieName, String color, Boolean showComma, Integer precision, String suffix) {
		super(entityId, alias, iconCls, nature, function);
		this.serieName = serieName;
		this.color = color;
		this.showComma = showComma;
		this.precision = precision;
		this.suffix = suffix;
	}
	public String getSerieName() {
		return serieName;
	}
	public void setSerieName(String serieName) {
		this.serieName = serieName;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public Boolean getShowComma() {
		return showComma;
	}
	public void setShowComma(Boolean showComma) {
		this.showComma = showComma;
	}
	public Integer getPrecision() {
		return precision;
	}
	public void setPrecision(Integer precision) {
		this.precision = precision;
	}
	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
}