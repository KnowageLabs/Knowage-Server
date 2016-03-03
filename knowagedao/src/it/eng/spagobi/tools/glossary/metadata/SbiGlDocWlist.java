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

public class SbiGlDocWlist extends SbiHibernateModel {

	private static final long serialVersionUID = -7917478737232664376L;

	private SbiGlDocWlistId id;
	
	private SbiGlWord word;
	private SbiObjects document;


	public SbiGlDocWlist() {

	}

	/**
	 * @param content
	 * @param word
	 * @param document
	 */
	public SbiGlDocWlist( SbiGlWord word,
			SbiObjects document) {
		super();
		this.word = word;
		this.document = document;
	}











	/**
	 * @return the document
	 */
	public SbiObjects getDocument() {
		return document;
	}






	/**
	 * @param document the document to set
	 */
	public void setDocument(SbiObjects document) {
		this.document = document;
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
	 * @return the id
	 */
	public SbiGlDocWlistId getId() {
		return id;
	}



	/**
	 * @param id the id to set
	 */
	public void setId(SbiGlDocWlistId id) {
		this.id = id;
	}


	



}
