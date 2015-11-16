package it.eng.spagobi.tools.glossary.metadata;

import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.tools.udp.metadata.SbiUdpValue;

import java.util.Set;

public class SbiGlWord extends SbiHibernateModel {

	private static final long serialVersionUID = 3377527470798741056L;

	private Integer wordId;

	private String word;
	private String descr;
	private String formula;
//	private String state;
//	private String category;

	private Integer state_id;
	private Integer category_id;
	
	private SbiDomains state;
	private SbiDomains category;
	
	private Set<SbiGlReferences> references;
	private Set<SbiUdpValue> attributes;

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
	public SbiGlWord(Integer wordId, String word, String descr, String formula, Integer state, Integer category) {
		super();
		this.wordId = wordId;
		this.word = word;
		this.descr = descr;
		this.formula = formula;
		this.state_id = state;
		this.category_id = category;
	}

	
	/**
	 * @param wordId
	 * @param word
	 * @param descr
	 * @param formula
	 * @param state
	 * @param category
	 * @param references
	 * @param attributes
	 */
	public SbiGlWord(Integer wordId, String word, String descr, String formula,
			SbiDomains state, SbiDomains category,
			Set<SbiGlReferences> references, Set<SbiUdpValue> attributes) {
		super();
		this.wordId = wordId;
		this.word = word;
		this.descr = descr;
		this.formula = formula;
		this.state = state;
		this.category = category;
		this.references = references;
		this.attributes = attributes;
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
	 * @return the state_id
	 */
	public Integer getState_id() {
		return state_id;
	}

	/**
	 * @param state_id the state_id to set
	 */
	public void setState_id(Integer state_id) {
		this.state_id = state_id;
	}

	/**
	 * @return the category_id
	 */
	public Integer getCategory_id() {
		return category_id;
	}

	/**
	 * @param category_id the category_id to set
	 */
	public void setCategory_id(Integer category_id) {
		this.category_id = category_id;
	}

	/**
	 * @return the state
	 */
	public SbiDomains getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(SbiDomains state) {
		this.state = state;
	}

	/**
	 * @return the category
	 */
	public SbiDomains getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(SbiDomains category) {
		this.category = category;
	}

	/**
	 * @return the references
	 */
	public Set<SbiGlReferences> getReferences() {
		return references;
	}

	/**
	 * @param references
	 *            the references to set
	 */
	public void setReferences(Set<SbiGlReferences> references) {
		this.references = references;
	}

	/**
	 * @return the attributes
	 */
	public Set<SbiUdpValue> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(Set<SbiUdpValue> attributes) {
		this.attributes = attributes;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return wordId==null?super.hashCode():wordId.intValue();
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
		SbiGlWord other = (SbiGlWord) obj;
		if (wordId == null) {
			if (other.wordId != null)
				return false;
		} else if (!wordId.equals(other.wordId))
			return false;
		return true;
	}


	
}
