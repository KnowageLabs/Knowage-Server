/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.catalogue.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.catalogue.bo.Artifact;
import it.eng.spagobi.tools.catalogue.bo.Content;

import java.util.List;

public interface IArtifactsDAO extends ISpagoBIDao {

	public Artifact loadArtifactById(Integer id);
	
	public Artifact loadArtifactByNameAndType(String name, String type);

	public List<Artifact> loadAllArtifacts(String type);
	
	public void modifyArtifact(Artifact artifact);
	
	public void insertArtifact(Artifact artifact);
	
	public void eraseArtifact(Integer id);

	public void insertArtifactContent(Integer artifactId, Content content);
	
	public void eraseArtifactContent(Integer contendId);
	
	public Content loadArtifactContentById(Integer contendId);
	
	public Content loadActiveArtifactContent(Integer artifactId);
	
	public List<Content> loadArtifactVersions(Integer artifactId);
	
	public void setActiveVersion(Integer artifactId, Integer contendId);
	
	public String lockArtifact(Integer artifactId, String userId);
	
	public String unlockArtifact(Integer artifactId, String userId);
	
	public Artifact loadArtifactByContentId(Integer contendId);

	
	
}
