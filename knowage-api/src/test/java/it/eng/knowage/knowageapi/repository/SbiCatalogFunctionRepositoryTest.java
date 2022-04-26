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

import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import it.eng.knowage.boot.error.KnowageBusinessException;
import it.eng.knowage.knowageapi.dao.dto.SbiCatalogFunction;
import it.eng.knowage.knowageapi.dao.dto.SbiFunctionInputColumn;
import it.eng.knowage.knowageapi.dao.dto.SbiFunctionInputVariable;
import it.eng.knowage.knowageapi.dao.dto.SbiFunctionOutputColumn;
import it.eng.knowage.knowageapi.dao.dto.SbiObjFunction;

/**
 * @author Marco Libanori
 */
@SpringBootTest
@ActiveProfiles("test")
class SbiCatalogFunctionRepositoryTest {

	@Autowired
	private SbiCatalogFunctionRepository repository;

	@Test
	void createAndDelete() throws KnowageBusinessException {

		// Create
		SbiCatalogFunction function = createRandomName();

		function = repository.create(function);

		// Update
		function.setDescription(RandomStringUtils.randomAlphanumeric(12));
		SbiFunctionInputColumn inCol = createInputColumn(function);
		function.getInputColumns().add(inCol);

		repository.update(function);

		// Delete
		String functionId = function.getId().getFunctionId();
		repository.delete(functionId);

		// Check
		function = repository.find(functionId);

		assertNull(function);
	}

	private SbiCatalogFunction createRandomName() {
		String name = RandomStringUtils.randomAlphanumeric(12);

		return create(name);
	}

	private SbiCatalogFunction create(String name) {
		String label = RandomStringUtils.randomAlphanumeric(12);

		SbiCatalogFunction n = new SbiCatalogFunction();

		Set<SbiFunctionInputColumn> inputColumns = new HashSet<>();
		Set<SbiFunctionInputVariable> inputVariables = new HashSet<>();
		Set<SbiObjFunction> objFunctions = new HashSet<>();
		Set<SbiFunctionOutputColumn> outputColumns = new HashSet<>();

		SbiFunctionInputColumn inCol = createInputColumn(n);

		inputColumns.add(inCol);

		inCol = createInputColumn(n);

		inputColumns.add(inCol);

		SbiFunctionOutputColumn outCol = new SbiFunctionOutputColumn();
		outCol.setColFieldType("type");
		outCol.setColType("type");
		outCol.getId().setColName("name");
		outCol.getId().setFunction(n);

		outputColumns.add(outCol);

		SbiFunctionInputVariable inVar = new SbiFunctionInputVariable();
		inVar.getId().setVarName("name");
		inVar.setVarType("type");
		inVar.setVarValue("value");
		inVar.getId().setFunction(n);

		inputVariables.add(inVar);

		n.setBenchmarks("benchmark");
		n.setDescription("description");
		n.setFamily("family");
		n.setInputColumns(inputColumns);
		n.setInputVariables(inputVariables);
		n.setKeywords("keyword");
		n.setLabel(label);
		n.setLanguage("language");
		n.setName(name);
//		n.setObjFunctions(objFunctions);
		n.setOfflineScriptTrain("offlineScriptTrain");
		n.setOfflineScriptUse("offlineScriptUse");
		n.setOnlineScript("onlineScript");
		n.setOutputColumns(outputColumns);
		n.setOwner("biadmin");
		n.setType("type");

		return n;
	}

	private SbiFunctionInputColumn createInputColumn(SbiCatalogFunction n) {
		SbiFunctionInputColumn inCol;
		inCol = new SbiFunctionInputColumn();
		inCol.setColType("type");
		inCol.getId().setColName(RandomStringUtils.randomAlphanumeric(10));
		inCol.getId().setFunction(n);
		return inCol;
	}

}
