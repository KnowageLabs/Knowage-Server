package it.eng.knowage.boot.dao;

import java.util.List;

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.dao.dto.SbiConfig;

/**
 * Dao for the <code>SBI_CONFIG</code> table.
 *
 * All the implementations of this class must be independent from {@link BusinessRequestContext} and every other
 * references to a user or a request.
 *
 * @author Matteo Massarotto
 */
public interface SbiConfigDao {

	List<SbiConfig> findAll();

	SbiConfig findByLabel(String label);

}
