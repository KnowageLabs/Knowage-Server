/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.mapcatalogue.metadata;

import java.util.List;

import it.eng.spagobi.commons.metadata.SbiDomains;
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
	private String pathFile;
	private String layerLabel;
	private String layerName;
	private String layerIdentify;
	private String layerURL;
	private String layerOptions;
	private String layerParams;
	private int layerOrder;
	private Integer category_id;
	private SbiDomains category;
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

	public void setPathFile(String path) {
		this.pathFile = path;

	}

	public String getPathFile() {
		return pathFile;
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

	public String getLayerLabel() {
		return layerLabel;
	}

	public void setLayerLabel(String layerLabel) {
		this.layerLabel = layerLabel;
	}

	public String getLayerName() {
		return layerName;
	}

	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}

	public String getLayerIdentify() {
		return layerIdentify;
	}

	public void setLayerIdentify(String layerIdentify) {
		this.layerIdentify = layerIdentify;
	}

	public String getLayerURL() {
		return layerURL;
	}

	public void setLayerURL(String layerURL) {
		this.layerURL = layerURL;
	}

	public String getLayerOptions() {
		return layerOptions;
	}

	public void setLayerOptions(String layerOptions) {
		this.layerOptions = layerOptions;
	}

	public String getLayerParams() {
		return layerParams;
	}

	public void setLayerParams(String layerParams) {
		this.layerParams = layerParams;
	}

	public int getLayerOrder() {
		return layerOrder;
	}

	public void setLayerOrder(int layerOrder) {
		this.layerOrder = layerOrder;
	}

	public SbiDomains getCategory() {
		return category;
	}

	public void setCategory(SbiDomains category) {
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

	public GeoLayer toGeoLayer() {
		GeoLayer geo = new GeoLayer();
		geo.setDescr(getDescr());
		geo.setLabel(label);
		geo.setLayerDef(layerDef);
		geo.setName(name);
		geo.setType(type);
		geo.setLayerId(layerId);
		geo.setBaseLayer(baseLayer);
		geo.setPathFile(pathFile);
		geo.setLayerLabel(layerLabel);
		geo.setLayerName(layerName);
		geo.setLayerIdentify(layerIdentify);
		geo.setLayerURL(layerURL);
		geo.setLayerOptions(layerOptions);
		geo.setLayerParams(layerParams);
		geo.setLayerOrder(layerOrder);
		geo.setCategory(category);
		geo.setCategory_id(category_id);

		geo.setRoles(roles);

		return geo;
	}

}
