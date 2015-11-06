package it.eng.spagobi.mapcatalogue.metadata;

public class SbiGeoLayersRolesId implements java.io.Serializable {

	private static final long serialVersionUID = 3557389011111017484L;

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

	public SbiGeoLayersRolesId(int layer) {
		super();
		this.layer = layer;

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
