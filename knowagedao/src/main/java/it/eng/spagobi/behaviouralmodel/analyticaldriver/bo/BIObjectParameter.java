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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Defines a <code>BIObjectParameter</code> object.
 *
 * @author Zoppello This class map the SBI_OBJ_PAR table
 */
public class BIObjectParameter extends AbstractDriverUsage implements Serializable {

	/* BIOBJ_ID NUMBER N Business Intelligence Object identifier */
	private Integer biObjectID = null;

	/* REQ_FL NUMBER Y Parameter required flag. */
	private Integer required = null;

	/* MOD_FL NUMBER Y Parameter modifiable flag. */
	private Integer modifiable = null;

	/* VIEW_FL NUMBER Y Paramenter visibility flag. */
	@JsonDeserialize(using = BooleanJsonDeserializer.class)
	@JsonSerialize(using = BooleanJsonSerializer.class)
	private Integer visible = null;

	/* MULT_FL NUMBER Y Multivalue parameter. */
	private Integer multivalue = null;

	/**
	 * Gets the bi object id.
	 *
	 * @return Returns the biObjectID.
	 */
	public Integer getBiObjectID() {
		return biObjectID;
	}

	/**
	 * Sets the bi object id.
	 *
	 * @param biObjectID
	 *            The biObjectID to set.
	 */
	public void setBiObjectID(Integer biObjectID) {
		this.biObjectID = biObjectID;
	}

	/**
	 * Gets the modifiable.
	 *
	 * @return Returns the modifiable.
	 */
	public Integer getModifiable() {
		return modifiable;
	}

	/**
	 * Sets the modifiable.
	 *
	 * @param modifiable
	 *            The modifiable to set.
	 */
	public void setModifiable(Integer modifiable) {
		this.modifiable = modifiable;
	}

	/**
	 * Gets the multivalue attribute that is equal to 0 if the parameter is not multivalue, 1 otherwise .
	 *
	 * @return Returns the multivalue.
	 */
	@JsonIgnore
	public Integer getMultivalue() {
		return multivalue;
	}

	@JsonProperty(value = "multivalue")
	public boolean isMultivalue() {
		if (multivalue == null)
			return false;
		return multivalue.intValue() > 0;
	}

	/**
	 * Sets the multivalue.
	 *
	 * @param multivalue
	 *            The multivalue to set.
	 */
	@JsonIgnore
	public void setMultivalue(Integer multivalue) {
		this.multivalue = multivalue;
	}

	public void setMultivalue(boolean multivalue) {
		if (multivalue)
			this.multivalue = 1;
		else
			this.multivalue = 0;
	}

	/**
	 * Gets the parameter url name.
	 *
	 * @return Returns the parameterUrlName.
	 */

	@JsonIgnore
	public Integer getRequired() {
		return required;
	}

	@JsonProperty(value = "required")
	public boolean isRequired() {
		if (required == null)
			return false;
		return required.intValue() > 0;
	}

	/**
	 * Sets the required.
	 *
	 * @param required
	 *            The required to set.
	 */
	@JsonIgnore
	public void setRequired(Integer required) {
		this.required = required;
	}

	public void setRequired(boolean required) {
		if (required)
			this.required = 1;
		else
			this.required = 0;
	}

	/**
	 * Gets the visible.
	 *
	 * @return Returns the visible.
	 */
	public Integer getVisible() {
		return visible;
	}

	/**
	 * Sets the visible.
	 *
	 * @param visible
	 *            The visible to set.
	 */
	public void setVisible(Integer visible) {
		this.visible = visible;
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
		toReturn.setModifiable(modifiable);
		toReturn.setMultivalue(multivalue);
		toReturn.setParID(super.getParID());
		toReturn.setPriority(super.getPriority());
		toReturn.setProg(super.getProg());
		toReturn.setRequired(required);
		toReturn.setVisible(visible);
		toReturn.setColSpan(super.getColSpan());
		toReturn.setThickPerc(super.getThickPerc());

		return toReturn;
	}

}
