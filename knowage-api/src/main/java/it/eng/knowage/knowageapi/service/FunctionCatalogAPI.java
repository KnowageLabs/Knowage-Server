/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.knowage.knowageapi.service;

import java.util.List;
import java.util.UUID;

import it.eng.knowage.boot.error.KnowageBusinessException;
import it.eng.knowage.knowageapi.resource.dto.FunctionCompleteDTO;
import it.eng.knowage.knowageapi.resource.dto.FunctionDTO;

/**
 * @author Marco Libanori
 */
public interface FunctionCatalogAPI {

	default List<FunctionDTO> find() {
		return find(null);
	}

	List<FunctionDTO> find(String search);

	List<FunctionCompleteDTO> findComplete(String search);

	FunctionCompleteDTO get(UUID id);

	FunctionCompleteDTO create(FunctionCompleteDTO functionCatalog);

	FunctionCompleteDTO update(FunctionCompleteDTO functionCatalog);

	void delete(UUID id) throws KnowageBusinessException;
}
