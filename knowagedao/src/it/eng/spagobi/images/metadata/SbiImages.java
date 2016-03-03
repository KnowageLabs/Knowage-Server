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
package it.eng.spagobi.images.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import java.sql.Blob;

public class SbiImages extends SbiHibernateModel {
	private static final long serialVersionUID = -7525846956199531737L;

	private Integer imageId;
	private String name;
	private Blob content;
	private Blob contentIco;

	/**
	 * @return the imageId
	 */
	public Integer getImageId() {
		return imageId;
	}

	/**
	 * @param imageId
	 *            the imageId to set
	 */
	public void setImageId(Integer imageId) {
		this.imageId = imageId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the content
	 */
	public Blob getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(Blob content) {
		this.content = content;
	}

	/**
	 * @return the contentIco
	 */
	public Blob getContentIco() {
		return contentIco;
	}

	/**
	 * @param contentIco
	 *            the contentIco to set
	 */
	public void setContentIco(Blob contentIco) {
		this.contentIco = contentIco;
	}

}
