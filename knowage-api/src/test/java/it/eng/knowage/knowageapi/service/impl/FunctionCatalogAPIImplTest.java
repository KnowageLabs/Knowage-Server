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

package it.eng.knowage.knowageapi.service.impl;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import it.eng.knowage.knowageapi.resource.dto.FunctionCompleteDTO;
import it.eng.knowage.knowageapi.resource.dto.FunctionDTO;
import it.eng.knowage.knowageapi.service.FunctionCatalogAPI;

/**
 * @author Marco Libanori
 */
public class FunctionCatalogAPIImplTest implements FunctionCatalogAPI {

	@Override
	public List<FunctionDTO> find(String search) {
		List<FunctionDTO> ret = Lists.<FunctionDTO>newArrayList();

		FunctionDTO el = null;

		el = new FunctionDTO();
		el.setId(UUID.nameUUIDFromBytes(new byte[] { 1 }));
		el.setName("Function 1");
		ret.add(el);

		el = new FunctionDTO();
		el.setId(UUID.nameUUIDFromBytes(new byte[] { 2 }));
		el.setName("Function 2");
		ret.add(el);

		el = new FunctionDTO();
		el.setId(UUID.nameUUIDFromBytes(new byte[] { 3 }));
		el.setName("Function 3");
		ret.add(el);

		return ret;
	}

	@Override
	public FunctionCompleteDTO get(UUID id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FunctionCompleteDTO create(FunctionCompleteDTO functionCatalog) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FunctionCompleteDTO update(FunctionCompleteDTO functionCatalog) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(UUID id) {
		// TODO Auto-generated method stub
	}

	@Override
	public List<FunctionCompleteDTO> findComplete(String search) {
		// TODO Auto-generated method stub
		return null;
	}

}
