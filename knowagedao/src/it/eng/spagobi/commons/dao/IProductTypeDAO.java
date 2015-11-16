package it.eng.spagobi.commons.dao;

import it.eng.spagobi.commons.metadata.SbiProductType;
import it.eng.spagobi.commons.metadata.SbiProductTypeEngine;

import java.util.List;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public interface IProductTypeDAO extends ISpagoBIDao {

	public List<SbiProductType> loadAllProductType();

	public List<SbiProductTypeEngine> loadSelectedEngines(String productType);

}
