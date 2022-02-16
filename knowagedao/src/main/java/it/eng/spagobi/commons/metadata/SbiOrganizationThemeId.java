/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.commons.metadata;

import java.util.Objects;

import javax.persistence.Column;

public class SbiOrganizationThemeId implements java.io.Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8833471178587308541L;

	@Column(name = "UUID")
	protected String uuid;

	@Column(name = "ORGANIZATION_ID")
	protected int organizationId;

	/**
	 *
	 */
	public SbiOrganizationThemeId() {
		// TODO Auto-generated constructor stub
	}

	@Column(columnDefinition = "UUID", updatable = false)
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Column(columnDefinition = "ORGANIZATION_ID", updatable = false)
	public int getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(int organizationId) {
		this.organizationId = organizationId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(organizationId, uuid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SbiOrganizationThemeId other = (SbiOrganizationThemeId) obj;
		return organizationId == other.organizationId && Objects.equals(uuid, other.uuid);
	}

}
