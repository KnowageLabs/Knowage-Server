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

package it.eng.spagobi.tools.news.metadata;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiNews extends SbiHibernateModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String name;
	private String description;
	private boolean active;
	private String news;
	private boolean manual;
	private Date expirationDate;
	private Integer categoryId;
	private SbiDomains sbiDomains;
	private Set<SbiExtRoles> sbiNewsRoles = new HashSet<SbiExtRoles>(0);
	private Set<SbiNewsRead> read = new HashSet<>();

	public SbiNews() {

	}

	public SbiNews(Integer id) {
		this.id = id;
	}

	public SbiNews(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public SbiNews(String name) {
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getNews() {
		return news;
	}

	public void setNews(String news) {
		this.news = news;
	}

	public boolean isManual() {
		return manual;
	}

	public void setManual(boolean manual) {
		this.manual = manual;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public SbiDomains getSbiDomains() {
		return sbiDomains;
	}

	public void setSbiDomains(SbiDomains sbiDomains) {
		this.sbiDomains = sbiDomains;
	}

	public Set<SbiExtRoles> getSbiNewsRoles() {
		return sbiNewsRoles;
	}

	public void setSbiNewsRoles(Set<SbiExtRoles> sbiNewsRoles) {
		this.sbiNewsRoles = sbiNewsRoles;
	}

	public Set<SbiNewsRead> getRead() {
		return read;
	}

	public void setRead(Set<SbiNewsRead> read) {
		this.read = read;
	}

}
