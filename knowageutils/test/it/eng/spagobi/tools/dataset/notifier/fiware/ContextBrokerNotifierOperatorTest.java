package it.eng.spagobi.tools.dataset.notifier.fiware;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tools.dataset.bo.RESTDataSet;
import it.eng.spagobi.tools.dataset.bo.RESTDataSetTest;
import it.eng.spagobi.tools.dataset.common.dataproxy.IDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReaderTest;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.listener.DataSetListenerException;
import it.eng.spagobi.tools.dataset.listener.DataSetListenerManager;
import it.eng.spagobi.tools.dataset.listener.DataSetListenerManagerFactory;
import it.eng.spagobi.tools.dataset.listener.DataStoreChangedEvent;
import it.eng.spagobi.tools.dataset.listener.IDataSetListener;
import it.eng.spagobi.utilities.HelperForTest;
import it.eng.spagobi.utilities.engines.AbstractEngineAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import junit.framework.TestCase;

public class ContextBrokerNotifierOperatorTest extends TestCase {

	private boolean done;

	@SuppressWarnings("serial")
	public void testNotifySourceBeanSourceBeanAbstractEngineAction() throws IOException {


		DataSetListenerManager manager = DataSetListenerManagerFactory.getManager();
		RESTDataSet dataset = RESTDataSetTest.getRestDataSet();
		// load data to set the datastore for the first time
		String jsonData = JSONPathDataReaderTest.getJSONData();
		dataset.setDataProxy(new DataProxymock(jsonData));
		dataset.setIgnoreConfigurationOnLoad(true);
		dataset.loadData();

		manager.changedDataSet("user1", "label1", dataset);
		manager.addIDataSetListenerIfAbsent("user1", "label1", new IDataSetListener() {

			public void dataStoreChanged(DataStoreChangedEvent event) throws DataSetListenerException {
				done = true;

				List<IRecord> deleted = event.getDeleted();
				assertEquals(0, deleted.size());
				List<IRecord> updated = event.getUpdated();
				assertEquals(2, updated.size());
				assertContains(updated, "pros6_Meter");
				IRecord rec1 = assertContains(updated, "pros5_Meter");
				IRecord rec2 = assertContains(updated, 1.8);
				assertTrue(rec1 == rec2);
				List<IRecord> added = event.getAdded();
				assertEquals(0, added.size());
			}

		}, "1");

		JSONPathDataReader reader = (JSONPathDataReader) dataset.getDataReader();
		ContextBrokerNotifierOperator operator = new ContextBrokerNotifierOperator("55dae93ff23205eb4241ccd0", "user1", "label1", manager, reader);
		String body = HelperForTest.readFile("notification.json", getClass());
		operator.notify(new HttpServletRequestMock(body), null,body);
		assertTrue(done);
	}

	private IRecord assertContains(List<IRecord> added, Object id) {
		for (IRecord rec : added) {
			for (IField field : rec.getFields()) {
				if (field.getValue().equals(id)) {
					return rec;
				}
			}
		}
		fail();
		return null;
	}

	private static class DataProxymock implements IDataProxy {

		private final String data;

		public DataProxymock(String data) {
			this.data = data;
		}

		public IDataStore load(IDataReader dataReader) {
			try {
				return dataReader.read(data);
			} catch (EMFUserError e) {
				throw new RuntimeException(e);
			} catch (EMFInternalError e) {
				throw new RuntimeException(e);
			}
		}

		public String getStatement() {

			return null;
		}

		public void setStatement(String statement) {

		}

		public String getResPath() {

			return null;
		}

		public void setResPath(String resPath) {

		}

		public boolean isPaginationSupported() {

			return false;
		}

		public boolean isOffsetSupported() {

			return false;
		}

		public int getOffset() {

			return 0;
		}

		public void setOffset(int offset) {

		}

		public boolean isFetchSizeSupported() {

			return false;
		}

		public int getFetchSize() {

			return 0;
		}

		public void setFetchSize(int fetchSize) {

		}

		public boolean isMaxResultsSupported() {

			return false;
		}

		public int getMaxResults() {

			return 0;
		}

		public void setMaxResults(int maxResults) {

		}

		public boolean isCalculateResultNumberOnLoadEnabled() {

			return false;
		}

		public void setCalculateResultNumberOnLoad(boolean enabled) {

		}

		public long getResultNumber() {

			return 0;
		}

		@SuppressWarnings("rawtypes")
		public Map getParameters() {

			return null;
		}

		@SuppressWarnings("rawtypes")
		public void setParameters(Map parameters) {

		}

		@SuppressWarnings("rawtypes")
		public Map getProfile() {

			return null;
		}

		@SuppressWarnings("rawtypes")
		public void setProfile(Map profile) {

		}

	}

	private static class HttpServletRequestMock implements HttpServletRequest {

		private final String body;

		public HttpServletRequestMock(String body) {
			this.body = body;
		}

		public AsyncContext getAsyncContext() {

			return null;
		}

		public Object getAttribute(String arg0) {

			return null;
		}

		public Enumeration<String> getAttributeNames() {

			return null;
		}

		public String getCharacterEncoding() {

			return null;
		}

		public int getContentLength() {

			return 0;
		}

		public String getContentType() {

			return null;
		}

		public DispatcherType getDispatcherType() {

			return null;
		}

		public ServletInputStream getInputStream() throws IOException {

			return null;
		}

		public String getLocalAddr() {

			return null;
		}

		public String getLocalName() {

			return null;
		}

		public int getLocalPort() {

			return 0;
		}

		public Locale getLocale() {

			return null;
		}

		public Enumeration<Locale> getLocales() {

			return null;
		}

		public String getParameter(String arg0) {

			return null;
		}

		public Map<String, String[]> getParameterMap() {

			return null;
		}

		public Enumeration<String> getParameterNames() {

			return null;
		}

		public String[] getParameterValues(String arg0) {

			return null;
		}

		public String getProtocol() {

			return null;
		}

		public BufferedReader getReader() throws IOException {
			return new BufferedReader(new StringReader(body));
		}

		public String getRealPath(String arg0) {

			return null;
		}

		public String getRemoteAddr() {

			return null;
		}

		public String getRemoteHost() {

			return null;
		}

		public int getRemotePort() {

			return 0;
		}

		public RequestDispatcher getRequestDispatcher(String arg0) {

			return null;
		}

		public String getScheme() {

			return null;
		}

		public String getServerName() {

			return null;
		}

		public int getServerPort() {

			return 0;
		}

		public ServletContext getServletContext() {

			return null;
		}

		public boolean isAsyncStarted() {

			return false;
		}

		public boolean isAsyncSupported() {

			return false;
		}

		public boolean isSecure() {

			return false;
		}

		public void removeAttribute(String arg0) {

		}

		public void setAttribute(String arg0, Object arg1) {

		}

		public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {

		}

		public AsyncContext startAsync() {

			return null;
		}

		public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) {

			return null;
		}

		public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException {

			return false;
		}

		public String getAuthType() {

			return null;
		}

		public String getContextPath() {

			return null;
		}

		public Cookie[] getCookies() {

			return null;
		}

		public long getDateHeader(String arg0) {

			return 0;
		}

		public String getHeader(String arg0) {

			return null;
		}

		public Enumeration<String> getHeaderNames() {

			return null;
		}

		public Enumeration<String> getHeaders(String arg0) {

			return null;
		}

		public int getIntHeader(String arg0) {

			return 0;
		}

		public String getMethod() {

			return null;
		}

		public Part getPart(String arg0) throws IOException, IllegalStateException, ServletException {

			return null;
		}

		public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {

			return null;
		}

		public String getPathInfo() {

			return null;
		}

		public String getPathTranslated() {

			return null;
		}

		public String getQueryString() {

			return null;
		}

		public String getRemoteUser() {

			return null;
		}

		public String getRequestURI() {

			return null;
		}

		public StringBuffer getRequestURL() {

			return null;
		}

		public String getRequestedSessionId() {

			return null;
		}

		public String getServletPath() {

			return null;
		}

		public HttpSession getSession() {

			return null;
		}

		public HttpSession getSession(boolean arg0) {

			return null;
		}

		public Principal getUserPrincipal() {

			return null;
		}

		public boolean isRequestedSessionIdFromCookie() {

			return false;
		}

		public boolean isRequestedSessionIdFromURL() {

			return false;
		}

		public boolean isRequestedSessionIdFromUrl() {

			return false;
		}

		public boolean isRequestedSessionIdValid() {

			return false;
		}

		public boolean isUserInRole(String arg0) {

			return false;
		}

		public void login(String arg0, String arg1) throws ServletException {

		}

		public void logout() throws ServletException {

		}

	}

}
