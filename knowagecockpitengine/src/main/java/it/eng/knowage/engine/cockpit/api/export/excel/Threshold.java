package it.eng.knowage.engine.cockpit.api.export.excel;

import java.awt.Color;

import org.apache.poi.xssf.usermodel.XSSFColor;

public class Threshold {

	private String operator;
	private Double value;
	private String color;
	private XSSFColor xSSFColor;

	public Threshold(String operator, Double value, String color) {
		super();
		this.operator = operator;
		this.value = value;
		this.color = color;
		this.xSSFColor = getXSSFColorFromString(color);
	}

	private XSSFColor getXSSFColorFromString(String color) {
		String[] colors = color.substring(4, color.length() - 1).split(",");
		int r = Integer.parseInt(colors[0].trim());
		int g = Integer.parseInt(colors[1].trim());
		int b = Integer.parseInt(colors[2].trim());
		return new XSSFColor(new Color(r, g, b));
	}

	public boolean isConstraintSatisfied(Double value) {
		if (operator == null)
			return false;

		if (operator.equals(">")) {
			return (value > this.value);
		} else if (operator.equals("<")) {
			return (value < this.value);
		} else if (operator.equals("==")) {
			return (value == this.value);
		} else if (operator.equals(">=")) {
			return (value >= this.value);
		} else if (operator.equals("<=")) {
			return (value <= this.value);
		} else if (operator.equals("!=")) {
			return (value != this.value);
		} else
			return false;
	}

	public XSSFColor getXSSFColor() {
		return xSSFColor;
	}

	@Override
	public String toString() {
		return "[" + operator + String.valueOf(value) + " : " + color + "]";
	}

}
