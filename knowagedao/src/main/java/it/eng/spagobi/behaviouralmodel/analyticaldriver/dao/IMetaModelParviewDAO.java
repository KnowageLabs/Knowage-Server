package it.eng.spagobi.behaviouralmodel.analyticaldriver.dao;

import java.util.List;

import org.hibernate.HibernateException;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.MetaModelParview;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

public interface IMetaModelParviewDAO extends ISpagoBIDao {

	public List loadMetaModelParviewsByMetaModelParameterId(Integer metaModelId);

	public void modifyMetaModelParview(MetaModelParview metaModelParview);

	public Integer insertMetaModelParview(MetaModelParview metaModelParview) throws HibernateException;

	public void eraseMetaModelParview(Integer parviewId) throws HibernateException;

	public List loadMetaModelParviewByID(Integer parviewId);

	public List loadMetaModelParviews(Integer metaModelParId);

	public List loadMetaModelParviewsFather(Integer metaModelParId);
}
