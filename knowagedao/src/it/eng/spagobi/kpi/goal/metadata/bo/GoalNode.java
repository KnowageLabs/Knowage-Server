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

import it.eng.spagobi.kpi.ou.bo.OrganizationalUnit;

public class GoalNode {
	private String name;
	private String label;
	private String goalDescr;
	private Integer goalId;
	private Integer ouId;
	private Integer id;
	private Integer fatherCountId;

	
	
	public GoalNode(String name, String label, String goalDescr, Integer goal,
			Integer ou) {
		super();
		this.name = name;
		this.label = label;
		this.goalDescr = goalDescr;
		this.goalId = goal;
		this.ouId = ou;
	}
	
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getGoalDescr() {
		return goalDescr;
	}
	public void setGoalDescr(String goalDescr) {
		this.goalDescr = goalDescr;
	}
	public Integer getGoalId() {
		return goalId;
	}
	public void setGoalId(Integer goalId) {
		this.goalId = goalId;
	}
	public Integer getOuId() {
		return ouId;
	}
	public void setOuId(Integer ouId) {
		this.ouId = ouId;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}



	public Integer getFatherCountId() {
		return fatherCountId;
	}



	public void setFatherCountId(Integer fatherCountId) {
		this.fatherCountId = fatherCountId;
	}
	
	
}
