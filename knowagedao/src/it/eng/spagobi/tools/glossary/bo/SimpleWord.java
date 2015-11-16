package it.eng.spagobi.tools.glossary.bo;

import java.io.Serializable;

public class SimpleWord implements Serializable {

	private Integer wordId;
	private String word;

	/**
	 * @return the wordId
	 */
	public Integer getWordId() {
		return wordId;
	}

	/**
	 * @param wordId
	 *            the wordId to set
	 */
	public void setWordId(Integer wordId) {
		this.wordId = wordId;
	}

	/**
	 * @return the word
	 */
	public String getWord() {
		return word;
	}

	/**
	 * @param word
	 *            the word to set
	 */
	public void setWord(String word) {
		this.word = word;
	}

}
