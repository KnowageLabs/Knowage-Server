package it.eng.spagobi.kpi.bo;

import it.eng.spagobi.commons.bo.Domain;

import java.io.Serializable;

public class Kpi implements Serializable {

	private static final long serialVersionUID = -3696035077361936505L;
	/**
	 * 
	 */
	private Integer id;
	private String name;
	private String definition;
	/**
	 * A json object
	 */
	private String cardinality;
	/**
	 * A json array
	 */
	private String placeholder;
	/**
	 * domainCd = KPI_KPI_CATEGORY
	 */
	private Domain category;
	private Threshold threshold;

	public Kpi() {
	}

	public Kpi(Integer id) {
		this.id = id;
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
	 * @return the definition
	 */
	public String getDefinition() {
		return definition;
	}

	/**
	 * @param definition
	 *            the definition to set
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	/**
	 * @return the cardinality
	 */
	public String getCardinality() {
		return cardinality;
	}

	/**
	 * @param cardinality
	 *            the cardinality to set
	 */
	public void setCardinality(String cardinality) {
		this.cardinality = cardinality;
	}

	/**
	 * @return the placeholder
	 */
	public String getPlaceholder() {
		return placeholder;
	}

	/**
	 * @param placeholder
	 *            the placeholder to set
	 */
	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	/**
	 * @return the category
	 */
	public Domain getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(Domain category) {
		this.category = category;
	}

	@Override
	public int hashCode() {
		return id == null ? super.hashCode() : id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Kpi && id != null && id.equals(((Kpi) o).getId());
	}

	/**
	 * @return the threshold
	 */
	public Threshold getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold
	 *            the threshold to set
	 */
	public void setThreshold(Threshold threshold) {
		this.threshold = threshold;
	}

}
