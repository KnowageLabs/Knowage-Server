/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.tools.tag.dao;

import java.util.List;

import org.hibernate.Session;
import org.json.JSONArray;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSetId;
import it.eng.spagobi.tools.tag.SbiDatasetTag;
import it.eng.spagobi.tools.tag.SbiTag;

public interface ISbiTagDAO extends ISpagoBIDao {

	public List<SbiTag> loadTags();

	public List<SbiTag> loadTagsByDatasetId(SbiDataSetId dsId);

	public SbiTag loadTagById(Integer id);

	public SbiTag loadTagByName(String name);

	public List<SbiDataSet> loadDatasetsByTagId(Integer tagId);

	public void insertTag(SbiTag tag);

	public List<SbiDatasetTag> loadDatasetTags(Integer dsId);

	public List<SbiTag> associateTagsToDatasetVersion(SbiDataSetId dsId, JSONArray tagsToAdd);

	public void removeDatasetTags(Integer dsId, Session curSession);

	public void deleteDatasetTag(SbiDatasetTag dsTag);

	public void deleteTag(Integer tagId);

}
