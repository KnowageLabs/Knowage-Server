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
