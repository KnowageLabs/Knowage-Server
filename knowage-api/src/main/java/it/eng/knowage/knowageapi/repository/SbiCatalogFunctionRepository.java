/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.knowageapi.repository;

import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.knowage.boot.error.KnowageBusinessException;
import it.eng.knowage.knowageapi.dao.SbiCatalogFunctionDao;
import it.eng.knowage.knowageapi.dao.dto.SbiCatalogFunction;

/**
 * @author Marco Libanori
 */
@Component
public class SbiCatalogFunctionRepository {

	@Autowired
	private SbiCatalogFunctionDao dao;

	public List<SbiCatalogFunction> findAll() {
		return findAll(null);
	}

	public List<SbiCatalogFunction> findAll(String searchStr) {
		return dao.findAll(searchStr);
	}

	public SbiCatalogFunction find(String id) {
		SbiCatalogFunction.Pk realId = new SbiCatalogFunction.Pk();

		realId.setFunctionId(id);

		return dao.find(realId);
	}

	@Transactional(value = TxType.REQUIRED)
	public SbiCatalogFunction create(SbiCatalogFunction function) {
		return dao.create(function);
	}

	@Transactional(value = TxType.REQUIRED)
	public SbiCatalogFunction update(SbiCatalogFunction function) {
		return dao.update(function);
	}

	@Transactional(value = TxType.REQUIRED)
	public void delete(String id) throws KnowageBusinessException {

		SbiCatalogFunction.Pk realId = new SbiCatalogFunction.Pk();

		realId.setFunctionId(id);

		SbiCatalogFunction entity = dao.find(realId);
		dao.delete(entity);
	}

}
