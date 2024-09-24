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

public class SbiImages extends SbiHibernateModel {
	private static final long serialVersionUID = -7525846956199531737L;

	private Integer imageId;
	private String name;
	private byte[] content;
	private byte[] contentIco;

	public SbiImages() {
	}

	public SbiImages(Integer imageId) {
		this.setImageId(imageId);
	}

	/**
	 * @return the imageId
	 */
	public Integer getImageId() {
		return imageId;
	}

	/**
	 * @param imageId the imageId to set
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
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the content.
	 *
	 * @return the content
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * Sets the content.
	 *
	 * @param content the new content
	 */
	public void setContent(byte[] content) {
		this.content = content;
	}

	/**
	 * Gets the content icon.
	 *
	 * @return the content
	 */
	public byte[] getContentIco() {
		return contentIco;
	}

	/**
	 * Sets the content icon.
	 *
	 * @param content the new content
	 */
	public void setContentIco(byte[] contentIco) {
		this.contentIco = contentIco;
	}

}
