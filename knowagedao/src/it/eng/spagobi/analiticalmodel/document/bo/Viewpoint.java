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
package it.eng.spagobi.analiticalmodel.document.bo;

import java.io.Serializable;
import java.util.Date;

/**
 * Defines a value constraint object.
 *
 * @author giachino
 *
 */


public class Viewpoint  implements Serializable   {

	private Integer vpId;
    private Integer biobjId;
    private String vpOwner;
    private String vpName;
    private String vpDesc;
    private String vpScope;
    private String vpValueParams;
    private Date   vpCreationDate;

	/**
	 * Gets the biobj id
	 *
	 * @return the biobjId
	 */
	public Integer getBiobjId() {
		return biobjId;
	}

	/**
	 * Sets the biobj id.
	 *
	 * @param biobjId the biobjId to set
	 */
	public void setBiobjId(Integer biobjId) {
		this.biobjId = biobjId;
	}

	/**
	 * Gets the vp desc.
	 *
	 * @return the vpDesc
	 */
	public String getVpDesc() {
		return vpDesc;
	}

	/**
	 * Sets the vp desc.
	 *
	 * @param vpDesc the vpDesc to set
	 */
	public void setVpDesc(String vpDesc) {
		this.vpDesc = vpDesc;
	}

	/**
	 * Gets the vp id.
	 *
	 * @return the vpId
	 */
	public Integer getVpId() {
		return vpId;
	}

	/**
	 * Sets the vp id.
	 *
	 * @param vpId the vpId to set
	 */
	public void setVpId(Integer vpId) {
		this.vpId = vpId;
	}

	/**
	 * Gets the vp name.
	 *
	 * @return the vpName
	 */
	public String getVpName() {
		return vpName;
	}

	/**
	 * Sets the vp name.
	 *
	 * @param vpName the vpName to set
	 */
	public void setVpName(String vpName) {
		this.vpName = vpName;
	}

	/**
	 * Gets the vp scope.
	 *
	 * @return the vpScope
	 */
	public String getVpScope() {
		return vpScope;
	}

	/**
	 * Sets the vp scope.
	 *
	 * @param vpScope the vpScope to set
	 */
	public void setVpScope(String vpScope) {
		this.vpScope = vpScope;
	}

	/**
	 * Gets the vp value params.
	 *
	 * @return the vpValueParams
	 */
	public String getVpValueParams() {
		return vpValueParams;
	}

	/**
	 * Sets the vp value params.
	 *
	 * @param vpValueParams the vpValueParams to set
	 */
	public void setVpValueParams(String vpValueParams) {
		this.vpValueParams = vpValueParams;
	}

	/**
	 * Gets the vp creation date.
	 *
	 * @return the vpCreationDate
	 */
	public Date getVpCreationDate() {
		return vpCreationDate;
	}

	/**
	 * Sets the vp creation date.
	 *
	 * @param vpCreationDate the vpCreationDate to set
	 */
	public void setVpCreationDate(Date vpCreationDate) {
		this.vpCreationDate = vpCreationDate;
	}

	/**
	 * Gets the vp owner.
	 *
	 * @return the vpOwner
	 */
	public String getVpOwner() {
		return vpOwner;
	}

	/**
	 * Sets the vp owner.
	 *
	 * @param vpOwner the vpOwner to set
	 */
	public void setVpOwner(String vpOwner) {
		this.vpOwner = vpOwner;
	}




}
