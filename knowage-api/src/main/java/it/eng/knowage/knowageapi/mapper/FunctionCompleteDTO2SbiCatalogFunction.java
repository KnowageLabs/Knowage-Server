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

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import it.eng.knowage.knowageapi.dao.dto.SbiCatalogFunction;
import it.eng.knowage.knowageapi.dao.dto.SbiFunctionInputColumn;
import it.eng.knowage.knowageapi.dao.dto.SbiFunctionInputVariable;
import it.eng.knowage.knowageapi.dao.dto.SbiFunctionOutputColumn;
import it.eng.knowage.knowageapi.resource.dto.FunctionCompleteDTO;

/**
 * @author Marco Libanori
 */
public class FunctionCompleteDTO2SbiCatalogFunction implements Function<FunctionCompleteDTO, SbiCatalogFunction> {

	private static final FunctionInputColumnDTO2SbiFunctionInputColumn TO_SBI_FUNCTION_INPUT_COLUMN = new FunctionInputColumnDTO2SbiFunctionInputColumn();
	private static final FunctionInputVariableDTO2SbiFunctionInputVariable TO_SBI_FUNCTION_INPUT_VARIABLE = new FunctionInputVariableDTO2SbiFunctionInputVariable();
	private static final FunctionOutputColumnDTO2SbiFunctionOutputColumn TO_SBI_FUNCTION_OUTPUT_COLUMN = new FunctionOutputColumnDTO2SbiFunctionOutputColumn();

	@Override
	public SbiCatalogFunction apply(FunctionCompleteDTO t) {
		SbiCatalogFunction ret = new SbiCatalogFunction();

		String functionId = Optional.ofNullable(t.getId())
				.map(UUID::toString)
				.orElse(null);

		List<SbiFunctionInputColumn> beInputCols = t.getInputColumns()
				.stream()
				.map(TO_SBI_FUNCTION_INPUT_COLUMN)
				.map(e -> {
					// Very important!
					e.getId().setFunction(ret);
					return e;
				})
				.collect(toList());

		List<SbiFunctionInputVariable> beInputVars = t.getInputVariables()
				.stream()
				.map(TO_SBI_FUNCTION_INPUT_VARIABLE)
				.map(e -> {
					// Very important!
					e.getId().setFunction(ret);
					return e;
				})
				.collect(toList());

		List<SbiFunctionOutputColumn> beOutputCols = t.getOutputColumns()
				.stream()
				.map(TO_SBI_FUNCTION_OUTPUT_COLUMN)
				.map(e -> {
					// Very important!
					e.getId().setFunction(ret);
					return e;
				})
				.collect(toList());

		String beKeywords = t.getTags()
				.stream()
				.collect(joining(","));

		ret.setBenchmarks(t.getBenchmark());
		ret.setDescription(t.getDescription());
		ret.setFamily(t.getFamily());
		ret.getInputColumns().addAll(beInputCols);
		ret.getInputVariables().addAll(beInputVars);
		ret.setKeywords(beKeywords);
		ret.setLabel(t.getLabel());
		ret.setLanguage(t.getLanguage());
		ret.setName(t.getName());
		ret.setType(t.getType());
		// ret.getObjFunctions() not mapped
		ret.setOfflineScriptTrain(t.getOfflineScriptTrain());
		ret.setOfflineScriptUse(t.getOfflineScriptUse());
		ret.setOnlineScript(t.getOnlineScript());
		ret.setOwner(t.getOwner());
		ret.getOutputColumns().addAll(beOutputCols);
		ret.getId().setFunctionId(functionId);
		// organization and other technical fields are managed by the DAO

		return ret;
	}

}
