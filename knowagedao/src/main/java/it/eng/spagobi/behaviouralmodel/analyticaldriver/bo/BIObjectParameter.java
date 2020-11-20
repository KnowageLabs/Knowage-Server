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
package it.eng.spagobi.behaviouralmodel.analyticaldriver.bo;

import java.io.Serializable;

/**
 * Defines a <code>BIObjectParameter</code> object.
 *
 * @author Zoppello This class map the SBI_OBJ_PAR table
 */
public class BIObjectParameter extends AbstractDriver implements Serializable, Comparable<BIObjectParameter> {

	/* BIOBJ_ID NUMBER N Business Intelligence Object identifier */
	private Integer biObjectID = null;

	public Integer getBiObjectID() {
		return biObjectID;
	}

	public void setBiObjectID(Integer biObjectID) {
		this.biObjectID = biObjectID;
	}

	/**
	 * Gets the parameter.
	 *
	 * @return the Parameter object
	 */

	@Override
	public BIObjectParameter clone() {
		BIObjectParameter toReturn = new BIObjectParameter();
		toReturn.setLabel(super.getLabel());
		toReturn.setParameterUrlName(super.getParameterUrlName());
		toReturn.setParameterValues(super.getParameterValues());
		toReturn.setParameterValuesDescription(super.getParameterValuesDescription());
		toReturn.setParameter(super.getParameter());
		toReturn.setIterative(super.isIterative());
		toReturn.setTransientParmeters(super.isTransientParmeters());
		toReturn.setHasValidValues(super.hasValidValues());
		toReturn.setBiObjectID(biObjectID);
		toReturn.setId(super.getId());
		toReturn.setModifiable(super.getModifiable());
		toReturn.setMultivalue(super.isMultivalue());
		toReturn.setParID(super.getParID());
		toReturn.setPriority(super.getPriority());
		toReturn.setProg(super.getProg());
		toReturn.setRequired(super.isRequired());
		toReturn.setVisible(super.getVisible());
		toReturn.setColSpan(super.getColSpan());
		toReturn.setThickPerc(super.getThickPerc());

		return toReturn;
	}

	@Override
	public int compareTo(BIObjectParameter arg0) {
		return this.getParameterUrlName().compareTo(arg0.getParameterUrlName());
	}

}
