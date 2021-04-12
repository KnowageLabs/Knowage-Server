package it.eng.knowage.knowageapi;

import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.namespace.QName;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import it.eng.spagobi.services.security.SecurityService;
import it.eng.spagobi.services.security.SecurityServiceService;

@Configuration
@ComponentScan("it.eng.knowage.knowageapi")
public class KnowageApiConfiguration {

	@Bean
	@Qualifier("knowage-gallery")
	public EntityManagerFactory entityManagerFactory() {
		return Persistence.createEntityManagerFactory("knowage-gallery");
	}

	@Bean
	@Qualifier("knowage-gallery")
	public EntityManager entityManager(@Qualifier("knowage-gallery") EntityManagerFactory emf) {
		return emf.createEntityManager();
	}

	@Bean
	public SecurityServiceService securityService() throws NamingException, MalformedURLException {
		QName SERVICE_QNAME = new QName("http://security.services.spagobi.eng.it/", "SecurityService");
		URL serviceWsdlUrl = new URL("classpath:SecurityService.wsdl");
		SecurityService service = new SecurityService(serviceWsdlUrl, SERVICE_QNAME);
		return service.getSecurityServicePort();
	}

}
