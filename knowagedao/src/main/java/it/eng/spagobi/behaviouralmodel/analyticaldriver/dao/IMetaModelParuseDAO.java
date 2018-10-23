package it.eng.spagobi.behaviouralmodel.analyticaldriver.dao;

import java.util.List;

import org.hibernate.HibernateException;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.MetaModelParuse;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

public interface IMetaModelParuseDAO extends ISpagoBIDao {

	public List loadMetaModelParuseById(Integer metaModelParuseId);

	public void modifyMetaModelParuse(MetaModelParuse aMetaModelParuse) throws HibernateException;

	public void insertMetaModelParuse(MetaModelParuse aMetaModelParuse) throws HibernateException;

	public void eraseMetaModelParuse(MetaModelParuse aMetaModelParuse) throws HibernateException;

	public List loadAllParuses(Integer metaModelParId);

	public List loadMetaModelParusesFather(Integer metaModelParId) throws HibernateException;

	public List loadMetaModelParuse(Integer metaModelParId, Integer paruseId) throws HibernateException;
}
