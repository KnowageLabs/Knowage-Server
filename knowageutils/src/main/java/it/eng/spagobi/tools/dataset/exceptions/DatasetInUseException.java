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
	ArrayList<String> federationsLabel;
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

	public String getFederationsMessage() {
		String toReturn = "";

		for (Iterator iterator = federationsLabel.iterator(); iterator.hasNext();) {
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

	public ArrayList<String> getFederationsLabel() {
		return federationsLabel;
	}

	public void setFederationsLabel(ArrayList<String> federationsLabel) {
		this.federationsLabel = federationsLabel;
	}

}
