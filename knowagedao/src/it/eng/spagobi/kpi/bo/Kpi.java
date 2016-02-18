package it.eng.spagobi.kpi.bo;

import java.io.Serializable;
import java.util.List;

public class Kpi implements Serializable {

	private static final long serialVersionUID = -3696035077361936505L;
	/**
	 * 
	 */
	private Integer id;
	private String name;
	private String definition;
	private String cardinality; // json
	private String placeholder; // json
	private String category;
	private Integer categoryId;
	private boolean newRecord;
	private List<Threshold> listThreshold; // lazy

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

	/**
	 * @return the categoryId
	 */
	public Integer getCategoryId() {
		return categoryId;
	}

	/**
	 * @param categoryId
	 *            the categoryId to set
	 */
	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	/**
	 * @return the listThreshold
	 */
	public List<Threshold> getListThreshold() {
		return listThreshold;
	}

	/**
	 * @param listThreshold
	 *            the listThreshold to set
	 */
	public void setListThreshold(List<Threshold> listThreshold) {
		this.listThreshold = listThreshold;
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
	 * @return the newRecord
	 */
	public boolean isNewRecord() {
		return newRecord;
	}

	/**
	 * @param newRecord
	 *            the newRecord to set
	 */
	public void setNewRecord(boolean newRecord) {
		this.newRecord = newRecord;
	}

}
