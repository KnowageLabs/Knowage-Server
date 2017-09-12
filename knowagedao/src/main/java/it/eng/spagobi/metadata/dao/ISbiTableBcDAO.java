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
import it.eng.spagobi.metadata.metadata.SbiMetaBc;
import it.eng.spagobi.metadata.metadata.SbiMetaTable;
import it.eng.spagobi.metadata.metadata.SbiMetaTableBc;
import it.eng.spagobi.metadata.metadata.SbiMetaTableBcId;

import java.util.List;

import org.hibernate.Session;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public interface ISbiTableBcDAO extends ISpagoBIDao {

	public List<SbiMetaTable> loadTablesByBcId(Integer bcId) throws EMFUserError;

	public List<SbiMetaBc> loadBcByTableId(Integer tableId) throws EMFUserError;

	public SbiMetaTableBc loadTableBcByBcIdAndTableId(SbiMetaTableBcId tableBcId) throws EMFUserError;

	public void modifyTableBc(SbiMetaTableBc aMetaTableBc) throws EMFUserError;

	public void insertTableBc(SbiMetaTableBc aMetaTableBc) throws EMFUserError;

	public void deleteTableBc(SbiMetaTableBc aMetaTableBc) throws EMFUserError;

	// TRANSACTIONAL METHODS (the session is an input parameter):

	public SbiMetaTableBc loadTableBcByBcIdAndTableId(Session session, SbiMetaTableBcId tableBcId) throws EMFUserError;

	public void modifyTableBc(Session session, SbiMetaTableBc aMetaTableBc) throws EMFUserError;

	public void insertTableBc(Session session, SbiMetaTableBc aMetaTableBc) throws EMFUserError;
}
