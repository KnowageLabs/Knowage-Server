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

import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;

public class AdvancedNews extends BasicNews {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	static protected Logger logger = Logger.getLogger(AdvancedNews.class);

	private Date expirationDate;
	private String html;
	private Boolean active;
	private Set roles = null;

	public AdvancedNews() {

	}

	public AdvancedNews(Integer id) {
		super(id);
	}

	public AdvancedNews(Date expirationDate, String html, Boolean active, Set roles) {
		this.expirationDate = expirationDate;
		this.html = html;
		this.active = active;
		this.roles = roles;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Set getRoles() {
		return roles;
	}

	public void setRoles(Set roles) {
		this.roles = roles;
	}

}
