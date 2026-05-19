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
package it.eng.spagobi.wapp.metadata;

import java.util.HashSet;
import java.util.Set;

import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiHomepage extends SbiHibernateModel {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String type;
	private SbiObjects document;
	private String imageUrl;
	private String staticPage;
	private String html;
	private String css;
	private String menuPlaceholders;
	private Boolean defaultHomepage;
	private Set<SbiExtRoles> sbiHomepageRoles = new HashSet<>(0);

	public Integer getId() {
		return id;
	}

	private void setId(Integer id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public SbiObjects getDocument() {
		return document;
	}

	public void setDocument(SbiObjects document) {
		this.document = document;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getStaticPage() {
		return staticPage;
	}

	public void setStaticPage(String staticPage) {
		this.staticPage = staticPage;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String getMenuPlaceholders() {
		return menuPlaceholders;
	}

	public void setMenuPlaceholders(String menuPlaceholders) {
		this.menuPlaceholders = menuPlaceholders;
	}

	public Boolean getDefaultHomepage() {
		return defaultHomepage;
	}

	public void setDefaultHomepage(Boolean defaultHomepage) {
		this.defaultHomepage = defaultHomepage;
	}

	public Set<SbiExtRoles> getSbiHomepageRoles() {
		return sbiHomepageRoles;
	}

	public void setSbiHomepageRoles(Set<SbiExtRoles> sbiHomepageRoles) {
		this.sbiHomepageRoles = sbiHomepageRoles;
	}

	public void changeId(Integer id) {
		this.setId(id);
	}

}
