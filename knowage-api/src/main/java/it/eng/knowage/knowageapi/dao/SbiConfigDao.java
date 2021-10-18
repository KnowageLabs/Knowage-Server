package it.eng.knowage.knowageapi.dao;

import java.util.List;

import it.eng.knowage.knowageapi.dao.dto.SbiConfig;

/**
 * @author Matteo Massarotto
 */
public interface SbiConfigDao {

	List<SbiConfig> findAll();

	SbiConfig findByLabel(String label);

}
