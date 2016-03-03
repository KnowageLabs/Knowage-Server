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

public class SbiGlTableWlistId implements Serializable {

	private static final long serialVersionUID = 6627335927675526751L;

	private int wordId;
	private int tableId;

	public SbiGlTableWlistId() {
	}



	/**
	 * @param wordId
	 * @param documentId
	 */
	public SbiGlTableWlistId(int wordId, int tableId) {
		super();
		this.wordId = wordId;
		this.tableId = tableId;
	}



	/**
	 * @return the wordId
	 */
	public int getWordId() {
		return wordId;
	}



	/**
	 * @param wordId the wordId to set
	 */
	public void setWordId(int wordId) {
		this.wordId = wordId;
	}
 
	
	
	/**
	 * @return the tableId
	 */
	public int getTableId() {
		return tableId;
	}



	/**
	 * @param tableId the tableId to set
	 */
	public void setTableId(int tableId) {
		this.tableId = tableId;
	}



	/* (non-Javadoc)
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
		SbiGlTableWlistId other = (SbiGlTableWlistId) obj;
		if (tableId != other.tableId)
			return false;
		if (wordId != other.wordId)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + tableId;
		result = prime * result + wordId;
		return result;
	}

}

