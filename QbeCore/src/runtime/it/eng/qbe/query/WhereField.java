/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.query;


/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class WhereField implements IQueryField{
	
	private String name;
	private String description;
	
	private boolean promptable;
	
	private Operand leftOperand;
	private String operator;	
	private Operand rightOperand;
	
	private String booleanConnector;

	
	public WhereField(String name, String description, boolean promptable,
			Operand leftOperand, String operator, Operand rightOperand,
			String booleanConnector) {
		
		setName(name);
		setDescription(description);
		setPromptable(promptable);
		setLeftOperand(leftOperand);
		setOperator(operator);
		setRightOperand(rightOperand);
		setBooleanConnector(booleanConnector);
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



	public static class Operand extends it.eng.qbe.query.Operand {

		public Operand(String[] values, String description, String type,
				String[] defaulttValues, String[] lastValues, String alias) {
			super(values, description, type, defaulttValues, lastValues, alias);
		}
		public Operand(String[] values, String description, String type,
				String[] defaulttValues, String[] lastValues) {
			super(values, description, type, defaulttValues, lastValues, "");
		}
	}
	
}
