package it.eng.spagobi.api.v3;

class DataSetParameterDTO {

	private String name;
	private String type;
	private Object defaultValue;
	private boolean multiValue;

	public DataSetParameterDTO(String name, String type, Object defaultValue, boolean multiValue) {
		super();
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.multiValue = multiValue;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public boolean isMultiValue() {
		return multiValue;
	}
}
