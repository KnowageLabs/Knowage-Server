package it.eng.knowage.knowageapi.mapper;

import java.util.UUID;
import java.util.function.Function;

import it.eng.knowage.knowageapi.dao.dto.SbiCatalogFunction;
import it.eng.knowage.knowageapi.resource.dto.FunctionDTO;

public class SbiFunctionCatalog2FunctionDTO implements Function<SbiCatalogFunction, FunctionDTO> {

	@Override
	public FunctionDTO apply(SbiCatalogFunction t) {
		FunctionDTO ret = new FunctionDTO();

		String functionId = t.getFunctionId();
		String name = t.getName();

		UUID transformedFuntionId = UUID.fromString(functionId);

		ret.setId(transformedFuntionId);
		ret.setName(name);

		return ret;
	}

}
