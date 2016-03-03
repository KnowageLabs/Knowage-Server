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
package it.eng.qbe.query;

import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class WhereField implements IQueryField {

	private String name;
	private String description;

	private boolean promptable;

	private Operand leftOperand;
	private String operator;
	private Operand rightOperand;

	private String booleanConnector;

	private JSONObject temporalOperand;

	public WhereField(String name, String description, boolean promptable, Operand leftOperand, String operator, Operand rightOperand,
			String booleanConnector) {

		setName(name);
		setDescription(description);
		setPromptable(promptable);
		setLeftOperand(leftOperand);
		setOperator(operator);
		setRightOperand(rightOperand);
		setBooleanConnector(booleanConnector);
	}

	public WhereField(String name, String description, boolean promptable, Operand leftOperand, String operator, Operand rightOperand, String booleanConnector,
			JSONObject temporalOperand) {

		this(name, description, promptable, leftOperand, operator, rightOperand, booleanConnector);
		setTemporalOperand(temporalOperand);
	}

	public String getAlias() {
		return name;
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

	public boolean isPromptable() {
		return promptable;
	}

	public void setPromptable(boolean promptable) {
		this.promptable = promptable;
	}

	public Operand getLeftOperand() {
		return leftOperand;
	}

	public void setLeftOperand(Operand leftOperand) {
		this.leftOperand = leftOperand;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public Operand getRightOperand() {
		return rightOperand;
	}

	public void setRightOperand(Operand rightOperand) {
		this.rightOperand = rightOperand;
	}

	public String getBooleanConnector() {
		return booleanConnector;
	}

	public void setBooleanConnector(String booleanConnector) {
		this.booleanConnector = booleanConnector;
	}

	public void setTemporalOperand(JSONObject temporalOperand) {
		this.temporalOperand = temporalOperand;
	}

	public JSONObject getTemporalOperand() {
		return temporalOperand;
	}

	public static class Operand extends it.eng.qbe.query.Operand {

		public Operand(String[] values, String description, String type, String[] defaulttValues, String[] lastValues, String alias) {
			super(values, description, type, defaulttValues, lastValues, alias);
		}

		public Operand(String[] values, String description, String type, String[] defaulttValues, String[] lastValues) {
			super(values, description, type, defaulttValues, lastValues, "");
		}
	}

}
