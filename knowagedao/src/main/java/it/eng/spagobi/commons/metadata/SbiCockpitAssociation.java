/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.
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

/**
 *
 */
package it.eng.spagobi.commons.metadata;

/**
 * @author albnale
 *
 */
public class SbiCockpitAssociation extends SbiHibernateModel {
	private Integer sbiCockpitAssociationId;
	private Integer biobjId;

	private Integer dsIdFrom;
	private String columnNameFrom;
	private Integer dsIdTo;
	private String columnNameTo;

	public Integer getSbiCockpitAssociationId() {
		return sbiCockpitAssociationId;
	}

	public void setSbiCockpitAssociationId(Integer sbiCockpitAssociationId) {
		this.sbiCockpitAssociationId = sbiCockpitAssociationId;
	}

	public Integer getBiobjId() {
		return biobjId;
	}

	public void setBiobjId(Integer biobjId) {
		this.biobjId = biobjId;
	}

	public Integer getDsIdFrom() {
		return dsIdFrom;
	}

	public void setDsIdFrom(Integer dsIdFrom) {
		this.dsIdFrom = dsIdFrom;
	}

	public String getColumnNameFrom() {
		return columnNameFrom;
	}

	public void setColumnNameFrom(String columnNameFrom) {
		this.columnNameFrom = columnNameFrom;
	}

	public Integer getDsIdTo() {
		return dsIdTo;
	}

	public void setDsIdTo(Integer dsIdTo) {
		this.dsIdTo = dsIdTo;
	}

	public String getColumnNameTo() {
		return columnNameTo;
	}

	public void setColumnNameTo(String columnNameTo) {
		this.columnNameTo = columnNameTo;
	}

}
