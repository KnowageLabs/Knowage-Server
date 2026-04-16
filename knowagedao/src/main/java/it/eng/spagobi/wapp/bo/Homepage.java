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
package it.eng.spagobi.wapp.bo;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Homepage implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Integer id;
	private boolean defaultHomepage;
	private String type;
	private Integer documentId;
	private String imageUrl;
	private String staticPage;
	private HomepageTemplate template;
	private List<Integer> roleIds = new ArrayList<>();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public boolean isDefaultHomepage() {
		return defaultHomepage;
	}

	public void setDefaultHomepage(boolean defaultHomepage) {
		this.defaultHomepage = defaultHomepage;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getDocumentId() {
		return documentId;
	}

	public void setDocumentId(Integer documentId) {
		this.documentId = documentId;
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

	public HomepageTemplate getTemplate() {
		return template;
	}

	public void setTemplate(HomepageTemplate template) {
		this.template = template;
	}

	public List<Integer> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(List<Integer> roleIds) {
		this.roleIds = roleIds == null ? new ArrayList<>() : roleIds;
	}

}
