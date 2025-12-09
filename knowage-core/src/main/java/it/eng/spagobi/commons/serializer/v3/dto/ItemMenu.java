package it.eng.spagobi.commons.serializer.v3.dto;

public final class ItemMenu {
	private final String id;
	private final String label;
	private final String requiredFunctionality;
	private final String to;
	private final String command;
	private final String iconCls;
	private final String condition;
	private final String conditionedView;
	private final String toBeAuthorized;
	private final String toBeLicensed;

	public ItemMenu(String id, String label, String requiredFunctionality, String to, String command, String iconCls, String condition, String conditionedView,
			String toBeAuthorized, String toBeLicensed) {
		this.id = id;
		this.label = label;
		this.requiredFunctionality = requiredFunctionality;
		this.to = to;
		this.command = command;
		this.iconCls = iconCls;
		this.condition = condition;
		this.conditionedView = conditionedView;
		this.toBeAuthorized = toBeAuthorized;
		this.toBeLicensed = toBeLicensed;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public String getRequiredFunctionality() {
		return requiredFunctionality;
	}

	public String getTo() {
		return to;
	}

	public String getCommand() {
		return command;
	}

	public String getIconCls() {
		return iconCls;
	}

	public String getCondition() {
		return condition;
	}

	public String getConditionedView() {
		return conditionedView;
	}

	public String getToBeAuthorized() {
		return toBeAuthorized;
	}

	public String getToBeLicensed() {
		return toBeLicensed;
	}

	@Override
	public String toString() {
		return "Item [id=" + id + ", label=" + label + ", requiredFunctionality=" + requiredFunctionality + ", to=" + to + ", command=" + command + ", iconCls="
				+ iconCls + ", condition=" + condition + ", conditionedView=" + conditionedView + ", toBeAuthorized=" + toBeAuthorized + ", toBeLicensed="
				+ toBeLicensed + "]";
	}

}