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
package it.eng.knowage.knowageapi.mapper;

import java.util.function.Function;

import it.eng.knowage.knowageapi.dao.dto.SbiFunctionOutputColumn;
import it.eng.knowage.knowageapi.resource.dto.FunctionOutputColumnDTO;

/**
 * @author Marco Libanori
 */
public class FunctionOutputColumnDTO2SbiFunctionOutputColumn implements Function<FunctionOutputColumnDTO, SbiFunctionOutputColumn> {

	@Override
	public SbiFunctionOutputColumn apply(FunctionOutputColumnDTO t) {
		SbiFunctionOutputColumn ret = new SbiFunctionOutputColumn();

		ret.setColFieldType(t.getFieldType());
		ret.setColType(t.getType());
		ret.getId().setColName(t.getName());

		return ret;
	}

}
