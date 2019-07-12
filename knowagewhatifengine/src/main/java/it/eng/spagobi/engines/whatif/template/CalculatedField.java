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
package it.eng.spagobi.engines.whatif.template;

/**
 * @author Dragan Pirkovic
 *
 */
public class CalculatedField {

	private String name;
	private String parentMemberUniqueName;
	private Formula formula;

	/**
	 * @param name
	 * @param calculatedFieldFormula
	 * @param parentMemberUniqueName
	 */
	public CalculatedField(String name, Formula formula, String parentMemberUniqueName) {
		this.name = name;
		this.formula = formula;
		this.parentMemberUniqueName = parentMemberUniqueName;
	}

	/**
	 * @return the calculatedFieldName
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param calculatedFieldName
	 *            the calculatedFieldName to set
	 */
	public void setName(String calculatedFieldName) {
		this.name = calculatedFieldName;
	}

	/**
	 * @return the calculatedFieldFormula
	 */
	public String getCalculatedFieldFormula() {
		return this.formula.getExpression();
	}

	/**
	 * @return the parentMemberUniqueName
	 */
	public String getParentMemberUniqueName() {
		return parentMemberUniqueName;
	}

	/**
	 * @param parentMemberUniqueName
	 *            the parentMemberUniqueName to set
	 */
	public void setParentMemberUniqueName(String parentMemberUniqueName) {
		this.parentMemberUniqueName = parentMemberUniqueName;
	}

	/**
	 * @return the formula
	 */
	public Formula getFormula() {
		return formula;
	}

	/**
	 * @param formula
	 *            the formula to set
	 */
	public void setFormula(Formula formula) {
		this.formula = formula;
	}

}
