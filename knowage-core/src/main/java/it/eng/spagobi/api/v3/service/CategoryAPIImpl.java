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

import static it.eng.spagobi.commons.dao.ICategoryDAO.BUSINESS_MODEL_CATEGORY;
import static it.eng.spagobi.commons.dao.ICategoryDAO.DATASET_CATEGORY;
import static it.eng.spagobi.commons.dao.ICategoryDAO.GEO_CATEGORY;
import static it.eng.spagobi.commons.dao.ICategoryDAO.KPI_CATEGORY;
import static it.eng.spagobi.commons.dao.ICategoryDAO.KPI_RULE_OUTPUT;
import static it.eng.spagobi.commons.dao.ICategoryDAO.KPI_TARGET_CATEGORY;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.dto.SbiCategory;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.RuleOutput;
import it.eng.spagobi.kpi.bo.Target;
import it.eng.spagobi.kpi.dao.IKpiDAO;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoLayersDAO;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class CategoryAPIImpl implements CategoryAPI {

	private static final Logger LOGGER = Logger.getLogger(CategoryAPIImpl.class);

	@Override
	public int getNumberOfCategoryUsages(SbiCategory cat) {

		int numberOfOccurrences = 0;
		if (cat.getType().equals(DATASET_CATEGORY)) {
			numberOfOccurrences = this.getNumberOfCategoryUsagesFromDataset(cat);
		} else if (cat.getType().equals(GEO_CATEGORY)) {
			numberOfOccurrences = this.getNumberOfCategoryUsagesFromGeoLayer(cat);
		} else if (cat.getType().equals(KPI_CATEGORY)) {
			numberOfOccurrences = this.getNumberOfCategoryUsagesFromKPI(cat);
		} else if (cat.getType().equals(KPI_TARGET_CATEGORY)) {
			numberOfOccurrences = this.getNumberOfCategoryUsagesFromKPITarget(cat);
		} else if (cat.getType().equals(KPI_RULE_OUTPUT)) {
			numberOfOccurrences = this.getNumberOfCategoryUsagesFromKPIRuleOutput(cat);
		} else if (cat.getType().equals(BUSINESS_MODEL_CATEGORY)) {
			numberOfOccurrences = this.getNumberOfCategoryUsagesFromMetaModels(cat);
		}
		return numberOfOccurrences;
	}

	private int getNumberOfCategoryUsagesFromDataset(SbiCategory cat) {
		IDataSetDAO dsDao = DAOFactory.getDataSetDAO();
		return dsDao.countCategories(cat.getId());

	}

	private int getNumberOfCategoryUsagesFromGeoLayer(SbiCategory cat) {
		ISbiGeoLayersDAO dsDao = DAOFactory.getSbiGeoLayerDao();
		return dsDao.countCategories(cat.getId());

	}

	private int getNumberOfCategoryUsagesFromKPI(SbiCategory cat) {
		IKpiDAO kpiDao = DAOFactory.getKpiDAO();
		return kpiDao.countCategoriesKPI(cat.getId());

	}

	private int getNumberOfCategoryUsagesFromKPITarget(SbiCategory cat) {
		IKpiDAO kpiDao = DAOFactory.getKpiDAO();
		return kpiDao.countCategoriesKPITarget(cat.getId());

	}

	private int getNumberOfCategoryUsagesFromKPIRuleOutput(SbiCategory cat) {
		IKpiDAO kpiDao = DAOFactory.getKpiDAO();
		return kpiDao.countCategoriesKPIRuleOutput(cat.getId());

	}

	private int getNumberOfCategoryUsagesFromMetaModels(SbiCategory cat) {
		IMetaModelsDAO dsMeta = DAOFactory.getMetaModelsDAO();
		return dsMeta.countCategories(cat.getId());

	}

	@Override
	public List<IDataSet> getDatasetsUsedByCategory(Integer catId) {
		IDataSetDAO dsDao = DAOFactory.getDataSetDAO();
		return dsDao.loadDataSetByCategoryId(catId);

	}

	@Override
	public List<GeoLayer> getGeoLayersUsedByCategory(Integer catId) {
		ISbiGeoLayersDAO dsDao = DAOFactory.getSbiGeoLayerDao();
		try {
			return dsDao.loadLayerByCategoryId(catId);
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

	@Override
	public List<MetaModel> getMetaModelsUsedByCategory(Integer catId) {
		IMetaModelsDAO dsMeta = DAOFactory.getMetaModelsDAO();
		List<Integer> catList = new ArrayList<>();
		catList.add(catId);
		return dsMeta.loadMetaModelByCategories(catList);
	}

	@Override
	public List<Kpi> getKPIUsedByCategory(Integer catId) {
		IKpiDAO kpiDao = DAOFactory.getKpiDAO();
		return kpiDao.listKpiByCategoryId(catId);
	}

	@Override
	public List<Target> getKPITargetUsedByCategory(Integer catId) {
		IKpiDAO kpiDao = DAOFactory.getKpiDAO();
		return kpiDao.listTargetByCategoryId(catId);
	}

	@Override
	public List<RuleOutput> getKpiRuleOutputUsedByCategory(Integer catId) {
		IKpiDAO kpiDao = DAOFactory.getKpiDAO();
		return kpiDao.listRuleOutputByCategoryId(catId);
	}

}
