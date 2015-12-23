/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.georeport.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;

import java.util.Map;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;

/**
 * @authors Andrea Gioia (andrea.gioia@eng.it), Fabio D'Ovidio (f.dovidio@inovaos.it)
 */
public interface IFeaturesProviderWFSDAO extends ISpagoBIDao {

	SimpleFeature getFeatureById(Object fetureProviderEndPoint, String layerName, Map parameters);

	FeatureCollection getAllFeatures(String srcFile);

	FeatureCollection getAllFeaturesOLD(Object fetureProviderEndPoint, String layerName);

}
