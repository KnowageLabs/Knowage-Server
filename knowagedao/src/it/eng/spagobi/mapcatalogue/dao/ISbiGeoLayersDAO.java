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
package it.eng.spagobi.mapcatalogue.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;
import it.eng.spagobi.mapcatalogue.metadata.SbiGeoLayersRoles;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public interface ISbiGeoLayersDAO extends ISpagoBIDao {

	public GeoLayer loadLayerByID(Integer layerID) throws EMFUserError;

	public GeoLayer loadLayerByLabel(String label) throws EMFUserError;

	public void modifyLayer(GeoLayer aLayer, Boolean modified) throws EMFUserError, JSONException, UnsupportedEncodingException;

	public List<SbiExtRoles> listRolesFromId(final Object[] arr) throws EMFUserError;

	public Integer insertLayer(GeoLayer aLayer) throws EMFUserError, JSONException, UnsupportedEncodingException, IOException;

	public ArrayList<String> getProperties(int layerId);

	public void eraseLayer(Integer layerId) throws EMFUserError, JSONException;

	public void eraseRole(Integer roleId, Integer layerId) throws EMFUserError;

	public List<GeoLayer> loadAllLayers(String[] listLabel, IEngUserProfile profile) throws EMFUserError, JSONException, UnsupportedEncodingException;

	public String getDescribeFeatureTypeURL(String url);

	public String getWMSDescribeFeatureTypeURL(String url, String layerName);

	public String getOutputFormatKML(String url);

	public String getOutputFormatSHP(String url);

	public JSONObject getContentforDownload(int layerId, String typeWFS);

	public List<SbiGeoLayersRoles> getListRolesById(Integer id);
}
