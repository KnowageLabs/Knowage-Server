/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.maps;

import it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
import it.eng.spagobi.sdk.maps.bo.SDKFeature;
import it.eng.spagobi.sdk.maps.bo.SDKMap;


public interface MapsSDKService {
	
	SDKMap[] getMaps() throws NotAllowedOperationException;

	SDKMap getMapById(Integer mapId) throws NotAllowedOperationException;

	SDKFeature[] getMapFeatures(Integer mapId) throws NotAllowedOperationException;

	SDKFeature[] getFeatures() throws NotAllowedOperationException;

	SDKFeature getFeatureById(Integer featureId) throws NotAllowedOperationException;
	
}
