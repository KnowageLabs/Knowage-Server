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
package it.eng.spagobi.tools.glossary.metadata;

import java.io.Serializable;

public class SbiGlWlistId implements Serializable {

	private static final long serialVersionUID = 6627335927675526751L;

	private int wordId;
	private int contentId;

	public SbiGlWlistId() {
	}

	/**
	 * @param wordId
	 * @param contentId
	 */
	public SbiGlWlistId(int wordId, int contentId) {
		super();
		this.wordId = wordId;
		this.contentId = contentId;
	}

	/**
	 * @return the wordId
	 */
	public int getWordId() {
		return wordId;
	}

	/**
	 * @param wordId
	 *            the wordId to set
	 */
	public void setWordId(int wordId) {
		this.wordId = wordId;
	}

	/**
	 * @return the contentId
	 */
	public int getContentId() {
		return contentId;
	}

	/**
	 * @param contentId
	 *            the contentId to set
	 */
	public void setContentId(int contentId) {
		this.contentId = contentId;
	}

	@Override
	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof SbiGlWlistId))
			return false;
		SbiGlWlistId castOther = (SbiGlWlistId) other;

		return (this.getContentId()) == castOther.getContentId() && (this.getWordId() == castOther.getWordId());
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getWordId();
		result = 37 * result + this.getWordId();
		return result;
	}

}
