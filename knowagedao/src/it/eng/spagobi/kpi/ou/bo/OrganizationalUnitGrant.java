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
package it.eng.spagobi.kpi.ou.bo;

import java.util.Date;

/**
 * This class represents the grant to an Organizational Unit hierarchy for a KPI model instance
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 * 
 */
public class OrganizationalUnitGrant {

	// Fields

	private Integer id;
	private OrganizationalUnitHierarchy hierarchy;
	private Date startDate;
	private Date endDate;
	private String label;
	private String name;
	private String description;
	private Boolean isAvailable;

	// Constructors

	/** default constructor */
	public OrganizationalUnitGrant() {
	}

	/** full constructor */
	public OrganizationalUnitGrant(Integer id, Boolean isAvailable, OrganizationalUnitHierarchy hierarchy, Date startDate, Date endDate, String label,
			String name, String description) {
		this.id = id;
		this.hierarchy = hierarchy;
		this.startDate = startDate;
		this.endDate = endDate;
		this.label = label;
		this.name = name;
		this.description = description;
		this.isAvailable = isAvailable;
	}

	public Boolean getIsAvailable() {
		return isAvailable;
	}

	public void setIsAvailable(Boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public OrganizationalUnitHierarchy getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(OrganizationalUnitHierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "OrganizationalUnitGrant [id=" + id + ", hierarchy=" + hierarchy + ", startDate=" + startDate + ", endDate=" + endDate + ", label=" + label
				+ ", name=" + name + ", description=" + description + "]";
	}

}
