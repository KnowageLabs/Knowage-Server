package it.eng.spagobi.mapcatalogue.metadata;

import it.eng.spagobi.commons.metadata.SbiExtRoles;

public class SbiGeoLayersRoles {
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

	public SbiGeoLayersRoles(SbiGeoLayersRolesId id, SbiGeoLayers layer, SbiExtRoles role) {
		super();
		this.id = id;
		this.layer = layer;
		this.role = role;
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

}
