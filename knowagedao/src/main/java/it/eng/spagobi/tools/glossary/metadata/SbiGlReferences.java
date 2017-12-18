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

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiGlReferences extends SbiHibernateModel {

	private static final long serialVersionUID = 7655394276429952068L;

	private SbiGlReferencesId id;

	private SbiGlWord word;

	private SbiGlWord refWord;

	private Integer sequence;

	public SbiGlReferences() {
	}

	/**
	 * @param id
	 * @param word
	 * @param refWord
	 * @param sequence
	 */
	public SbiGlReferences(SbiGlReferencesId id, SbiGlWord word, SbiGlWord refWord, Integer sequence) {
		super();
		this.id = id;
		this.word = word;
		this.refWord = refWord;
		this.sequence = sequence;
	}

	/**
	 * @return the id
	 */
	public SbiGlReferencesId getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(SbiGlReferencesId id) {
		this.id = id;
	}

	/**
	 * @return the word
	 */
	public SbiGlWord getWord() {
		return word;
	}

	/**
	 * @param word
	 *            the word to set
	 */
	public void setWord(SbiGlWord word) {
		this.word = word;
	}

	/**
	 * @return the refWord
	 */
	public SbiGlWord getRefWord() {
		return refWord;
	}

	/**
	 * @param refWord
	 *            the refWord to set
	 */
	public void setRefWord(SbiGlWord refWord) {
		this.refWord = refWord;
	}

	/**
	 * @return the sequence
	 */
	public Integer getSequence() {
		return sequence;
	}

	/**
	 * @param sequence
	 *            the sequence to set
	 */
	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

}
