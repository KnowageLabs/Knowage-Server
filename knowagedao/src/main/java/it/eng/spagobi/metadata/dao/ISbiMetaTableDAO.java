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
import it.eng.spagobi.metadata.metadata.SbiMetaTable;

import java.util.List;

import org.hibernate.Session;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public interface ISbiMetaTableDAO extends ISpagoBIDao {

	public SbiMetaTable loadTableByID(Integer id) throws EMFUserError;

	public SbiMetaTable loadTableWithColumnByID(Integer id) throws EMFUserError;

	public SbiMetaTable loadTableByName(String name) throws EMFUserError;

	public SbiMetaTable loadTableByNameAndSource(String name, Integer sourceId) throws EMFUserError;

	public void modifyTable(SbiMetaTable aMetaTable) throws EMFUserError;

	public Integer insertTable(SbiMetaTable aMetaTable) throws EMFUserError;

	public void deleteTable(SbiMetaTable aMetaTable) throws EMFUserError;

	public List<SbiMetaTable> loadAllTables() throws EMFUserError;

	public List<SbiMetaTable> loadPaginatedTables(Integer page, Integer item_per_page, String search) throws EMFUserError;

	public List<SbiMetaTable> loadTablesFromSource(int sourceId) throws EMFUserError;

	public boolean hasBcAssociated(Integer id) throws EMFUserError;

	public boolean hasJobsAssociated(Integer id) throws EMFUserError;

	// TRANSACTIONAL METHODS (the session is an input parameter):

	public SbiMetaTable loadTableByID(Session session, Integer id) throws EMFUserError;

	public SbiMetaTable loadTableByName(Session session, String name) throws EMFUserError;

	public SbiMetaTable loadTableByNameAndSource(Session session, String name, Integer sourceId) throws EMFUserError;

	public void modifyTable(Session session, SbiMetaTable aMetaTable) throws EMFUserError;

	public Integer insertTable(Session session, SbiMetaTable aMetaTable) throws EMFUserError;

	public Integer countSbiMetaTable(String searchText) throws EMFUserError;

}
