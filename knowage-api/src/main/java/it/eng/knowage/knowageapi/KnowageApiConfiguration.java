package it.eng.knowage.knowageapi;

import java.net.MalformedURLException;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

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

	@Lazy
	@Bean
	public SecurityServiceFactory securityService() throws NamingException, MalformedURLException {
		return new SecurityServiceFactory();
	}

}
