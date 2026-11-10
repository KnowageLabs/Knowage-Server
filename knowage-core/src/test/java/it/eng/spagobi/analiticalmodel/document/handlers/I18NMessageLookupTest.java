package it.eng.spagobi.analiticalmodel.document.handlers;

import static org.junit.Assert.assertEquals;
import java.lang.reflect.Proxy;
import java.util.Locale;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Test;

import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.i18n.dao.I18NMessagesDAOHibImpl;
import it.eng.spagobi.i18n.metadata.SbiI18NMessages;

public class I18NMessageLookupTest {

	@Test
	public void looksUpMappedLanguageAndLabelProperties() throws Exception {
		Query domainQuery = query(new SbiDomains(162), "valueCd", "ITA");
		Query messageQuery = query(new SbiI18NMessages(1, 162, "i18nAzienda", "Azienda"),
				"languageCd", 162, "label", "i18nAzienda");
		Transaction transaction = (Transaction) Proxy.newProxyInstance(Transaction.class.getClassLoader(),
				new Class<?>[] { Transaction.class }, (proxy, method, args) -> null);
		Session session = (Session) Proxy.newProxyInstance(Session.class.getClassLoader(), new Class<?>[] { Session.class },
				(proxy, method, args) -> {
					if ("beginTransaction".equals(method.getName())) {
						return transaction;
					}
					if ("createQuery".equals(method.getName())) {
						if (((String) args[0]).startsWith("from SbiDomains")) {
							return domainQuery;
						}
						assertEquals("from SbiI18NMessages att where att.languageCd = :languageCd AND att.label = :label", args[0]);
						return messageQuery;
					}
					if ("isOpen".equals(method.getName())) {
						return false;
					}
					return null;
				});
		I18NMessagesDAOHibImpl dao = new I18NMessagesDAOHibImpl() {
			@Override
			public Session getSession() {
				return session;
			}
		};
		assertEquals("Azienda", dao.getI18NMessages(Locale.ITALY, "i18nAzienda"));
		assertEquals("i18nAzienda", dao.getI18NMessages(null, "i18nAzienda"));
	}

	private Query query(Object result, Object... parameters) {
		return (Query) Proxy.newProxyInstance(Query.class.getClassLoader(), new Class<?>[] { Query.class },
				(proxy, method, args) -> {
					if ("uniqueResult".equals(method.getName())) {
						return result;
					}
					if ("setString".equals(method.getName()) || "setInteger".equals(method.getName())) {
						for (int i = 0; i < parameters.length; i += 2) {
							if (parameters[i].equals(args[0])) {
								assertEquals(parameters[i + 1], args[1]);
								return proxy;
							}
						}
						throw new AssertionError("Unexpected parameter: " + args[0]);
					}
					return null;
				});
	}
}
