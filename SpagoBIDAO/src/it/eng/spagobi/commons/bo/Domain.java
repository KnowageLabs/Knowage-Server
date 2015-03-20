/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.bo;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;

import java.io.Serializable;
import java.util.Locale;

/**
 * Defines a Domain object.
 */

public class Domain  implements Serializable  {

	private Integer valueId ;
	private String valueCd = "";
	private String valueName = "";
	private String valueDescription = "";
	private String domainCode = "";
	private String domainName = "";

	/**
	 * Gets the value cd.
	 * 
	 * @return Returns the valueCd.
	 */
	public String getValueCd() {
		return valueCd;
	}

	/**
	 * Sets the value cd.
	 * 
	 * @param valueCd The valueCd to set.
	 */
	public void setValueCd(String valueCd) {
		this.valueCd = valueCd;
	}

	/**
	 * Gets the value id.
	 * 
	 * @return Returns the valueId.
	 */
	public Integer  getValueId() {
		return valueId;
	}

	/**
	 * Sets the value id.
	 * 
	 * @param valueId The valueId to set.
	 */
	public void setValueId(Integer  valueId) {
		this.valueId = valueId;
	}

	/**
	 * Gets the value name.
	 * 
	 * @return Returns the valueName.
	 */
	public String getValueName() {
		return valueName;
	}

	/**
	 * Sets the value name.
	 * 
	 * @param valueName The valueName to set.
	 */
	public void setValueName(String valueName) {
		this.valueName = valueName;
	}

	/**
	 * Gets the domain code.
	 * 
	 * @return the domain code
	 */
	public String getDomainCode() {
		return domainCode;
	}

	/**
	 * Sets the domain code.
	 * 
	 * @param domainCode the new domain code
	 */
	public void setDomainCode(String domainCode) {
		this.domainCode = domainCode;
	}

	/**
	 * Gets the domain name.
	 * 
	 * @return the domain name
	 */
	public String getDomainName() {
		return domainName;
	}

	/**
	 * Sets the domain name.
	 * 
	 * @param domainName the new domain name
	 */
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	/**
	 * Gets the value description.
	 * 
	 * @return the value description
	 */
	public String getValueDescription() {
		return valueDescription;
	}

	/**
	 * Sets the value description.
	 * 
	 * @param valueDescription the new value description
	 */
	public void setValueDescription(String valueDescription) {
		this.valueDescription = valueDescription;
	}

	public String getTranslatedValueName(Locale locale){
		String toReturn=new String(valueName);
		MessageBuilder msgBuild=new MessageBuilder();
		toReturn=msgBuild.getMessage(toReturn, locale);
		return toReturn;		

	}

	
	public String getTranslatedValueDescription(Locale locale){
		String toReturn=new String(valueDescription);
		MessageBuilder msgBuild=new MessageBuilder();
		toReturn=msgBuild.getMessage(toReturn, locale);
		return toReturn;		
	}


}




