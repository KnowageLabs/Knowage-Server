/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2023 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.sdk.utilities;

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPMessage;

/**
 * Replaces MessageContext in Axis 1.4.
 */
public class KnowageSoapMessageContext {

	private static final ThreadLocal<Context> CONTEXT_HOLDER = ThreadLocal.withInitial(Context::new);

	public static Context getCurrentContext() {
		return CONTEXT_HOLDER.get();
	}

	public static void removeCurrentContext() {
		CONTEXT_HOLDER.remove();
	}

	public static class Context implements SOAPMessageContext {
		private final ConcurrentHashMap<String, Object> properties = new ConcurrentHashMap<>();
		private SOAPMessage message;

		@Override
		public void setProperty(String name, Object value) {
			properties.put(name, value);
		}

		@Override
		public Object getProperty(String name) {
			return properties.get(name);
		}

		@Override
		public void removeProperty(String name) {
			properties.remove(name);
		}

		@Override
		public boolean containsProperty(String name) {
			return properties.containsKey(name);
		}

		@Override
		public Iterator<String> getPropertyNames() {
			return Collections.list(properties.keys()).iterator();
		}

		@Override
		public SOAPMessage getMessage() {
			synchronized (this) {
				return message;
			}
		}

		@Override
		public void setMessage(SOAPMessage message) {
			synchronized (this) {
				this.message = message;
			}
		}

		@Override
		public String[] getRoles() {
			return new String[0];
		}
	}

	private KnowageSoapMessageContext() {
	}

}
