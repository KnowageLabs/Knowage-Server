package it.eng.knowage.knowageapi;

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
		String serviceUrl = (String) ctx.lookup("java:/comp/env/service_url");
		QName SERVICE_QNAME = new QName("http://security.services.spagobi.eng.it/", "SecurityService");
		URL serviceWsdlUrl = new URL(serviceUrl + "/services/SecurityService?wsdl");
		SecurityService service = new SecurityService(serviceWsdlUrl, SERVICE_QNAME);

		return service.getSecurityServicePort();
	}

}
