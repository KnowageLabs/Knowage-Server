/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package it.eng.spagobi.tools.forecastedit.service.rest;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class ForecastValue {

	String source;
	String target;
	double varPerc;
	double varAbs;

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source
	 *            the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @param target
	 *            the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * @return the varPerc
	 */
	public double getVarPerc() {
		return varPerc;
	}

	/**
	 * @param varPerc
	 *            the varPerc to set
	 */
	public void setVarPerc(double varPerc) {
		this.varPerc = varPerc;
	}

	/**
	 * @return the varAbs
	 */
	public double getVarAbs() {
		return varAbs;
	}

	/**
	 * @param varAbs
	 *            the varAbs to set
	 */
	public void setVarAbs(double varAbs) {
		this.varAbs = varAbs;
	}

}
