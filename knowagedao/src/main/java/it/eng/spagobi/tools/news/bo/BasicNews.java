/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.tools.news.bo;

import java.io.Serializable;

import org.apache.log4j.Logger;

public class BasicNews implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	static protected Logger logger = Logger.getLogger(BasicNews.class);

	private Integer id;
	private String title;
	private String description;
	private Integer type;

	public BasicNews() {

	}

	public BasicNews(Integer id) {
		this.id = id;
	}

	public BasicNews(Integer id, String title, String description, Integer type) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.type = type;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

}
