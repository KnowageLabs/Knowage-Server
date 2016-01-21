package it.eng.spagobi.tools.crossnavigation.bo;

public class SimpleNavigation {

	private Integer id;
	private String name;
	private String fromDoc;
	private String toDoc;

	public SimpleNavigation() {
	}

	public SimpleNavigation(Integer id, String name, String fromDoc, String toDoc) {
		this.id = id;
		this.name = name;
		this.fromDoc = fromDoc;
		this.toDoc = toDoc;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the fromDoc
	 */
	public String getFromDoc() {
		return fromDoc;
	}

	/**
	 * @param fromDoc
	 *            the fromDoc to set
	 */
	public void setFromDoc(String fromDoc) {
		this.fromDoc = fromDoc;
	}

	/**
	 * @return the toDoc
	 */
	public String getToDoc() {
		return toDoc;
	}

	/**
	 * @param toDoc
	 *            the toDoc to set
	 */
	public void setToDoc(String toDoc) {
		this.toDoc = toDoc;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

}
