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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HomepageTemplate implements Serializable {

	private static final long serialVersionUID = 1L;

	private String html;
	private String css;
	private List<MenuPlaceholder> menuPlaceholders = new ArrayList<>();

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

	public List<MenuPlaceholder> getMenuPlaceholders() {
		return menuPlaceholders;
	}

	public void setMenuPlaceholders(List<MenuPlaceholder> menuPlaceholders) {
		this.menuPlaceholders = menuPlaceholders == null ? new ArrayList<>() : menuPlaceholders;
	}

}
