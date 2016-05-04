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
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.metadata.metadata.SbiMetaObjDs;
import it.eng.spagobi.metadata.metadata.SbiMetaObjDsId;

import java.util.List;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public interface ISbiObjDsDAO extends ISpagoBIDao {

	public List<SbiMetaObjDs> loadObjByDsId(Integer dsId) throws EMFUserError;

	public List<SbiMetaObjDs> loadDsByObjId(Integer objId) throws EMFUserError;

	public SbiMetaObjDs loadDsObjByKey(SbiMetaObjDsId objDsId) throws EMFUserError;

	public void modifyObjDs(SbiMetaObjDs aMetaObjDs) throws EMFUserError;

	public void insertObjDs(SbiMetaObjDs aMetaObjDs) throws EMFUserError;

	public void insertUniqueRelationFromObj(BIObject biObj) throws EMFUserError;

	public void deleteObjDs(SbiMetaObjDs aMetaObjDs) throws EMFUserError;

}
