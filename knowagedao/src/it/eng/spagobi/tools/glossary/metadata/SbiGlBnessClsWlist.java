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
import it.eng.spagobi.metadata.metadata.SbiMetaBc;

public class SbiGlBnessClsWlist extends SbiHibernateModel {

	private static final long serialVersionUID = -7917478737232664376L;

	private SbiGlBnessClsWlistId id;

	private SbiGlWord word;
	private SbiMetaBc bness_cls;

	private String column_name;

	public SbiGlBnessClsWlist() {

	}

	/**
	 * @param word
	 * @param bness_cls
	 */
	public SbiGlBnessClsWlist(SbiGlWord word, SbiMetaBc bness_cls, String column_name) {
		super();
		this.word = word;
		this.bness_cls = bness_cls;
		this.column_name = column_name;
	}

	/**
	 * @return the id
	 */
	public SbiGlBnessClsWlistId getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(SbiGlBnessClsWlistId id) {
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
	 * @return the bness_cls
	 */
	public SbiMetaBc getBness_cls() {
		return bness_cls;
	}

	/**
	 * @param bness_cls
	 *            the bness_cls to set
	 */
	public void setBness_cls(SbiMetaBc bness_cls) {
		this.bness_cls = bness_cls;
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

}
