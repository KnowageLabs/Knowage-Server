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

public class SbiGlBnessClsWlistId implements Serializable {

	private static final long serialVersionUID = 6627335927675526751L;

	private int wordId;
	private int bcId;
	private String column_name;

	public SbiGlBnessClsWlistId() {
	}

	/**
	 * @param wordId
	 * @param documentId
	 */
	public SbiGlBnessClsWlistId(int wordId, int bcId) {
		super();
		this.wordId = wordId;
		this.bcId = bcId;
	}

	/**
	 * @param wordId
	 * @param bcId
	 * @param column_name
	 */
	public SbiGlBnessClsWlistId(int wordId, int bcId, String column_name) {
		super();
		this.wordId = wordId;
		this.bcId = bcId;
		this.column_name = column_name;
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
	 * @return the bcId
	 */
	public int getBcId() {
		return bcId;
	}

	/**
	 * @param bcId
	 *            the bcId to set
	 */
	public void setBcId(int bcId) {
		this.bcId = bcId;
	}

	/**
	 * @return the column_name
	 */
	public String getColumn_name() {
		return column_name;
	}

	/**
	 * @param column_name
	 *            the column_name to set
	 */
	public void setColumn_name(String column_name) {
		this.column_name = column_name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bcId;
		result = prime * result + ((column_name == null) ? 0 : column_name.hashCode());
		result = prime * result + wordId;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SbiGlBnessClsWlistId other = (SbiGlBnessClsWlistId) obj;
		if (bcId != other.bcId)
			return false;
		if (column_name == null) {
			if (other.column_name != null)
				return false;
		} else if (!column_name.equals(other.column_name))
			return false;
		if (wordId != other.wordId)
			return false;
		return true;
	}

}
