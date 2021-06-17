package it.eng.spagobi.functions.dao;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.functions.metadata.IInputVariable;
import it.eng.spagobi.functions.metadata.IOutputColumn;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.utilities.CatalogFunction;

public interface ICatalogFunctionDAO extends ISpagoBIDao {

	public UUID insertCatalogFunction(CatalogFunction catalogFunction, Map<String, String> inputColumns, Map<String, ? extends IInputVariable> inputVariables,
			Map<String, ? extends IOutputColumn> outputColumns) throws EMFUserError;

	public List<SbiCatalogFunction> loadAllCatalogFunctions();

	public UUID updateCatalogFunction(CatalogFunction updatedCatalogFunction, UUID uuid);

	public void deleteCatalogFunction(UUID uuid);

	public SbiCatalogFunction getCatalogFunctionByUuid(UUID uuid);

	public SbiCatalogFunction getCatalogFunctionByLabel(String label);

	/**
	 * @param id
	 * @return List<SbiCatalogFunction>
	 */
	public List<SbiCatalogFunction> loadAllCatalogFunctionsByBiobjId(Integer id);

}