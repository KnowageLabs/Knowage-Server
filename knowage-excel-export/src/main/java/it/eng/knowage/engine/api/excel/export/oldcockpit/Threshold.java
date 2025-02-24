package it.eng.knowage.engine.api.excel.export.oldcockpit;

import org.apache.poi.xssf.usermodel.XSSFColor;

import java.util.Objects;

public class Threshold {

	private final String operator;
	private final Double value;
	private final String color;
	private final XSSFColor xSSFColor;

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
		
		return new XSSFColor(new byte[]{(byte) r, (byte) g, (byte) b});
	}

	public boolean isConstraintSatisfied(Double value) {
		if (operator == null)
			return false;

        return switch (operator) {
            case ">" -> (value > this.value);
            case "<" -> (value < this.value);
            case "==" -> (Objects.equals(value, this.value));
            case ">=" -> (value >= this.value);
            case "<=" -> (value <= this.value);
            case "!=" -> (!Objects.equals(value, this.value));
            default -> false;
        };
	}

	public XSSFColor getXSSFColor() {
		return xSSFColor;
	}

	@Override
	public String toString() {
		return "[" + operator + String.valueOf(value) + " : " + color + "]";
	}

}
