/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
}
