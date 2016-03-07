package it.eng.spagobi.tools.scheduler.utils;

public enum PredefinedCronExpression {

	EVERY_10_MINS("EVERY_10_MINS", "0 0/10 * 1/1 * ? *"), EVERY_15_MINS("EVERY_15_MINS", "0 0/15 * 1/1 * ? *"), EVERY_20_MINS("EVERY_20_MINS",
			"0 0/20 * 1/1 * ? *"), EVERY_30_MINS("EVERY_30_MINS", "0 0/30 * 1/1 * ? *"), HOURLY("HOURLY", "0 0 0/1 1/1 * ? *"), DAILY("DAILY",
			"0 0 0 1/1 * ? *"), WEEKLY("WEEKLY", "0 0 0 ? * SUN *"), MONTHLY("MONTHLY", "0 0 0 1 1/1 ? *"), YEARLY("YEARLY", "0 0 0 1 1 ? *");
	private String label;
	private String expression;

	private PredefinedCronExpression(String label, String expression) {
		this.label = label;
		this.expression = expression;
	}

	public String getLabel() {
		return label;
	}

	public String getExpression() {
		return expression;
	}

}
