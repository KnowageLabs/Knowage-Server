package it.eng.spagobi.tools.glossary.metadata;

import java.io.Serializable;

public class SbiGlBnessClsWlistId implements Serializable {

	private static final long serialVersionUID = 6627335927675526751L;

	private int wordId;
	private int bcId;

	public SbiGlBnessClsWlistId() {
	}



	/**
	 * @param wordId
	 * @param documentId
	 */
	public SbiGlBnessClsWlistId(int wordId, int bcId) {
		super();
		this.wordId = wordId;
		this.bcId = bcId;
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
	 * @return the bcId
	 */
	public int getBcId() {
		return bcId;
	}



	/**
	 * @param bcId the bcId to set
	 */
	public void setBcId(int bcId) {
		this.bcId = bcId;
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
		SbiGlBnessClsWlistId other = (SbiGlBnessClsWlistId) obj;
		if (bcId != other.bcId)
			return false;
		if (wordId != other.wordId)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bcId;
		result = prime * result + wordId;
		return result;
	}

}

