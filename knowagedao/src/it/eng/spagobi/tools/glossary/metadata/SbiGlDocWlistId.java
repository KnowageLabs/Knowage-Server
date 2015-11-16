package it.eng.spagobi.tools.glossary.metadata;

import java.io.Serializable;

public class SbiGlDocWlistId implements Serializable {

	private static final long serialVersionUID = 6627335927675526751L;

	private int wordId;
	private int documentId;

	public SbiGlDocWlistId() {
	}



	/**
	 * @param wordId
	 * @param documentId
	 */
	public SbiGlDocWlistId(int wordId, int documentId) {
		super();
		this.wordId = wordId;
		this.documentId = documentId;
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
	 * @return the documentId
	 */
	public int getDocumentId() {
		return documentId;
	}



	/**
	 * @param documentId the documentId to set
	 */
	public void setDocumentId(int documentId) {
		this.documentId = documentId;
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
		SbiGlDocWlistId other = (SbiGlDocWlistId) obj;
		if (documentId != other.documentId)
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
		result = prime * result + documentId;
		result = prime * result + wordId;
		return result;
	}

}

