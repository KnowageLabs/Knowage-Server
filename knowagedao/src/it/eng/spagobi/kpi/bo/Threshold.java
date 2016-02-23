package it.eng.spagobi.kpi.bo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Threshold implements Serializable {

	private static final long serialVersionUID = -4276808765746335659L;
	/**
	 * 
	 */
	private Integer id;
	private String description;
	private String name;
	private Integer typeId;
	private String type;

	private final List<ThresholdValue> thresholdValues = new ArrayList<>();

	public Threshold() {
	}

	public Threshold(Integer id) {
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
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
	 * @return the typeId
	 */
	public Integer getTypeId() {
		return typeId;
	}

	/**
	 * @param typeId
	 *            the typeId to set
	 */
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the thresholdValues
	 */
	public List<ThresholdValue> getThresholdValues() {
		return thresholdValues;
	}

	@Override
	public int hashCode() {
		return id == null ? super.hashCode() : id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Threshold && id != null && id.equals(((Threshold) o).getId());
	}
}
