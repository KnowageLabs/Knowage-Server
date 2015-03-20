/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.mapcatalogue.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;

import java.util.List;

public interface ISbiGeoLayersDAO {

	
	public GeoLayer loadLayerByID(Integer layerID) throws EMFUserError;
	
	public GeoLayer loadLayerByLabel(String label) throws EMFUserError;
	
	public void modifyLayer(GeoLayer aLayer) throws EMFUserError;
	
	public Integer insertLayer(GeoLayer aLayer) throws EMFUserError ;

	public void eraseLayer(Integer layerId) throws EMFUserError;

	public List<GeoLayer> loadAllLayers() throws EMFUserError;
	
}
