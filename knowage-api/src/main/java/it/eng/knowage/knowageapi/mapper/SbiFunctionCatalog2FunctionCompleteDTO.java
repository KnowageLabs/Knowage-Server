package it.eng.knowage.knowageapi.mapper;

import java.util.UUID;
import java.util.function.Function;

import it.eng.knowage.knowageapi.dao.dto.SbiCatalogFunction;
import it.eng.knowage.knowageapi.resource.dto.FunctionCompleteDTO;

public class SbiFunctionCatalog2FunctionCompleteDTO implements Function<SbiCatalogFunction, FunctionCompleteDTO> {

	@Override
	public FunctionCompleteDTO apply(SbiCatalogFunction t) {
		FunctionCompleteDTO ret = new FunctionCompleteDTO();

		String functionId = t.getFunctionId();
		String name = t.getName();

		UUID transformedFuntionId = UUID.fromString(functionId);

		ret.setId(transformedFuntionId);
		ret.setName(name);

		return ret;
	}

}
