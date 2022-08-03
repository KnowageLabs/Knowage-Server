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

import it.eng.spagobi.commons.dao.dto.SbiCategory;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;

public class SbiGeoLayers extends SbiHibernateModel {

	private static final long serialVersionUID = 9129008218416362745L;
	private int layerId;
	private String name;
	private String descr;
	private String type;
	private boolean baseLayer = false;
	private String label;
	private byte[] layerDef;
	private byte[] bodyFile;
	private int layerOrder;
	private Integer category_id;
	private SbiCategory category;
	private List<SbiExtRoles> roles;

	public SbiGeoLayers() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getLayerId() {
		return layerId;
	}

	public void setLayerId(int layerId) {
		this.layerId = layerId;
	}

	public byte[] getLayerDef() {
		return layerDef;
	}

	public void setLayerDef(byte[] layerDef) {
		this.layerDef = layerDef;
	}

	/**
	 * @return the isBaseLayer
	 */
	public boolean isBaseLayer() {
		return baseLayer;
	}

	/**
	 * @param isBaseLayer
	 *            the isBaseLayer to set
	 */
	public void setBaseLayer(boolean baseLayer) {
		this.baseLayer = baseLayer;
	}

	public int getLayerOrder() {
		return layerOrder;
	}

	public void setLayerOrder(int layerOrder) {
		this.layerOrder = layerOrder;
	}

	public SbiCategory getCategory() {
		return category;
	}

	public void setCategory(SbiCategory category) {
		this.category = category;
	}

	public Integer getCategory_id() {
		return category_id;
	}

	public void setCategory_id(Integer category_id) {
		this.category_id = category_id;
	}

	public List<SbiExtRoles> getRoles() {
		return roles;
	}

	public void setRoles(List<SbiExtRoles> roles) {
		this.roles = roles;
	}

	public byte[] getBodyFile() {
		return bodyFile;
	}

	public void setBodyFile(byte[] bodyFile) {
		this.bodyFile = bodyFile;
	}

	public GeoLayer toGeoLayer() {
		GeoLayer geo = new GeoLayer();
		geo.setDescr(getDescr());
		geo.setLabel(label);
		geo.setFilebody(bodyFile);
		geo.setName(name);
		geo.setType(type);
		geo.setLayerDef(layerDef);
		geo.setLayerId(layerId);
		geo.setBaseLayer(baseLayer);
		geo.setLayerOrder(layerOrder);
		geo.setCategory(category);
		geo.setCategory_id(category_id);

		geo.setRoles(roles);

		return geo;
	}

}
