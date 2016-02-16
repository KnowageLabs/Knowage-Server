package it.eng.spagobi.tools.crossnavigation.metadata;

import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjPar;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SbiCrossNavigationPar extends SbiHibernateModel {

	private static final long serialVersionUID = -5674358775970036877L;
	/**
	 * 
	 */
	private Integer id;

	@JsonIgnore
	private SbiCrossNavigation sbiCrossNavigation;
	private SbiObjPar toKey;

	private Integer fromKeyId;
	private Integer toKeyId;
	private Integer fromType;

	private boolean newRecord;

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
	 * @return the toKey
	 */
	public SbiObjPar getToKey() {
		return toKey;
	}

	/**
	 * @param toKey
	 *            the toKey to set
	 */
	public void setToKey(SbiObjPar toKey) {
		this.toKey = toKey;
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

	/**
	 * @return the crossNavigation
	 */
	public SbiCrossNavigation getSbiCrossNavigation() {
		return sbiCrossNavigation;
	}

	/**
	 * @param crossNavigation
	 *            the crossNavigation to set
	 */
	public void setSbiCrossNavigation(SbiCrossNavigation crossNavigation) {
		this.sbiCrossNavigation = crossNavigation;
	}

	/**
	 * @return the fromKeyId
	 */
	public Integer getFromKeyId() {
		return fromKeyId;
	}

	/**
	 * @param fromKeyId
	 *            the fromKeyId to set
	 */
	public void setFromKeyId(Integer fromKeyId) {
		this.fromKeyId = fromKeyId;
	}

	/**
	 * @return the fromType
	 */
	public Integer getFromType() {
		return fromType;
	}

	/**
	 * @param fromType
	 *            the fromType to set
	 */
	public void setFromType(Integer fromType) {
		this.fromType = fromType;
	}

	/**
	 * @return the toKeyId
	 */
	public Integer getToKeyId() {
		return toKeyId;
	}

	/**
	 * @param toKeyId
	 *            the toKeyId to set
	 */
	public void setToKeyId(Integer toKeyId) {
		this.toKeyId = toKeyId;
	}

}
