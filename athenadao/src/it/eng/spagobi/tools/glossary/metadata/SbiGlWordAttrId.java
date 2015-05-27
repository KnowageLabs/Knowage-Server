package it.eng.spagobi.tools.glossary.metadata;

import java.io.Serializable;

public class SbiGlWordAttrId implements Serializable {

	private static final long serialVersionUID = 6627335927675526751L;

	private int wordId;
	private int attributeId;

	public SbiGlWordAttrId() {
	}

	/**
	 * @param wordId
	 * @param attributeId
	 */
	public SbiGlWordAttrId(int wordId, int attributeId) {
		super();
		this.wordId = wordId;
		this.attributeId = attributeId;
	}

	/**
	 * @return the wordId
	 */
	public int getWordId() {
		return wordId;
	}

	/**
	 * @param wordId
	 *            the wordId to set
	 */
	public void setWordId(int wordId) {
		this.wordId = wordId;
	}

	/**
	 * @return the attributeId
	 */
	public int getAttributeId() {
		return attributeId;
	}

	/**
	 * @param attributeId
	 *            the attributeId to set
	 */
	public void setAttributeId(int attributeId) {
		this.attributeId = attributeId;
	}

	@Override
	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof SbiGlWordAttrId))
			return false;
		SbiGlWordAttrId castOther = (SbiGlWordAttrId) other;

		return (this.getAttributeId()) == castOther.getAttributeId() && (this.getWordId() == castOther.getWordId());
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getWordId();
		result = 37 * result + this.getWordId();
		return result;
	}

}
