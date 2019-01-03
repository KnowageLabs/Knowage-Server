/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.tools.tag;

import java.io.Serializable;

public class SbiDatasetTagId implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer dsId;
	private Integer versionNum;
	private String organization;
	private Integer tagId;

	public SbiDatasetTagId() {

	}

	public SbiDatasetTagId(Integer dsId, Integer versionNum, String organization, Integer tagId) {
		this.dsId = dsId;
		this.versionNum = versionNum;
		this.organization = organization;
		this.tagId = tagId;
	}

	public Integer getDsId() {
		return dsId;
	}

	public void setDsId(Integer dsId) {
		this.dsId = dsId;
	}

	public Integer getVersionNum() {
		return versionNum;
	}

	public void setVersionNum(Integer versionNum) {
		this.versionNum = versionNum;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public Integer getTagId() {
		return tagId;
	}

	public void setTagId(Integer tagId) {
		this.tagId = tagId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 17;
		result = prime * result + ((getTagId() == null) ? 0 : this.getTagId().hashCode());
		result = prime * result + ((getDsId() == null) ? 0 : this.getDsId().hashCode());
		result = prime * result + ((getVersionNum() == null) ? 0 : this.getVersionNum().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		if (!(obj instanceof SbiDatasetTagId))
			return false;
		SbiDatasetTagId other = (SbiDatasetTagId) obj;
		if (!(other.getDsId().equals(getDsId())) && !(other.getVersionNum().equals(getVersionNum())) && !(other.getOrganization().equals(getOrganization()))
				&& !(other.getTagId().equals(getTagId())))
			return false;

		return true;
	}

}
