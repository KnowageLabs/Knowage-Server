package it.eng.spagobi.functions.metadata;

public class SbiFunctionOutputColumnId implements java.io.Serializable {

	private int functionId;
	private String colName;

	// private int versionNum;
	// private String organization;

	public SbiFunctionOutputColumnId() {
	}

	public SbiFunctionOutputColumnId(int functionId, String colName/* , int versionNum , String organization */) {
		this.functionId = functionId;
		this.colName = colName;
		// this.versionNum = versionNum;
		// this.organization = organization;
	}

	public int getFunctionId() {
		return this.functionId;
	}

	public void setFunctionId(int functionId) {
		this.functionId = functionId;
	}

	public String getColName() {
		return this.colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	/*
	 * public int getVersionNum() { return this.versionNum; }
	 *
	 * public void setVersionNum(int versionNum) { this.versionNum = versionNum; }
	 *
	 * public String getOrganization() { return this.organization; }
	 *
	 * public void setOrganization(String organization) { this.organization = organization; }
	 */

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((colName == null) ? 0 : colName.hashCode());
		result = prime * result + functionId;
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
		if (functionId != other.functionId)
			return false;
		return true;
	}
}
