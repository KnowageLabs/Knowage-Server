/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.whatif.calculatedmember;

import org.olap4j.Axis;
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

}
