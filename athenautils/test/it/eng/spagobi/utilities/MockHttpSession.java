package it.eng.spagobi.utilities;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

@SuppressWarnings("deprecation")
public class MockHttpSession implements HttpSession {

	private final Map<String, Object> attributes = new HashMap<String, Object>();

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return null;
	}

	@Override
	public long getCreationTime() {

		return 0;
	}

	@Override
	public String getId() {

		return null;
	}

	@Override
	public long getLastAccessedTime() {

		return 0;
	}

	@Override
	public int getMaxInactiveInterval() {

		return 0;
	}

	@Override
	public ServletContext getServletContext() {

		return null;
	}

	@Override
	public HttpSessionContext getSessionContext() {

		return null;
	}

	@Override
	public Object getValue(String name) {

		return null;
	}

	@Override
	public String[] getValueNames() {

		return null;
	}

	@Override
	public void invalidate() {

	}

	@Override
	public boolean isNew() {

		return false;
	}

	@Override
	public void putValue(String name, Object value) {

	}

	@Override
	public void removeAttribute(String name) {

	}

	@Override
	public void removeValue(String name) {

	}

	@Override
	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	@Override
	public void setMaxInactiveInterval(int interval) {

	}

}