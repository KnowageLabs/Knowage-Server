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
package it.eng.spagobi.mapcatalogue.bo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.dto.SbiCategory;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.mapcatalogue.metadata.SbiGeoLayers;

public class GeoLayer {

	private int layerId;
	private String name;
	private String descr;
	private String type;
	private String label;
	private boolean baseLayer;
	private byte[] layerDef;
	private byte[] Filebody;
	private String pathFile;
	private String layerLabel;
	private String layerName;
	private String layerIdentify;
	private String layerURL;
	private String layerOptions;
	private String layerParams;
	private Integer layerOrder;
	private Integer category_id;
	private SbiCategory category;
	private List<SbiExtRoles> roles;
	private List<String> properties;

	public GeoLayer() {
		super();
	}

	public int getLayerId() {
		return layerId;
	}

	public void setLayerId(int layerId) {
		this.layerId = layerId;
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

	public byte[] getLayerDef() {
		return layerDef;
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

	public void setLayerDef(byte[] layerDef) {
		this.layerDef = layerDef;
		if (layerDef != null) {
			try {

				String layerDefString = new String(layerDef, "UTF-8");
				JSONObject layerDefJson = new JSONObject(layerDefString);

				this.setLayerIdentify(layerDefJson.getString("layerId"));
				this.setLayerLabel(layerDefJson.getString("layerLabel"));
				this.setLayerName(layerDefJson.getString("layerName"));

				if (!layerDefJson.getString("properties").isEmpty()) {
					List<String> prop = new ArrayList<>();
					JSONArray obj = layerDefJson.getJSONArray("properties");

					for (int j = 0; j < obj.length(); j++) {

						prop.add(obj.getString(j));
					}

					this.setProperties(prop);
				}
				if (!layerDefJson.getString("layer_file").equals("null")) {
					this.setPathFile(layerDefJson.getString("layer_file"));

				}
				if (!layerDefJson.getString("layer_url").equals("null")) {
					this.setLayerURL(layerDefJson.getString("layer_url"));

				}
				if (!layerDefJson.getString("layer_params").equals("null")) {
					this.setLayerParams(layerDefJson.getString("layer_params"));
				}
				if (!layerDefJson.getString("layer_options").equals("null")) {
					this.setLayerOptions(layerDefJson.getString("layer_options"));

					if (!layerDefJson.getString("layer_order").equals("null")) {
						this.setLayerOrder(new Integer(layerDefJson.getString("layer_order")));

					}
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {
			this.setLayerIdentify(null);
			this.setLayerLabel(null);
			this.setLayerName(null);
			this.setProperties(null);
			this.setPathFile(null);
			this.setLayerURL(null);
			this.setLayerParams(null);
			this.setLayerOptions(null);
			this.setLayerOrder(null);
		}
	}

	// Chiara add this functions

	public String getPathFile() {
		return pathFile;
	}

	public void setPathFile(String pathFile) {
		this.pathFile = pathFile;
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

	public Integer getLayerOrder() {
		return layerOrder;
	}

	public void setLayerOrder(Integer layerOrder) {
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

	public byte[] getFilebody() {
		return Filebody;
	}

	public void setFilebody(byte[] filebody) {
		Filebody = filebody;
	}

	public List<String> getProperties() {
		return properties;
	}

	public void setProperties(List<String> properties) {
		this.properties = properties;
	}

	public SbiGeoLayers toSpagoBiGeoLayers() throws EMFUserError, EMFInternalError {

		SbiGeoLayers sbm = new SbiGeoLayers();
		sbm.setLayerId(getLayerId());
		sbm.setLabel(getLabel());
		sbm.setName(getName());
		sbm.setDescr(getDescr());
		sbm.setBaseLayer(isBaseLayer());
		sbm.setBodyFile(getFilebody());
		sbm.setCategory(getCategory());
		sbm.setLayerDef(getLayerDef());
		sbm.setLayerOrder(getLayerOrder());
		sbm.setRoles(getRoles());
		sbm.setType(getType());

		return sbm;
	}

}
