package it.eng.spagobi.tools.crossnavigation.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import java.util.Set;

public class SbiCrossNavigation extends SbiHibernateModel {

	private static final long serialVersionUID = -5674358775970036877L;
	/**
	 * 
	 */
	private Integer id;

	private String name;
	private Set<SbiCrossNavigationPar> sbiCrossNavigationPars;

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
	 * @return the parameters
	 */
	public Set<SbiCrossNavigationPar> getSbiCrossNavigationPars() {
		return sbiCrossNavigationPars;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setSbiCrossNavigationPars(Set<SbiCrossNavigationPar> sbiCrossNavigationPars) {
		this.sbiCrossNavigationPars = sbiCrossNavigationPars;
	}

}
