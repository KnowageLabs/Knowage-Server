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

package it.eng.spagobi.tools.dataset.measurecatalogue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 *         This class search the measures in the catalogue. There is more than one type os searc
 *
 */
public class MeasureCatalogueSearchManager {

	/**
	 * Search the measures that has a property with a specific value
	 *
	 * @param measureCatalogue
	 * @param alias
	 * @return
	 */
	public static List<MeasureCatalogueMeasure> searchMeasureByPropery(MeasureCatalogue measureCatalogue, String propertyName, Object propertyValue) {
		List<MeasureCatalogueMeasure> toreturn = new ArrayList<MeasureCatalogueMeasure>();
		for (Iterator<MeasureCatalogueMeasure> iterator = measureCatalogue.getMeasures().iterator(); iterator.hasNext();) {
			MeasureCatalogueMeasure measure = iterator.next();
			Object property = measure.getProperty(propertyName);
			if (property != null && property.equals(propertyValue)) {
				toreturn.add(measure);
			}
		}
		return toreturn;
	}

	/**
	 * Search the measures by alias
	 *
	 * @param measureCatalogue
	 * @param alias
	 * @return
	 */
	public static List<MeasureCatalogueMeasure> searchMeasureByAlias(MeasureCatalogue measureCatalogue, String alias) {
		List<MeasureCatalogueMeasure> toreturn = new ArrayList<MeasureCatalogueMeasure>();
		for (Iterator<MeasureCatalogueMeasure> iterator = measureCatalogue.getMeasures().iterator(); iterator.hasNext();) {
			MeasureCatalogueMeasure measure = iterator.next();
			if (measure.getAlias().equals(alias)) {
				toreturn.add(measure);
			}
		}
		return toreturn;
	}

}
