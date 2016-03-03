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
package it.eng.spagobi.kpi.config.bo;

import java.io.Serializable;

public class MeasureUnit implements Serializable{

	Integer id=null;
	String name=null;
	String scaleCd=null;
	String scaleNm=null;
	Integer scaleTypeId=null;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getScaleCd() {
		return scaleCd;
	}
	public void setScaleCd(String scaleCd) {
		this.scaleCd = scaleCd;
	}
	public String getScaleNm() {
		return scaleNm;
	}
	public void setScaleNm(String scaleNm) {
		this.scaleNm = scaleNm;
	}
	public Integer getScaleTypeId() {
		return scaleTypeId;
	}
	public void setScaleTypeId(Integer scaleTypeId) {
		this.scaleTypeId = scaleTypeId;
	}

}
