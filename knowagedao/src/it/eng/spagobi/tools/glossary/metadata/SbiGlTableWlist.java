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

import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiGlTableWlist extends SbiHibernateModel {

	private static final long serialVersionUID = -7917478737232664376L;

	private SbiGlTableWlistId id;
	
	private SbiGlWord word;
	private SbiGlTable table;


	public SbiGlTableWlist() {

	}


	/**
	 * @param word
	 * @param table
	 */
	public SbiGlTableWlist(SbiGlWord word, SbiGlTable table) {
		super();
		this.word = word;
		this.table = table;
	}


	/**
	 * @return the id
	 */
	public SbiGlTableWlistId getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(SbiGlTableWlistId id) {
		this.id = id;
	}


	/**
	 * @return the word
	 */
	public SbiGlWord getWord() {
		return word;
	}


	/**
	 * @param word the word to set
	 */
	public void setWord(SbiGlWord word) {
		this.word = word;
	}


	/**
	 * @return the table
	 */
	public SbiGlTable getTable() {
		return table;
	}


	/**
	 * @param table the table to set
	 */
	public void setTable(SbiGlTable table) {
		this.table = table;
	}

	
}
