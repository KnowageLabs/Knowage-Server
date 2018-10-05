package it.eng.qbe.statement.jpa;

public class JPQLJoinPath {

	String sourceEntityAlias;
	String targetPropertyName;

	public JPQLJoinPath(String sourceEntityAlias, String targetPropertyName) {
		this.sourceEntityAlias = sourceEntityAlias;
		this.targetPropertyName = targetPropertyName;
	}

	public String getSourceEntityAlias() {
		return sourceEntityAlias;
	}

	public void setSource(String sourceEntityAlias) {
		this.sourceEntityAlias = sourceEntityAlias;
	}

	public String getTargetPropertyName() {
		return targetPropertyName;
	}

	public void setTargetPropertyName(String targetPropertyName) {
		this.targetPropertyName = targetPropertyName;
	}

	@Override
	public String toString() {

		return sourceEntityAlias + "." + targetPropertyName;
	}

}
