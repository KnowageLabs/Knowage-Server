package it.eng.spagobi.tools.glossary.metadata;

import java.io.Serializable;

public class SbiGlDataSetWlistId implements Serializable {

	private static final long serialVersionUID = 6627335927675526751L;

	private int wordId;
	private int datasetId;
	private String organization;
	private String column_name;
	

	public SbiGlDataSetWlistId() {
	}



	/**
	 * @param wordId
	 * @param datasetId
	 */
	public SbiGlDataSetWlistId(int wordId, int datasetId) {
		super();
		this.wordId = wordId;
		this.datasetId = datasetId;
	}



	/**
	 * @param wordId
	 * @param datasetId
	 * @param organization
	 */
	public SbiGlDataSetWlistId(int wordId, int datasetId, String organization,String column_name) {
		super();
		this.wordId = wordId;
		this.datasetId = datasetId;
		this.organization = organization;
		this.column_name = column_name;
	}






	/**
	 * @return the wordId
	 */
	public int getWordId() {
		return wordId;
	}



	/**
	 * @param wordId the wordId to set
	 */
	public void setWordId(int wordId) {
		this.wordId = wordId;
	}

	
	/**
	 * @return the datasetId
	 */
	public int getDatasetId() {
		return datasetId;
	}



	/**
	 * @param datasetId the datasetId to set
	 */
	public void setDatasetId(int datasetId) {
		this.datasetId = datasetId;
	}



	/**
	 * @return the organization
	 */
	public String getOrganization() {
		return organization;
	}



	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}



	/**
	 * @return the column_name
	 */
	public String getColumn_name() {
		return column_name;
	}



	/**
	 * @param column_name the column_name to set
	 */
	public void setColumn_name(String column_name) {
		this.column_name = column_name;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((column_name == null) ? 0 : column_name.hashCode());
		result = prime * result + datasetId;
		result = prime * result
				+ ((organization == null) ? 0 : organization.hashCode());
		result = prime * result + wordId;
		return result;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SbiGlDataSetWlistId other = (SbiGlDataSetWlistId) obj;
		if (column_name == null) {
			if (other.column_name != null)
				return false;
		} else if (!column_name.equals(other.column_name))
			return false;
		if (datasetId != other.datasetId)
			return false;
		if (organization == null) {
			if (other.organization != null)
				return false;
		} else if (!organization.equals(other.organization))
			return false;
		if (wordId != other.wordId)
			return false;
		return true;
	}



	
	
}

