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
package it.eng.knowage.knowageapi;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import it.eng.knowage.knowageapi.context.BusinessRequestContext;
import it.eng.knowage.knowageapi.service.FunctionCatalogAPI;
import it.eng.knowage.knowageapi.service.impl.FunctionCatalogAPIImplTest;

@Configuration
@Profile("test")
@ComponentScan("it.eng.knowage.knowageapi")
public class KnowageApiConfigurationTest {

	@Bean
	@Qualifier("knowage-gallery")
	public EntityManagerFactory entityManagerFactoryForWidgetGallery() {
		Map<String, Object> properties = getEntityManagerFactoriesProperties();
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("knowage-gallery", properties);
		return emf;
	}

	@Bean
	@Qualifier("knowage-gallery")
	public EntityManager entityManagerForWidgetGallery(@Qualifier("knowage-gallery") EntityManagerFactory emf) {
		return emf.createEntityManager();
	}

	@Bean
	@Qualifier("knowage-functioncatalog")
	public EntityManagerFactory entityManagerFactoryForWidgetFunctionCatalog() {
		Map<String, Object> properties = getEntityManagerFactoriesProperties();
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("knowage-functioncatalog", properties);
		return emf;
	}

	@Bean
	@Qualifier("knowage-functioncatalog")
	public EntityManager entityManagerForWidgetFunctionCatalog(@Qualifier("knowage-functioncatalog") EntityManagerFactory emf) {
		return emf.createEntityManager();
	}

	private Map<String, Object> getEntityManagerFactoriesProperties() {
		Map<String, Object> properties = new HashMap<>();

		properties.put("javax.persistence.jdbc.url", "jdbc:mariadb://localhost:3310/knowage_master");
		properties.put("javax.persistence.jdbc.driver", "org.mariadb.jdbc.Driver");
		properties.put("javax.persistence.jdbc.user", "root");
		properties.put("javax.persistence.jdbc.password", "root");
		return properties;
	}

	@Bean
	public BusinessRequestContext businessRequestContext(@Value("${application.version}") String version) {
		BusinessRequestContext businessRequestContext = new BusinessRequestContext(version);
		businessRequestContext.setUsername("biadmin");
		businessRequestContext.setOrganization("DEFAULT_TENANT");
		return businessRequestContext;
	}

	@Bean
	public FunctionCatalogAPI functionCatalogAPI() {
		return new FunctionCatalogAPIImplTest();
	}

	@Lazy
	@Bean
	public SecurityServiceFactory securityService() throws NamingException, MalformedURLException {
		return new SecurityServiceFactoryTest();
	}

	@Bean(name = "multipartResolver")
	public CommonsMultipartResolver multipartResolver() {
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
		multipartResolver.setMaxUploadSize(100000);
		return multipartResolver;
	}

}
