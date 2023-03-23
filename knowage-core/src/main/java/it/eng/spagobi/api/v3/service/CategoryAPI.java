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
package it.eng.spagobi.api.v3.service;

import java.util.List;

import it.eng.spagobi.commons.dao.dto.SbiCategory;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.RuleOutput;
import it.eng.spagobi.kpi.bo.Target;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.dataset.bo.IDataSet;

public interface CategoryAPI {

	public int getNumberOfCategoryUsages(SbiCategory cat);

	List<IDataSet> getDatasetsUsedByCategory(Integer catId);

	List<GeoLayer> getGeoLayersUsedByCategory(Integer catId);

	List<MetaModel> getMetaModelsUsedByCategory(Integer catId);

	List<Kpi> getKPIUsedByCategory(Integer catId);

	List<Target> getKPITargetUsedByCategory(Integer catId);

	List<RuleOutput> getKpiRuleOutputUsedByCategory(Integer catId);

}
