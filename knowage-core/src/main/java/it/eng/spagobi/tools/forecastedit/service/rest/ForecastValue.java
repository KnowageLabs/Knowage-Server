/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
