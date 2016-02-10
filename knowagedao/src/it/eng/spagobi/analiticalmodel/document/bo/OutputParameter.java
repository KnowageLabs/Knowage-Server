package it.eng.spagobi.analiticalmodel.document.bo;

import java.io.Serializable;

public class OutputParameter implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private Integer id;
	private String name;
	private Integer typeId;
	private String typeLbl;
	private Integer biObjectId;
	private boolean newRecord;

	public OutputParameter() {
	}

	public OutputParameter(Integer id, String name, Integer typeId, String typeLbl, Integer biObjectId) {
		super();
		this.id = id;
		this.name = name;
		this.typeId = typeId;
		this.typeLbl = typeLbl;
		this.biObjectId = biObjectId;
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
	 * @return the typeLbl
	 */
	public String getTypeLbl() {
		return typeLbl;
	}

	/**
	 * @param typeLbl
	 *            the typeLbl to set
	 */
	public void setTypeLbl(String typeLbl) {
		this.typeLbl = typeLbl;
	}

	/**
	 * @return the biObjectId
	 */
	public Integer getBiObjectId() {
		return biObjectId;
	}

	/**
	 * @param biObjectId
	 *            the biObjectId to set
	 */
	public void setBiObjectId(Integer biObjectId) {
		this.biObjectId = biObjectId;
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
