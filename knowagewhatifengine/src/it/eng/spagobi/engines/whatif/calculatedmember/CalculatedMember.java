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
package it.eng.spagobi.engines.whatif.calculatedmember;

import org.olap4j.Axis;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

public class CalculatedMember {

	private String calculateFieldName;
	private String calculateFieldFormula;
	private Member parentMember;
	private Axis parentMemberAxis;

	public CalculatedMember(String calculateFieldName, String calculateFieldFormula, Member parentMember, Axis parentMemberAxis) {
		super();
		this.calculateFieldName = calculateFieldName;
		this.calculateFieldFormula = calculateFieldFormula;
		this.parentMember = parentMember;
		this.parentMemberAxis = parentMemberAxis;
	}

	public String getCalculateFieldName() {
		return calculateFieldName;
	}

	public void setCalculateFieldName(String calculateFieldName) {
		this.calculateFieldName = calculateFieldName;
	}

	public String getCalculateFieldFormula() {
		return calculateFieldFormula;
	}

	public void setCalculateFieldFormula(String calculateFieldFormula) {
		this.calculateFieldFormula = calculateFieldFormula;
	}

	public Member getParentMember() {
		return parentMember;
	}

	public void setParentMember(Member parentMember) {
		this.parentMember = parentMember;
	}

	public Axis getParentMemberAxis() {
		return parentMemberAxis;
	}

	public void setParentMemberAxis(Axis parentMemberAxis) {
		this.parentMemberAxis = parentMemberAxis;
	}
	
	public Hierarchy getHierarchy(){
		if(getParentMember()!=null){
			return this.getParentMember().getHierarchy();
		}
		return null;
	}

}
