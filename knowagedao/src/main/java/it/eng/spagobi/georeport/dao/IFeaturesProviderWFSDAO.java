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
