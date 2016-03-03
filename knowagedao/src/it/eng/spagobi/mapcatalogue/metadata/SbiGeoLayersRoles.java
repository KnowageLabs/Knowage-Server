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
package it.eng.spagobi.mapcatalogue.metadata;

import java.util.List;

import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;

public class SbiGeoLayersRoles extends SbiHibernateModel {
	private static final long serialVersionUID = 8286177194393981144L;

	private SbiGeoLayersRolesId id;

	private SbiGeoLayers layer;
	private SbiExtRoles role;

	public SbiGeoLayersRoles() {

	}

	/**
	 * @param id
	 * @param word
	 * @param content
	 * @param order
	 */

	/**
	 * @return the id
	 */
	public SbiGeoLayersRolesId getId() {
		return id;
	}

	public SbiGeoLayersRoles(SbiGeoLayersRolesId id, SbiGeoLayers layer, SbiExtRoles role, String org) {
		super();
		this.id = id;
		this.layer = layer;
		this.role = role;

	}

	public SbiGeoLayersRoles(int layer, int role) {
		super();
		this.id = new SbiGeoLayersRolesId(layer, role);

	}

	public SbiGeoLayersRoles(int layer) {
		super();
		this.id = new SbiGeoLayersRolesId(layer);

	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(SbiGeoLayersRolesId id) {
		this.id = id;
	}

	public SbiGeoLayers getLayer() {
		return layer;
	}

	public void setLayer(SbiGeoLayers layer) {
		this.layer = layer;
	}

	public SbiExtRoles getRole() {
		return role;
	}

	public void setRole(SbiExtRoles role) {
		this.role = role;
	}

	public GeoLayer toGeoLayer(List<SbiExtRoles> roles_list) {
		GeoLayer geo = new GeoLayer();
		geo.setRoles(roles_list);
		return geo;
	}

}
