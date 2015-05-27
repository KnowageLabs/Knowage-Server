package it.eng.spagobi.tools.glossary.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiGlWordAttr extends SbiHibernateModel {

	private static final long serialVersionUID = 8286177194393981144L;

	private SbiGlWordAttrId id;

	private SbiGlWord word;
	private SbiGlAttribute attribute;

	private String value;

	private Integer order;

	public SbiGlWordAttr() {

	}

	/**
	 * @param id
	 * @param word
	 * @param attribute
	 * @param value
	 * @param order
	 */
	public SbiGlWordAttr(SbiGlWordAttrId id, SbiGlWord word, SbiGlAttribute attribute, String value, Integer order) {
		super();
		this.id = id;
		this.word = word;
		this.attribute = attribute;
		this.value = value;
		this.order = order;
	}

	/**
	 * @return the id
	 */
	public SbiGlWordAttrId getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(SbiGlWordAttrId id) {
		this.id = id;
	}

	/**
	 * @return the word
	 */
	public SbiGlWord getWord() {
		return word;
	}

	/**
	 * @param word
	 *            the word to set
	 */
	public void setWord(SbiGlWord word) {
		this.word = word;
	}

	/**
	 * @return the attribute
	 */
	public SbiGlAttribute getAttribute() {
		return attribute;
	}

	/**
	 * @param attribute
	 *            the attribute to set
	 */
	public void setAttribute(SbiGlAttribute attribute) {
		this.attribute = attribute;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the order
	 */
	public Integer getOrder() {
		return order;
	}

	/**
	 * @param order
	 *            the order to set
	 */
	public void setOrder(Integer order) {
		this.order = order;
	}

}
