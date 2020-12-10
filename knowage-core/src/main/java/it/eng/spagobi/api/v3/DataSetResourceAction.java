package it.eng.spagobi.api.v3;

class DataSetResourceAction {

	private final String name;
	private final String description;

	public DataSetResourceAction(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

}

