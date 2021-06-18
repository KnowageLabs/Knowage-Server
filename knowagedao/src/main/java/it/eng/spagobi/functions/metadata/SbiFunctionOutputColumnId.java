package it.eng.spagobi.functions.metadata;

public class SbiFunctionOutputColumnId implements java.io.Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7848549421815500755L;
	private String functionUuid;
	private String colName;

	public SbiFunctionOutputColumnId() {
	}

	public SbiFunctionOutputColumnId(String functionUuid, String colName) {
		this.functionUuid = functionUuid;
		this.colName = colName;
	}

	public String getFunctionUuid() {
		return this.functionUuid;
	}

	public void setFunctionUuid(String functionUuid) {
		this.functionUuid = functionUuid;
	}

	public String getColName() {
		return this.colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((colName == null) ? 0 : colName.hashCode());
		result = prime * result + ((functionUuid == null) ? 0 : functionUuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof SbiFunctionOutputColumnId))
			return false;
		SbiFunctionOutputColumnId other = (SbiFunctionOutputColumnId) obj;
		if (colName == null) {
			if (other.colName != null)
				return false;
		} else if (!colName.equals(other.colName))
			return false;
		if (functionUuid == null) {
			if (other.functionUuid != null)
				return false;
		} else if (!functionUuid.equals(other.functionUuid))
			return false;
		return true;
	}
}
