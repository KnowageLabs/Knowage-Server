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
package it.eng.spagobi.kpi.goal.metadata.bo;

import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrant;

import java.util.Date;

public class Goal {

	private Integer id;
	private Date startDate;
	private Date endDate;
	private String name;
	private String label;
	private String description;
	private Integer grantId;
	
	
	
	public Goal(Integer id, Date startSate, Date endDate, String name, String label,
			String description, Integer grantId) {
		super();
		this.id = id;
		this.startDate = startSate;
		this.endDate = endDate;
		this.name = name;
		this.description = description;
		this.grantId = grantId;
		this.label = label;

	}
	public Goal(Date startSate, Date endDate, String name, String description,String label,
			Integer grant) {
		super();
		this.startDate = startSate;
		this.endDate = endDate;
		this.name = name;
		this.description = description;
		this.grantId = grant;
		this.label = label;

	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startSate) {
		this.startDate = startSate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
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
	public Integer getGrant() {
		return grantId;
	}
	public void setGrant(Integer grantId) {
		this.grantId = grantId;
	}

	public Integer getGrantId() {
		return grantId;
	}
	public void setGrantId(Integer grantId) {
		this.grantId = grantId;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	
	
}
