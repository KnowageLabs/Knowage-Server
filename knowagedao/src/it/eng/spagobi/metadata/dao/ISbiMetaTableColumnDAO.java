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
import it.eng.spagobi.metadata.metadata.SbiMetaTableColumn;

import java.util.List;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public interface ISbiMetaTableColumnDAO extends ISpagoBIDao {

	public SbiMetaTableColumn loadTableColumnByID(Integer id) throws EMFUserError;

	public SbiMetaTableColumn loadTableColumnByName(String name) throws EMFUserError;

	public SbiMetaTableColumn loadTableColumnByNameAndTable(String name, Integer tableId) throws EMFUserError;

	public void modifyTableColumn(SbiMetaTableColumn aMetaTableColumn) throws EMFUserError;

	public Integer insertTableColumn(SbiMetaTableColumn aMetaTableColumn) throws EMFUserError;

	public void deleteTableColumn(SbiMetaTableColumn aMetaTableColumn) throws EMFUserError;

	public List<SbiMetaTableColumn> loadTableColumnsFromTable(int tableId) throws EMFUserError;
}
