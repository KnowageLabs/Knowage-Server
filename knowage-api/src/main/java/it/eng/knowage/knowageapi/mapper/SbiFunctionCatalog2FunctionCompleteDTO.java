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

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import it.eng.knowage.knowageapi.dao.dto.SbiCatalogFunction;
import it.eng.knowage.knowageapi.resource.dto.FunctionCompleteDTO;
import it.eng.knowage.knowageapi.resource.dto.FunctionInputColumnDTO;
import it.eng.knowage.knowageapi.resource.dto.FunctionInputVariableDTO;
import it.eng.knowage.knowageapi.resource.dto.FunctionOutputColumnDTO;

/**
 * @author Marco Libanori
 */
public class SbiFunctionCatalog2FunctionCompleteDTO implements Function<SbiCatalogFunction, FunctionCompleteDTO> {

	private static final SbiFunctionInputColumn2FunctionInputColumnDTO TO_FUNCTION_INPUT_COLUMN = new SbiFunctionInputColumn2FunctionInputColumnDTO();
	private static final SbiFunctionInputVariable2FunctionInputVariableDTO TO_FUNCTION_INPUT_VARIABLE = new SbiFunctionInputVariable2FunctionInputVariableDTO();
	private static final SbiFunctionOutputColumn2FunctionOutputColumnDTO TO_FUNCTION_OUTPUT_COLUMN = new SbiFunctionOutputColumn2FunctionOutputColumnDTO();

	@Override
	public FunctionCompleteDTO apply(SbiCatalogFunction t) {
		FunctionCompleteDTO ret = new FunctionCompleteDTO();

		List<FunctionInputColumnDTO> feInputCols = t.getInputColumns()
				.stream()
				.map(TO_FUNCTION_INPUT_COLUMN)
				.collect(toList());

		List<FunctionInputVariableDTO> feInputVars = t.getInputVariables()
				.stream()
				.map(TO_FUNCTION_INPUT_VARIABLE)
				.collect(toList());

		List<FunctionOutputColumnDTO> feOutputCols = t.getOutputColumns()
				.stream()
				.map(TO_FUNCTION_OUTPUT_COLUMN)
				.collect(toList());

		String functionId = t.getId().getFunctionId();
		String name = t.getName();

		UUID feFuntionId = UUID.fromString(functionId);
		// TODO : fix the split on ','
		List<String> feKeywords = Arrays.asList(Optional.ofNullable(t.getKeywords()).orElse("").split(","));

		ret.setBenchmark(t.getBenchmarks());
		ret.setDescription(t.getDescription());
		ret.setFamily(t.getFamily());
		ret.setId(feFuntionId);
		ret.setLabel(t.getLabel());
		ret.setLanguage(t.getLanguage());
		ret.setName(name);
		ret.setOfflineScriptTrain(t.getOfflineScriptTrain());
		ret.setOfflineScriptUse(t.getOfflineScriptUse());
		ret.setOnlineScript(t.getOnlineScript());
		ret.setOwner(t.getOwner());
		ret.getInputColumns().addAll(feInputCols);
		ret.getInputVariables().addAll(feInputVars);
		ret.getTags().addAll(feKeywords);
		ret.getOutputColumns().addAll(feOutputCols);
		ret.setType(t.getType());

		return ret;
	}

}
