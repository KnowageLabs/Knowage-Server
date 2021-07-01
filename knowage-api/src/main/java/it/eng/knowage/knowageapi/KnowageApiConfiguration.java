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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import it.eng.knowage.knowageapi.context.BusinessRequestContext;
import it.eng.knowage.knowageapi.service.FunctionCatalogAPI;
import it.eng.knowage.knowageapi.service.impl.FunctionCatalogAPIImpl;

@Configuration
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
@Profile("production")
@ComponentScan({ "it.eng.knowage.knowageapi", "it.eng.knowage.resourcemanager" })
public class KnowageApiConfiguration {

	@Bean
	@Qualifier("knowage-gallery")
	public LocalEntityManagerFactoryBean entityManagerFactoryForWidgetGallery() {
		LocalEntityManagerFactoryBean factoryBean = new LocalEntityManagerFactoryBean();
		factoryBean.setPersistenceUnitName("knowage-gallery");
		return factoryBean;
	}

	@Bean
	@Qualifier("knowage-gallery")
	public EntityManager entityManagerForWidgetGallery(@Qualifier("knowage-gallery") EntityManagerFactory emf) {
		EntityManager em = emf.createEntityManager();
		em.setFlushMode(FlushModeType.COMMIT);
		return em;
	}

	@Primary
	@Bean
	@Qualifier("knowage-functioncatalog")
	public LocalEntityManagerFactoryBean entityManagerFactoryForWidgetFunctionCatalog() {
		LocalEntityManagerFactoryBean factoryBean = new LocalEntityManagerFactoryBean();
		factoryBean.setPersistenceUnitName("knowage-functioncatalog");
		return factoryBean;
	}

	@Bean
	@Qualifier("knowage-functioncatalog")
	public EntityManager entityManagerForWidgetFunctionCatalog(@Qualifier("knowage-functioncatalog") EntityManagerFactory emf) {
		EntityManager em = emf.createEntityManager();
		em.setFlushMode(FlushModeType.COMMIT);
		return em;
	}

	@Bean("knowage-gallery")
	public PlatformTransactionManager platformTransactionManagerForWidgetGallery(@Qualifier("knowage-gallery") EntityManagerFactory emf) {
		return new JpaTransactionManager(emf);
	}

	@Bean("knowage-functioncatalog")
	public PlatformTransactionManager platformTransactionManagerForFunctionCatalog(@Qualifier("knowage-functioncatalog") EntityManagerFactory emf) {
		return new JpaTransactionManager(emf);
	}

	@Primary
	@Bean
	public PlatformTransactionManager mainTransactionManager(@Qualifier("knowage-gallery") PlatformTransactionManager ptm1,
			@Qualifier("knowage-functioncatalog") PlatformTransactionManager ptm2) {
		return new ChainedTransactionManager(ptm1, ptm2);
	}

	@Bean
	@RequestScope
	public BusinessRequestContext businessRequestContext(@Value("${application.version}") String version) {
		return new BusinessRequestContext(version);
	}

	@Lazy
	@Bean
	public SecurityServiceFactory securityService() {
		return new SecurityServiceFactory();
	}

	@Bean
	public FunctionCatalogAPI functionCatalogAPI() {
		return new FunctionCatalogAPIImpl();
	}

	@Bean(name = "multipartResolver")
	public CommonsMultipartResolver multipartResolver() {
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
		multipartResolver.setMaxUploadSize(100000);
		return multipartResolver;
	}

	@Bean
	public Context context() throws NamingException {
		return new InitialContext();
	}

}
