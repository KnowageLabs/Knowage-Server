/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.tools.tag;

import java.io.Serializable;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiTag extends SbiHibernateModel implements Serializable {

	private static final long serialVersionUID = 4148097877725250815L;

	private Integer tagId;
	private String name;

	public SbiTag() {

	}

	public SbiTag(Integer tagId, String name) {
		this.tagId = tagId;
		this.name = name;
	}

	public SbiTag(String name) {
		this.name = name;
	}

	public Integer getTagId() {
		return tagId;
	}

	public void setTagId(Integer tagId) {
		this.tagId = tagId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
