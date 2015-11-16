package it.eng.spagobi.tools.glossary.metadata;

import java.io.Serializable;

public class SbiGlWlistId implements Serializable {

	private static final long serialVersionUID = 6627335927675526751L;

	private int wordId;
	private int contentId;

	public SbiGlWlistId() {
	}

	/**
	 * @param wordId
	 * @param contentId
	 */
	public SbiGlWlistId(int wordId, int contentId) {
		super();
		this.wordId = wordId;
		this.contentId = contentId;
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
	 * @return the contentId
	 */
	public int getContentId() {
		return contentId;
	}

	/**
	 * @param contentId
	 *            the contentId to set
	 */
	public void setContentId(int contentId) {
		this.contentId = contentId;
	}

	@Override
	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof SbiGlWlistId))
			return false;
		SbiGlWlistId castOther = (SbiGlWlistId) other;

		return (this.getContentId()) == castOther.getContentId() && (this.getWordId() == castOther.getWordId());
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getWordId();
		result = 37 * result + this.getWordId();
		return result;
	}

}
