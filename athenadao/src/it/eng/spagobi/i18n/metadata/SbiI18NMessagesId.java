/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.i18n.metadata;

public class SbiI18NMessagesId implements java.io.Serializable {

	private Integer languageCd;
	private String label;

	public SbiI18NMessagesId() {
	}

	public SbiI18NMessagesId(Integer languageCd, String label) {
		this.languageCd = languageCd;
		this.label = label;
	}

	public Integer getLanguageCd() {
		return this.languageCd;
	}

	public void setLanguageCd(Integer languageCd) {
		this.languageCd = languageCd;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof SbiI18NMessagesId))
			return false;
		SbiI18NMessagesId castOther = (SbiI18NMessagesId) other;

		return ((this.getLanguageCd() == castOther.getLanguageCd()) || (this
				.getLanguageCd() != null && castOther.getLanguageCd() != null && this
				.getLanguageCd().equals(castOther.getLanguageCd())))
				&& ((this.getLabel() == castOther.getLabel()) || (this.getLabel() != null
						&& castOther.getLabel() != null && this.getLabel()
						.equals(castOther.getLabel())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getLanguageCd() == null ? 0 : this.getLanguageCd().hashCode());
		result = 37 * result
				+ (getLabel() == null ? 0 : this.getLabel().hashCode());
		return result;
	}

}
