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
