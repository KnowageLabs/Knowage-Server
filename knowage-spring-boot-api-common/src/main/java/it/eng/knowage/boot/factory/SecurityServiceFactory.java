/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.knowage.boot.factory;

import java.net.URL;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import it.eng.spagobi.services.security.SecurityService;
import it.eng.spagobi.services.security.SecurityServiceService;

public class SecurityServiceFactory extends AbstractFactoryBean<SecurityServiceService> {

	@Override
	public Class<?> getObjectType() {
		return SecurityServiceService.class;
	}

	@Override
	protected SecurityServiceService createInstance() throws Exception {
		Context ctx = new InitialContext();
		String serviceUrl = (String) ctx.lookup("java:comp/env/service_url");
		QName SERVICE_QNAME = new QName("http://security.services.spagobi.eng.it/", "SecurityService");
		URL serviceWsdlUrl = new URL(serviceUrl + "/services/SecurityService?wsdl");
		SecurityService service = new SecurityService(serviceWsdlUrl, SERVICE_QNAME);

		return service.getSecurityServicePort();
	}

}
