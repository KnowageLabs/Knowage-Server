/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.dataset.exceptions;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * @author Giulio Gavardi
 * 
 */
public class DatasetInUseException extends DatasetException {

	ArrayList<String> objectsLabel;
	boolean kpi;
	boolean lov;

	public static final String USER_MESSAGE = "Dataset in use by objects, lovs or kpis ";

	/**
	 * Builds a <code>SpagoBIRuntimeException</code>.
	 * 
	 * @param message
	 *            Text of the exception
	 */
	public DatasetInUseException(String message) {
		super(message);
	}

	/**
	 * Builds a <code>SpagoBIRuntimeException</code>.
	 * 
	 * @param message
	 *            Text of the exception
	 * @param ex
	 *            previous Throwable object
	 */
	public DatasetInUseException(String message, Throwable ex) {
		super(message, ex);
	}

	public String getBiObjectMessage() {
		String toReturn = "";

		for (Iterator iterator = objectsLabel.iterator(); iterator.hasNext();) {
			String label = (String) iterator.next();
			toReturn += label;
			if (iterator.hasNext())
				toReturn += ", ";
		}

		return toReturn;
	}

	public String getKpiMessage() {
		if (kpi)
			return " kpis";
		else
			return "";
	}

	public String getLovMessage() {
		if (lov)
			return " lovs";
		else
			return "";
	}

	public ArrayList<String> getObjectsLabel() {
		return objectsLabel;
	}

	public void setObjectsLabel(ArrayList<String> objectsLabel) {
		this.objectsLabel = objectsLabel;
	}

	public boolean isKpi() {
		return kpi;
	}

	public void setKpi(boolean kpi) {
		this.kpi = kpi;
	}

	public boolean isLov() {
		return lov;
	}

	public void setLov(boolean lov) {
		this.lov = lov;
	}

}
