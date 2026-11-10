package it.eng.spagobi.analiticalmodel.document.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Proxy;
import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.i18n.dao.I18NMessagesDAO;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class DriverLabelLocalizationTest {

	@Test
	public void localizesDriverLabelsAndPreservesFallbacks() {
		final int[] lookups = { 0 };
		I18NMessagesDAO messages = (I18NMessagesDAO) Proxy.newProxyInstance(I18NMessagesDAO.class.getClassLoader(),
				new Class<?>[] { I18NMessagesDAO.class }, (proxy, method, args) -> {
					lookups[0]++;
					String code = (String) args[1];
					if ("i18nError".equals(code)) {
						throw new IllegalStateException("lookup failed");
					}
					if ("i18nEmpty".equals(code)) {
						return "";
					}
					if ("i18nAzienda".equals(code)) {
						return Locale.ITALIAN.equals(args[0]) ? "Azienda" : "Company";
					}
					return null;
				});
		IParameterUseDAO uses = (IParameterUseDAO) Proxy.newProxyInstance(IParameterUseDAO.class.getClassLoader(),
				new Class<?>[] { IParameterUseDAO.class }, (proxy, method, args) -> new ParameterUse());
		new MockUp<DAOFactory>() {
			@Mock
			public I18NMessagesDAO getI18NMessageDAO() {
				return messages;
			}

			@Mock
			public IParameterUseDAO getParameterUseDAO() {
				return uses;
			}

			@Mock
			public IParameterDAO getParameterDAO() {
				return null;
			}
		};

		AbstractDriverRuntime<BIObjectParameter> runtime = new AbstractDriverRuntime<BIObjectParameter>() {
			@Override
			public void initVisualDependencies(BIObjectParameter driver) {
			}

			@Override
			public void initDataDependencies(BIObjectParameter driver) {
			}
		};
		runtime.initDAO();
		Parameter parameter = new Parameter();
		parameter.setModalityValue(new ModalitiesValue());
		BIObjectParameter driver = new BIObjectParameter();
		driver.setParameter(parameter);
		driver.setRequired(0);
		driver.setVisible(1);

		runtime.locale = Locale.ITALIAN;
		driver.setLabel("i18nAzienda");
		runtime.initAttributes(driver);
		assertEquals("Azienda", runtime.label);
		runtime.locale = Locale.ENGLISH;
		runtime.initAttributes(driver);
		assertEquals("Company", runtime.label);

		for (String label : new String[] { "i18nMissing", "i18nEmpty", "i18nError" }) {
			driver.setLabel(label);
			runtime.initAttributes(driver);
			assertEquals(label, runtime.label);
		}
		driver.setLabel("Company");
		runtime.initAttributes(driver);
		assertEquals("Company", runtime.label);
		driver.setLabel(null);
		runtime.initAttributes(driver);
		assertNull(runtime.label);
		assertEquals(5, lookups[0]);
	}
}
