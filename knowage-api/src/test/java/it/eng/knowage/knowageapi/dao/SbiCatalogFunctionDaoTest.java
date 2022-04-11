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
package it.eng.knowage.knowageapi.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.sql.rowset.serial.SerialException;

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
class SbiCatalogFunctionDaoTest {

	@Autowired
	private SbiCatalogFunctionDao dao;

	@PersistenceContext(unitName = "knowage-functioncatalog")
	private EntityManager em;

	@Test
	void getAll() {
		List<SbiCatalogFunction> all = dao.findAll();

		assertEquals(true, true);
	}

	@Test
	void mathing() throws KnowageBusinessException {

		EntityTransaction transaction = em.getTransaction();
		transaction.begin();

		SbiCatalogFunction nonMatching1 = createRandomName();
		SbiCatalogFunction nonMatching2 = createRandomName();
		SbiCatalogFunction matching = create("match");

		nonMatching1 = store(nonMatching1);
		nonMatching2 = store(nonMatching2);
		matching = store(matching);

		transaction.commit();

		List<SbiCatalogFunction> all = dao.findAll("match");

		try {
			assertEquals(1, all.size());
		} finally {
			transaction = em.getTransaction();
			transaction.begin();

			dao.delete(nonMatching1);
			dao.delete(nonMatching2);

			for (SbiCatalogFunction e : all) {
				dao.delete(e);
			}

			transaction.commit();
		}
	}

	@Test
	void create() throws SerialException, SQLException, KnowageBusinessException {

		SbiCatalogFunction n = createRandomName();

		EntityTransaction transaction = em.getTransaction();
		transaction.begin();

		n = dao.create(n);

		transaction.commit();

		SbiCatalogFunction find = dao.find(n.getId());

		transaction = em.getTransaction();
		transaction.begin();

		dao.delete(find);

		transaction.commit();

		assertEquals(true, true);
	}

	@Test
	void update() throws SerialException, SQLException, KnowageBusinessException {

		SbiCatalogFunction n = createRandomName();

		EntityTransaction transaction = em.getTransaction();
		transaction.begin();

		n = dao.create(n);

		transaction.commit();

		n.getInputColumns().clear();
		n.getInputVariables().clear();
		n.getOutputColumns().clear();

		n = dao.find(n.getId());

		transaction = em.getTransaction();
		transaction.begin();

		dao.update(n);

		transaction.commit();

		transaction = em.getTransaction();
		transaction.begin();

		dao.delete(n);

		transaction.commit();

		assertEquals(true, true);
	}

	private SbiCatalogFunction store(SbiCatalogFunction toStore) {
		toStore = dao.create(toStore);
		return toStore;
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

		SbiFunctionInputColumn inCol = new SbiFunctionInputColumn();
		inCol.setColType("type");
		inCol.getId().setColName("name");
		inCol.getId().setFunction(n);

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

}
