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
package it.eng.spagobi.tools.udp.dao;

import java.util.List;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.udp.bo.Udp;
import it.eng.spagobi.tools.udp.metadata.SbiUdp;

/**
 *
 * @see it.eng.spagobi.udp.bo.Udp
 * @author Antonella Giachino
 */
public interface IUdpDAO extends ISpagoBIDao {

	Integer insert(SbiUdp prop);

	void update(SbiUdp prop);

	void delete(SbiUdp prop);

	void delete(Integer id);

	SbiUdp findById(Integer id);

	List<SbiUdp> findAll();

	List<Udp> loadAllByFamily(String familyCode) throws EMFUserError;

	List<Udp> loadByFamilyAndLikeLabel(String familyCode, String lab) throws EMFUserError;

	List<SbiUdp> listUdpFromArray(Object[] arr);

	Udp loadByLabel(String label) throws EMFUserError;

	Udp loadByLabelAndFamily(String label, String family) throws EMFUserError;

	Udp loadById(Integer id);

	List<SbiUdp> loadPagedUdpList(Integer offset, Integer fetchSize) throws EMFUserError;

	Integer countUdp() throws EMFUserError;

}
