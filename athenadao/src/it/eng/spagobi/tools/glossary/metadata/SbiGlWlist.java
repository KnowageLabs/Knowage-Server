package it.eng.spagobi.tools.glossary.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiGlWlist extends SbiHibernateModel {

	private static final long serialVersionUID = 8286177194393981144L;

	private SbiGlWlistId id;

	private SbiGlWord word;
	private SbiGlContents content;

	private Integer order;

	public SbiGlWlist() {

	}

	/**
	 * @param id
	 * @param word
	 * @param content
	 * @param order
	 */
	public SbiGlWlist(SbiGlWlistId id, SbiGlWord word, SbiGlContents content, Integer order) {
		super();
		this.id = id;
		this.word = word;
		this.content = content;
		this.order = order;
	}

	/**
	 * @return the id
	 */
	public SbiGlWlistId getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(SbiGlWlistId id) {
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
	 * @return the content
	 */
	public SbiGlContents getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(SbiGlContents content) {
		this.content = content;
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
