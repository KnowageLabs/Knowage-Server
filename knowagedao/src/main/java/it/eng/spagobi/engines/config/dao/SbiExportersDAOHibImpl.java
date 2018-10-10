/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.engines.config.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.commons.dao.SpagoBIDAOObjectNotExistingException;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.engines.config.bo.Exporters;
import it.eng.spagobi.engines.config.metadata.SbiEngines;
import it.eng.spagobi.engines.config.metadata.SbiExporters;
import it.eng.spagobi.engines.config.metadata.SbiExportersId;
import it.eng.spagobi.utilities.assertion.Assert;

public class SbiExportersDAOHibImpl extends AbstractHibernateDAO implements ISbiExportersDAO {

	static private Logger logger = Logger.getLogger(SbiExportersDAOHibImpl.class);

	@Override
	public Exporters loadExporterById(Integer engineId, Integer domainId) {
		Exporters toReturn = null;
		Session session = null;
		Transaction transaction = null;

		try {
			if (engineId == null || domainId == null) {
				throw new IllegalArgumentException("Input parameter [id] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}

			SbiExporters hibExporter = (SbiExporters) session.load(SbiExporters.class, new SbiExportersId(engineId, domainId));
			logger.debug("Exporter loaded");
			toReturn = toExporter(hibExporter, session);

			logger.debug("Exporter loaded");

			transaction.rollback();

		} catch (ObjectNotFoundException e) {
			throw new SpagoBIDAOObjectNotExistingException("There is no Exporter with engine id " + engineId + "and domain id " + domainId);
		} catch (Exception e) {

			throw new SpagoBIDAOException(
					"An unexpected error occured while loading artifact with engine id [" + engineId + "] and domain id [" + domainId + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		return toReturn;
	}

	@Override
	public List<Exporters> loadAllSbiExporters() {
		List<Exporters> toReturn = new ArrayList<Exporters>();
		Session session = null;
		Transaction transaction = null;

		try {

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}

			List<SbiExporters> list = session.createCriteria(SbiExporters.class).list();

			List<SbiExporters> exporters = new ArrayList<SbiExporters>();

			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getSbiDomains().getDomainCd().equals("EXPORT_TYPE")) {
					SbiExporters exp = list.get(i);
					exporters.add(exp);
				}
			}

			Iterator it = exporters.iterator();
			while (it.hasNext()) {
				toReturn.add(toExporter((SbiExporters) it.next(), session));
			}
			logger.debug("Exporters loaded");

			transaction.rollback();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading exporters' list", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		return toReturn;

	}

	@Override
	public void modifyExporter(Exporters exporter, Integer engineId, Integer domainId) {
		Session session = null;
		Transaction transaction = null;

		try {
			if (exporter == null) {
				throw new IllegalArgumentException("Input parameter [exporter] cannot be null");
			}
			if (exporter.getEngineId() == null || exporter.getDomainId() == null) {
				throw new IllegalArgumentException("Input exporters id cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}

			// delete exporter
			SbiExporters hibExportertoDelete = (SbiExporters) session.load(SbiExporters.class, new SbiExportersId(engineId, domainId));
			logger.debug("Exporter loaded");
			if (hibExportertoDelete == null) {
				logger.warn("Exporter with id [" + engineId + "] not found");
			} else {
				session.delete(hibExportertoDelete);
			}

			SbiExporters hibExporterToInsert = toHibExporter(exporter);

			session.save(hibExporterToInsert);

			transaction.commit();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while saving exporter [" + exporter.getEngineId() + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		logger.debug("OUT");

	}

	@Override
	public void insertExporter(Exporters exporter) {
		Session session = null;
		Transaction transaction = null;

		try {
			if (exporter == null) {
				throw new IllegalArgumentException("Input parameter [exporter] cannot be null");
			}
			if (exporter.getEngineId() == null || exporter.getDomainId() == null) {
				throw new IllegalArgumentException("Input exporters id cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}

			SbiExporters hibExporter = toHibExporter(exporter);

			session.save(hibExporter);

			transaction.commit();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while saving exporter [" + exporter.getEngineId() + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		logger.debug("OUT");

	}

	@Override
	public void eraseExporter(Integer engineId, Integer domainId) {
		Session session = null;
		Transaction transaction = null;

		try {
			if (engineId == null || domainId == null) {
				throw new IllegalArgumentException("Input exporters id cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}

			SbiExporters hibExporter = (SbiExporters) session.load(SbiExporters.class, new SbiExportersId(engineId, domainId));
			logger.debug("Exporter loaded");
			if (hibExporter == null) {
				logger.warn("Artifact with id [" + engineId + "] not found");
			} else {
				session.delete(hibExporter);
			}

			transaction.commit();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while saving exporter [" + engineId + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		logger.debug("OUT");

	}

	private Exporters toExporter(SbiExporters hibExporter, Session session) {
		logger.debug("IN");
		Exporters toReturn = null;
		if (hibExporter != null) {
			toReturn = new Exporters();
			toReturn.setEngineId(hibExporter.getSbiEngines().getEngineId());
			toReturn.setDomainId(hibExporter.getSbiDomains().getValueId());
			toReturn.setDefaultValue(hibExporter.isDefaultValue());
			toReturn.setEngineLabel(hibExporter.getSbiEngines().getName());
			toReturn.setDomainLabel(hibExporter.getSbiDomains().getValueCd());
			toReturn.setPersisted(true);
			toReturn.setUpdateDomainId(hibExporter.getSbiDomains().getValueId());
			toReturn.setUpdateEngineId(hibExporter.getSbiEngines().getEngineId());

		}
		logger.debug("OUT");
		return toReturn;
	}

	private SbiExporters toHibExporter(Exporters exporter) {
		logger.debug("IN");
		SbiExporters toReturn = null;
		if (exporter != null) {
			toReturn = new SbiExporters();

			SbiDomains sbiDomain = new SbiDomains();
			sbiDomain.setValueId(exporter.getDomainId());

			SbiEngines sbiEngine = new SbiEngines();
			sbiEngine.setEngineId(exporter.getEngineId());

			SbiExportersId id = new SbiExportersId();
			id.setDomainId(exporter.getDomainId());
			id.setEngineId(exporter.getEngineId());

			toReturn.setSbiDomains(sbiDomain);
			toReturn.setSbiEngines(sbiEngine);
			toReturn.setId(id);

		}
		logger.debug("OUT");
		return toReturn;
	}

}
