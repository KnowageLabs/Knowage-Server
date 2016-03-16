package it.eng.spagobi.engines.whatif.calculatedmember;

public class Argument {

	private String expected_value;
	private String argument_description;

	public String getExpected_value() {
		return expected_value;
	}

	public void setExpected_value(String expected_value) {
		this.expected_value = expected_value;
	}

	public String getArgument_description() {
		return argument_description.trim();
	}

	public void setArgument_description(String argument_description) {
		this.argument_description = argument_description;
	}

}
