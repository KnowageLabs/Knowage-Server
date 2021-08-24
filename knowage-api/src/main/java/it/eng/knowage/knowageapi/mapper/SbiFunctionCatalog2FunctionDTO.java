package it.eng.knowage.knowageapi.mapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import it.eng.knowage.knowageapi.dao.dto.SbiCatalogFunction;
import it.eng.knowage.knowageapi.resource.dto.FunctionDTO;

public class SbiFunctionCatalog2FunctionDTO implements Function<SbiCatalogFunction, FunctionDTO> {

	@Override
	public FunctionDTO apply(SbiCatalogFunction t) {
		FunctionDTO ret = new FunctionDTO();

		String functionId = t.getId().getFunctionId();
		String name = t.getName();
		String type = t.getType();
		List<String> keywords = Arrays.asList(Optional.ofNullable(t.getKeywords()).orElse("").split(","));

		UUID transformedFuntionId = UUID.fromString(functionId);

		ret.setId(transformedFuntionId);
		ret.setName(name);
		ret.setType(type);
		ret.getTags().addAll(keywords);

		return ret;
	}

}
