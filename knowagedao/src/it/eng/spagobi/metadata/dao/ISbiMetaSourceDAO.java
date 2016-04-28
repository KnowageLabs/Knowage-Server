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
package it.eng.spagobi.metadata.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.metadata.metadata.SbiMetaSource;
import it.eng.spagobi.metadata.metadata.SbiMetaTable;

import java.util.List;

import org.hibernate.Session;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public interface ISbiMetaSourceDAO extends ISpagoBIDao {

	public SbiMetaSource loadSourceByID(Integer id) throws SpagoBIDOAException;

	public SbiMetaSource loadSourceByName(String name) throws EMFUserError;

	public SbiMetaSource loadSourceByNameAndType(String name, String type) throws EMFUserError;

	public void modifySource(SbiMetaSource aMetaSource) throws SpagoBIDOAException;

	public Integer insertSource(SbiMetaSource aMetaSource) throws EMFUserError;

	public void deleteSource(SbiMetaSource aMetaSource) throws EMFUserError;

	public List<SbiMetaSource> loadAllSources() throws EMFUserError;

	public List<SbiMetaTable> loadMetaTables(Integer sourceId) throws EMFUserError;

	// TRANSACTIONAL METHODS (the session is an input parameter):
	public SbiMetaSource loadSourceByName(Session session, String name) throws EMFUserError;

	public List<SbiMetaTable> loadMetaTables(Session aSession, Integer sourceId) throws EMFUserError;

	public SbiMetaSource loadSourceByNameAndType(Session session, String name, String type) throws EMFUserError;

	public void modifySource(Session session, SbiMetaSource aMetaSource) throws EMFUserError;

	public Integer insertSource(Session session, SbiMetaSource aMetaSource) throws EMFUserError;
}
