package it.eng.spagobi.tools.dataset.dao;

import java.io.File;

import it.eng.spagobi.commons.dao.DAOConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.news.dao.ISbiNewsDAO;
import it.eng.spagobi.tools.news.metadata.SbiNews;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class TestClass extends AbstractDAOTest {

	ISbiNewsDAO sbiNewsDAO = null;
	SbiNews sbiNews = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		DAOConfig.setHibernateConfigurationFileFile(new File("C:/Users/user2/Desktop/hibernate.cfg.xml"));

		try {

			sbiNewsDAO = DAOFactory.getSbiNewsDAO();

		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An error occured while instantiating DAO!!!");
		}
	}

	public void testMethod() {
		try {
			setUp();
			sbiNews = new SbiNews("Vernassa");
			// sbiNewsDAO.deleteNew(9);
			sbiNewsDAO.createOrUpdate(sbiNews);
////
//			List<SbiNews> allNews = new ArrayList<>();
//			allNews = sbiNewsDAO.getAllNews();
//
//			for (SbiNews s : allNews)
//				System.out.println("Name: " + s.getName());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
