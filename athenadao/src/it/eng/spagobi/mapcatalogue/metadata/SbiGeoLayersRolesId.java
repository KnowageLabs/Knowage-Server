package it.eng.spagobi.mapcatalogue.metadata;

public class SbiGeoLayersRolesId {

	private int layer;
	private int role;

	public SbiGeoLayersRolesId() {
		super();
	}

	public SbiGeoLayersRolesId(int layer, int role) {
		super();
		this.layer = layer;
		this.role = role;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

}
