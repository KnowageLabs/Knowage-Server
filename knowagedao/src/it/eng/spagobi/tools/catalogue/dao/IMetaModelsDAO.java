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
package it.eng.spagobi.tools.catalogue.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;

import java.util.List;

public interface IMetaModelsDAO extends ISpagoBIDao {

	public MetaModel loadMetaModelById(Integer id);

	public MetaModel loadMetaModelByName(String name);

	public List<MetaModel> loadMetaModelByFilter(String filter);

	public List<MetaModel> loadMetaModelByFilter(String filter, List<Integer> categories);

	public List<MetaModel> loadAllMetaModels();

	public void modifyMetaModel(MetaModel model);

	public void insertMetaModel(MetaModel model);

	public void eraseMetaModel(Integer modelId);

	public void insertMetaModelContent(Integer modelId, Content content);

	public void eraseMetaModelContent(Integer contendId);

	public Content loadMetaModelContentById(Integer contendId);

	public Content loadActiveMetaModelContentById(Integer modelId);

	public Content loadActiveMetaModelContentByName(String name);

	public long getActiveMetaModelContentLastModified(String name);

	public List<MetaModel> loadMetaModelByCategories(List<Integer> categories);

	public List<Content> loadMetaModelVersions(Integer modelId);

	public void setActiveVersion(Integer modelId, Integer contendId);

	public String lockMetaModel(Integer metaModelId, String userId);

	public String unlockMetaModel(Integer metaModelId, String userId);
}
