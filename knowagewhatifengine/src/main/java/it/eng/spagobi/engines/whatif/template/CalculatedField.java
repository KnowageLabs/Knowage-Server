/**
 *
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
