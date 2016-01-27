package it.eng.spagobi.tools.crossnavigation.bo;

import java.util.ArrayList;
import java.util.List;

public class NavigationDetail {

	private SimpleNavigation simpleNavigation;
	private List<SimpleParameter> fromPars = new ArrayList<>();
	private List<SimpleParameter> toPars = new ArrayList<>();
	private boolean newRecord;

	/**
	 * @return the fromPars
	 */
	public List<SimpleParameter> getFromPars() {
		return fromPars;
	}

	/**
	 * @param fromPars
	 *            the fromPars to set
	 */
	public void setFromPars(List<SimpleParameter> fromPars) {
		this.fromPars = fromPars;
	}

	/**
	 * @return the toPars
	 */
	public List<SimpleParameter> getToPars() {
		return toPars;
	}

	/**
	 * @param toPars
	 *            the toPars to set
	 */
	public void setToPars(List<SimpleParameter> toPars) {
		this.toPars = toPars;
	}

	/**
	 * @return the isNewRecord
	 */
	public boolean isNewRecord() {
		return newRecord;
	}

	/**
	 * @param isNewRecord
	 *            the isNewRecord to set
	 */
	public void setNewRecord(boolean newRecord) {
		this.newRecord = newRecord;
	}

	/**
	 * @return the simpleNavigation
	 */
	public SimpleNavigation getSimpleNavigation() {
		return simpleNavigation;
	}

	/**
	 * @param simpleNavigation
	 *            the simpleNavigation to set
	 */
	public void setSimpleNavigation(SimpleNavigation simpleNavigation) {
		this.simpleNavigation = simpleNavigation;
	}

}
