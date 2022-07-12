/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.commons.dao;

import java.util.List;

import org.hibernate.Session;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.dto.SbiCategory;

/**
 * Defines the interfaces for all methods needed to operate with a category.
 *
 * @author Marco Libanori
 */
public interface ICategoryDAO extends ISpagoBIDao {

	String BUSINESS_MODEL_CATEGORY = "BM_CATEGORY";
	String DATASET_CATEGORY = "DATASET_CATEGORY";
	String GEO_CATEGORY = "GEO_CATEGORY";
	String KPI_CATEGORY = "KPI_KPI_CATEGORY";
	String KPI_MEASURE_CATEGORY = "KPI_MEASURE_CATEGORY";
	String KPI_TARGET_CATEGORY = "KPI_TARGET_CATEGORY";

	// Queries

	List<SbiCategory> getCategories(String type);

	SbiCategory getCategory(int id);

	SbiCategory getCategory(Session aSession, int id);

	SbiCategory getCategory(String type, String name);

	// Commands

	SbiCategory create(SbiCategory category) throws EMFUserError;

	void update(SbiCategory category) throws EMFUserError;

	void delete(SbiCategory category) throws EMFUserError;

	// Defaults

	default SbiCategory getCategoryForBusinessModel(String name) {
		return getCategory(BUSINESS_MODEL_CATEGORY, name);
	}

	default SbiCategory getCategoryForDataSet(String name) {
		return getCategory(DATASET_CATEGORY, name);
	}

	default List<SbiCategory> getCategoriesForBusinessModel() {
		return getCategories(BUSINESS_MODEL_CATEGORY);
	}

	default List<SbiCategory> getCategoriesForDataset() {
		return getCategories(DATASET_CATEGORY);
	}

	default List<SbiCategory> getCategoriesForKpi() {
		return getCategories(KPI_CATEGORY);
	}

	default List<SbiCategory> getCategoriesForKpiTarget() {
		return getCategories(KPI_TARGET_CATEGORY);
	}

	default SbiCategory getCategoryForGeoReport(String name) {
		return getCategory(GEO_CATEGORY, name);
	}

	default List<SbiCategory> getCategoriesForGeoReport() {
		return getCategories(GEO_CATEGORY);
	}

	default List<SbiCategory> getCategoriesForKpiMeasure() {
		return getCategories(KPI_MEASURE_CATEGORY);
	}

	default SbiCategory create(String code, String name, String type) throws EMFUserError {
		SbiCategory category = new SbiCategory();

		category.setCode(code);
		category.setName(name);
		category.setType(type);

		return create(category);
	}

}