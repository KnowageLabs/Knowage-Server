/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.
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

/**
 *
 */
package it.eng.spagobi.commons.metadata;

/**
 * @author albnale
 *
 */
public class SbiCockpitWidget extends SbiHibernateModel {

	private Integer sbiCockpitWidgetId = null;
	private Integer biobjId = null;
	private String tab = null;
	private String widgetType = null;
	private Integer dsId = null;
	private Boolean associative = false;
	private Boolean cache = false;
	private Boolean filters = false;

	public Integer getSbiCockpitWidgetId() {
		return sbiCockpitWidgetId;
	}

	public void setSbiCockpitWidgetId(Integer sbiCockpitWidgetId) {
		this.sbiCockpitWidgetId = sbiCockpitWidgetId;
	}

	public Integer getBiobjId() {
		return biobjId;
	}

	public void setBiobjId(Integer biobjId) {
		this.biobjId = biobjId;
	}

	public String getTab() {
		return tab;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}

	public String getWidgetType() {
		return widgetType;
	}

	public void setWidgetType(String widgetType) {
		this.widgetType = widgetType;
	}

	public Integer getDsId() {
		return dsId;
	}

	public void setDsId(Integer dsId) {
		this.dsId = dsId;
	}

	public Boolean getAssociative() {
		return associative;
	}

	public void setAssociative(Boolean associative) {
		this.associative = associative;
	}

	public Boolean getCache() {
		return cache;
	}

	public void setCache(Boolean cache) {
		this.cache = cache;
	}

	public Boolean getFilters() {
		return filters;
	}

	public void setFilters(Boolean filters) {
		this.filters = filters;
	}

}
