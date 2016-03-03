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