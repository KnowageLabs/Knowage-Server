package it.eng.spagobi.kpi.bo;

import it.eng.spagobi.commons.bo.Domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Target {

	private Integer id;
	private String name;
	private Date startValidity;
	private Date endValidity;
	private String author;
	private List<TargetValue> values = new ArrayList<>();
	/**
	 * domainCd = KPI_TARGET_CATEGORY
	 */
	private Domain category;

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
	 * @return the startValidity
	 */
	public Date getStartValidity() {
		return startValidity;
	}

	/**
	 * @param startValidity
	 *            the startValidity to set
	 */
	public void setStartValidity(Date startValidity) {
		this.startValidity = startValidity;
	}

	/**
	 * @return the endValidity
	 */
	public Date getEndValidity() {
		return endValidity;
	}

	/**
	 * @param endValidity
	 *            the endValidity to set
	 */
	public void setEndValidity(Date endValidity) {
		this.endValidity = endValidity;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author
	 *            the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
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

	/**
	 * @return the values
	 */
	public List<TargetValue> getValues() {
		return values;
	}

	/**
	 * @param values
	 *            the values to set
	 */
	public void setValues(List<TargetValue> values) {
		this.values = values;
	}

}
