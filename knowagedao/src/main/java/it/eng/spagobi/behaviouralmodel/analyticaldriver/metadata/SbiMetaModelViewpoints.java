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

package it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata;

import java.util.Date;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.tools.catalogue.metadata.SbiMetaModel;

public class SbiMetaModelViewpoints extends SbiHibernateModel {

	// Fields

	private Integer vpId;
	private SbiMetaModel sbiMetaModel;
	private String vpOwner;
	private String vpName;
	private String vpDesc;
	private String vpScope;
	private String vpValueParams;
	private Date vpCreationDate;

	// Constructors

	/**
	 * default constructor.
	 */
	public SbiMetaModelViewpoints() {
	}

	/**
	 * minimal constructor.
	 *
	 * @param vpId           the vp id
	 * @param vpOwner        the vp owner
	 * @param vpName         the vp name
	 * @param vpScope        the vp scope
	 * @param vpCreationDate the vp creation date
	 */
	public SbiMetaModelViewpoints(Integer vpId, String vpOwner, String vpName, String vpScope, Date vpCreationDate) {
		this.vpId = vpId;
		this.vpOwner = vpOwner;
		this.vpName = vpName;
		this.vpScope = vpScope;
		this.vpCreationDate = vpCreationDate;
	}

	/**
	 * full constructor.
	 *
	 * @param vpId           the vp id
	 * @param vpOwner        the vp owner
	 * @param vpName         the vp name
	 * @param vpDesc         the vp desc
	 * @param vpScope        the vp scope
	 * @param vpValueParams  the vp value params
	 * @param vpCreationDate the vp creation date
	 */
	public SbiMetaModelViewpoints(Integer vpId, String vpOwner, String vpName, String vpDesc, String vpScope,
			String vpValueParams, Date vpCreationDate) {
		this.vpId = vpId;
		this.vpOwner = vpOwner;
		this.vpName = vpName;
		this.vpDesc = vpDesc;
		this.vpScope = vpScope;
		this.vpValueParams = vpValueParams;
		this.vpCreationDate = vpCreationDate;
	}

	// Property accessors

	/**
	 * Gets the vp id.
	 *
	 * @return the vp id
	 */
	public Integer getVpId() {
		return this.vpId;
	}

	/**
	 * Sets the vp id.
	 *
	 * @param vpId the new vp id
	 */
	public void setVpId(Integer vpId) {
		this.vpId = vpId;
	}

	/*
	 * public Integer getBiobjId() { return this.biobjId; }
	 *
	 * public void setBiobjId(Integer biobjId) { this.biobjId = biobjId; }
	 */
	/**
	 * Gets the vp name.
	 *
	 * @return the vp name
	 */
	public String getVpName() {
		return this.vpName;
	}

	/**
	 * Sets the vp name.
	 *
	 * @param vpName the new vp name
	 */
	public void setVpName(String vpName) {
		this.vpName = vpName;
	}

	/**
	 * Gets the vp desc.
	 *
	 * @return the vp desc
	 */
	public String getVpDesc() {
		return this.vpDesc;
	}

	/**
	 * Sets the vp desc.
	 *
	 * @param vpDesc the new vp desc
	 */
	public void setVpDesc(String vpDesc) {
		this.vpDesc = vpDesc;
	}

	/**
	 * Gets the vp scope.
	 *
	 * @return the vp scope
	 */
	public String getVpScope() {
		return this.vpScope;
	}

	/**
	 * Sets the vp scope.
	 *
	 * @param vpScope the new vp scope
	 */
	public void setVpScope(String vpScope) {
		this.vpScope = vpScope;
	}

	/**
	 * Gets the vp value params.
	 *
	 * @return the vp value params
	 */
	public String getVpValueParams() {
		return this.vpValueParams;
	}

	/**
	 * Sets the vp value params.
	 *
	 * @param vpValueParams the new vp value params
	 */
	public void setVpValueParams(String vpValueParams) {
		this.vpValueParams = vpValueParams;
	}

	/**
	 * Gets the vp creation date.
	 *
	 * @return the vp creation date
	 */
	public Date getVpCreationDate() {
		return this.vpCreationDate;
	}

	/**
	 * Sets the vp creation date.
	 *
	 * @param date the new vp creation date
	 */
	public void setVpCreationDate(Date date) {
		this.vpCreationDate = date;
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

	public SbiMetaModel getSbiMetaModel() {
		return sbiMetaModel;
	}

	public void setSbiMetaModel(SbiMetaModel sbiMetaModel) {
		this.sbiMetaModel = sbiMetaModel;
	}
}
