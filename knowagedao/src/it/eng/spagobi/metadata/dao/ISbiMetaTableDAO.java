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

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public interface ISbiMetaTableDAO extends ISpagoBIDao {

	public SbiMetaTable loadTableByID(Integer id) throws EMFUserError;

	public SbiMetaTable loadTableByName(String name) throws EMFUserError;

	public void modifyTable(SbiMetaTable aMetaTable) throws EMFUserError;

	public void insertTable(SbiMetaTable aMetaTable) throws EMFUserError;

	public void deleteTable(SbiMetaTable aMetaTable) throws EMFUserError;

	public List<SbiMetaTable> loadAllTables() throws EMFUserError;

	public List<SbiMetaTable> loadTablesFromSource(int sourceId) throws EMFUserError;

	public boolean hasBcAssociated(Integer id) throws EMFUserError;

	public boolean hasJobsAssociated(Integer id) throws EMFUserError;
}
