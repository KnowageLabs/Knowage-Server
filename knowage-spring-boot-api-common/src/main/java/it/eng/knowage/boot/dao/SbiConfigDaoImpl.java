package it.eng.knowage.boot.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.dao.dto.SbiConfig;

/**
 * @author Matteo Massarotto
 */
@Component
public class SbiConfigDaoImpl implements SbiConfigDao {

	@Autowired
	private BusinessRequestContext businessRequestContext;

	@PersistenceContext(unitName = "knowage-config")
	private EntityManager em;

	@Override
	public List<SbiConfig> findAll() {
		Query query = em.createQuery("SELECT e FROM SbiConfig e");
		List<SbiConfig> resultList = query.getResultList();
		return resultList;
	}

	@Override
	public SbiConfig findByLabel(String label) {
		SbiConfig result = null;
		List<SbiConfig> resultList = em.createQuery("SELECT t FROM SbiConfig t where t.label = :value1", SbiConfig.class).setParameter("value1", label)
				.getResultList();
		if (resultList.size() == 1)
			result = resultList.get(0);
		return result;
	}

}
