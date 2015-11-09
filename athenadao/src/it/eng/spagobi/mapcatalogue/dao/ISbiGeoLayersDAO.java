/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.mapcatalogue.dao;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.json.JSONException;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;

public interface ISbiGeoLayersDAO extends ISpagoBIDao {

	public GeoLayer loadLayerByID(Integer layerID) throws EMFUserError;

	public GeoLayer loadLayerByLabel(String label) throws EMFUserError;

	public void modifyLayer(GeoLayer aLayer, Boolean modified) throws EMFUserError, JSONException, UnsupportedEncodingException;

	public List<SbiExtRoles> listRolesFromId(final Object[] arr) throws EMFUserError;

	public Integer insertLayer(GeoLayer aLayer) throws EMFUserError, JSONException, UnsupportedEncodingException;

	public void getContentFile(int idLayer);

	public void eraseLayer(Integer layerId) throws EMFUserError, JSONException;

	public void eraseRole(Integer roleId, Integer layerId) throws EMFUserError;

	public List<GeoLayer> loadAllLayers() throws EMFUserError, JSONException, UnsupportedEncodingException;

}
