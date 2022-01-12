package it.eng.knowage.boot.dao;

import java.util.List;

import it.eng.knowage.boot.dao.dto.SbiConfig;

/**
 * @author Matteo Massarotto
 */
public interface SbiConfigDao {

	List<SbiConfig> findAll();

	SbiConfig findByLabel(String label);

}
