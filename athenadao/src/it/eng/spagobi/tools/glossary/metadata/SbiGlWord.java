package it.eng.spagobi.tools.glossary.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiGlWord extends SbiHibernateModel {

	private static final long serialVersionUID = 3377527470798741056L;

	private Integer wordId;

	private String word;
	private String descr;
	private String formula;
	private String state;
	private String category;

	public SbiGlWord() {
	}

	/**
	 * @param wordId
	 * @param word
	 * @param descr
	 * @param formula
	 * @param state
	 * @param category
	 */
	public SbiGlWord(Integer wordId, String word, String descr, String formula, String state, String category) {
		super();
		this.wordId = wordId;
		this.word = word;
		this.descr = descr;
		this.formula = formula;
		this.state = state;
		this.category = category;
	}

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

	/**
	 * @return the descr
	 */
	public String getDescr() {
		return descr;
	}

	/**
	 * @param descr
	 *            the descr to set
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}

	/**
	 * @return the formula
	 */
	public String getFormula() {
		return formula;
	}

	/**
	 * @param formula
	 *            the formula to set
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

}
