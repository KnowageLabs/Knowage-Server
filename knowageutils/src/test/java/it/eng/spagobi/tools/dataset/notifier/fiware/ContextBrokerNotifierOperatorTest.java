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
package it.eng.spagobi.tools.dataset.notifier.fiware;

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
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.HelperForTest;
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
		dataset.loadData();

		manager.changedDataSet("user1", "label1", dataset);
		manager.addIDataSetListenerIfAbsent("user1", "label1", new IDataSetListener() {

			@Override
			public void dataStoreChanged(DataStoreChangedEvent event) throws DataSetListenerException {
				done = true;
			}

		}, "1");

		JSONPathDataReader reader = dataset.getDataReader();
		ContextBrokerNotifierOperator operator = new ContextBrokerNotifierOperator("55dae93ff23205eb4241ccd0", UserProfileManager.getProfile(), "label1",
				dataset.getSignature(),
				dataset.isRealtimeNgsiConsumer(), manager, reader);
		String body = HelperForTest.readFile("notification.json", getClass());
		operator.notify(new HttpServletRequestMock(body), null, body);
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

		@Override
		public IDataStore load(IDataReader dataReader) {
			try {
				return dataReader.read(data);
			} catch (EMFUserError e) {
				throw new RuntimeException(e);
			} catch (EMFInternalError e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getStatement() {

			return null;
		}

		@Override
		public void setStatement(String statement) {

		}

		@Override
		public String getResPath() {

			return null;
		}

		@Override
		public void setResPath(String resPath) {

		}

		@Override
		public boolean isPaginationSupported() {

			return false;
		}

		@Override
		public boolean isOffsetSupported() {

			return false;
		}

		@Override
		public int getOffset() {

			return 0;
		}

		@Override
		public void setOffset(int offset) {

		}

		@Override
		public boolean isFetchSizeSupported() {

			return false;
		}

		@Override
		public int getFetchSize() {

			return 0;
		}

		@Override
		public void setFetchSize(int fetchSize) {

		}

		@Override
		public boolean isMaxResultsSupported() {

			return false;
		}

		@Override
		public int getMaxResults() {

			return 0;
		}

		@Override
		public void setMaxResults(int maxResults) {

		}

		@Override
		public boolean isCalculateResultNumberOnLoadEnabled() {

			return false;
		}

		@Override
		public void setCalculateResultNumberOnLoad(boolean enabled) {

		}

		@Override
		public long getResultNumber() {

			return 0;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Map<String, String> getParameters() {

			return null;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public void setParameters(Map parameters) {

		}

		@Override
		@SuppressWarnings("rawtypes")
		public Map getProfile() {

			return null;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public void setProfile(Map profile) {

		}

		@Override
		public Object getData(IDataReader dataReader, Object... resources) {
			return null;
		}

	}

	private static class HttpServletRequestMock implements HttpServletRequest {

		private final String body;

		public HttpServletRequestMock(String body) {
			this.body = body;
		}

		@Override
		public AsyncContext getAsyncContext() {

			return null;
		}

		@Override
		public Object getAttribute(String arg0) {

			return null;
		}

		@Override
		public Enumeration<String> getAttributeNames() {

			return null;
		}

		@Override
		public String getCharacterEncoding() {

			return null;
		}

		@Override
		public int getContentLength() {

			return 0;
		}

		@Override
		public String getContentType() {

			return null;
		}

		@Override
		public DispatcherType getDispatcherType() {

			return null;
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {

			return null;
		}

		@Override
		public String getLocalAddr() {

			return null;
		}

		@Override
		public String getLocalName() {

			return null;
		}

		@Override
		public int getLocalPort() {

			return 0;
		}

		@Override
		public Locale getLocale() {

			return null;
		}

		@Override
		public Enumeration<Locale> getLocales() {

			return null;
		}

		@Override
		public String getParameter(String arg0) {

			return null;
		}

		@Override
		public Map<String, String[]> getParameterMap() {

			return null;
		}

		@Override
		public Enumeration<String> getParameterNames() {

			return null;
		}

		@Override
		public String[] getParameterValues(String arg0) {

			return null;
		}

		@Override
		public String getProtocol() {

			return null;
		}

		@Override
		public BufferedReader getReader() throws IOException {
			return new BufferedReader(new StringReader(body));
		}

		@Override
		public String getRealPath(String arg0) {

			return null;
		}

		@Override
		public String getRemoteAddr() {

			return null;
		}

		@Override
		public String getRemoteHost() {

			return null;
		}

		@Override
		public int getRemotePort() {

			return 0;
		}

		@Override
		public RequestDispatcher getRequestDispatcher(String arg0) {

			return null;
		}

		@Override
		public String getScheme() {

			return null;
		}

		@Override
		public String getServerName() {

			return null;
		}

		@Override
		public int getServerPort() {

			return 0;
		}

		@Override
		public ServletContext getServletContext() {

			return null;
		}

		@Override
		public boolean isAsyncStarted() {

			return false;
		}

		@Override
		public boolean isAsyncSupported() {

			return false;
		}

		@Override
		public boolean isSecure() {

			return false;
		}

		@Override
		public void removeAttribute(String arg0) {

		}

		@Override
		public void setAttribute(String arg0, Object arg1) {

		}

		@Override
		public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {

		}

		@Override
		public AsyncContext startAsync() {

			return null;
		}

		@Override
		public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) {

			return null;
		}

		@Override
		public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException {

			return false;
		}

		@Override
		public String getAuthType() {

			return null;
		}

		@Override
		public String getContextPath() {

			return null;
		}

		@Override
		public Cookie[] getCookies() {

			return null;
		}

		@Override
		public long getDateHeader(String arg0) {

			return 0;
		}

		@Override
		public String getHeader(String arg0) {

			return null;
		}

		@Override
		public Enumeration<String> getHeaderNames() {

			return null;
		}

		@Override
		public Enumeration<String> getHeaders(String arg0) {

			return null;
		}

		@Override
		public int getIntHeader(String arg0) {

			return 0;
		}

		@Override
		public String getMethod() {

			return null;
		}

		@Override
		public Part getPart(String arg0) throws IOException, IllegalStateException, ServletException {

			return null;
		}

		@Override
		public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {

			return null;
		}

		@Override
		public String getPathInfo() {

			return null;
		}

		@Override
		public String getPathTranslated() {

			return null;
		}

		@Override
		public String getQueryString() {

			return null;
		}

		@Override
		public String getRemoteUser() {

			return null;
		}

		@Override
		public String getRequestURI() {

			return null;
		}

		@Override
		public StringBuffer getRequestURL() {

			return null;
		}

		@Override
		public String getRequestedSessionId() {

			return null;
		}

		@Override
		public String getServletPath() {

			return null;
		}

		@Override
		public HttpSession getSession() {

			return null;
		}

		@Override
		public HttpSession getSession(boolean arg0) {

			return null;
		}

		@Override
		public Principal getUserPrincipal() {

			return null;
		}

		@Override
		public boolean isRequestedSessionIdFromCookie() {

			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromURL() {

			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromUrl() {

			return false;
		}

		@Override
		public boolean isRequestedSessionIdValid() {

			return false;
		}

		@Override
		public boolean isUserInRole(String arg0) {

			return false;
		}

		@Override
		public void login(String arg0, String arg1) throws ServletException {

		}

		@Override
		public void logout() throws ServletException {

		}

	}

}
