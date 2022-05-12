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

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import it.eng.knowage.boot.filter.JWTSecurityFilter;
import it.eng.knowage.knowageapi.service.FunctionCatalogAPI;
import it.eng.knowage.knowageapi.service.impl.FunctionCatalogAPIImpl;

@Configuration
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
@Profile("production")
@ComponentScan({
	"it.eng.knowage.knowageapi",
	"it.eng.knowage.resourcemanager"
})
public class KnowageApiConfiguration {

	@Primary /* just to prevent Spring error */
	@Bean("knowage-gallery")
	public LocalEntityManagerFactoryBean entityManagerFactoryForWidgetGallery() {
		LocalEntityManagerFactoryBean factoryBean = new LocalEntityManagerFactoryBean();
		factoryBean.setPersistenceUnitName("knowage-gallery");
		return factoryBean;
	}

	@Bean("knowage-functioncatalog")
	public LocalEntityManagerFactoryBean entityManagerFactoryForWidgetFunctionCatalog() {
		LocalEntityManagerFactoryBean factoryBean = new LocalEntityManagerFactoryBean();
		factoryBean.setPersistenceUnitName("knowage-functioncatalog");
		return factoryBean;
	}

	@Primary
	@Bean
	public PlatformTransactionManager mainTransactionManager() {
		return new ChainedTransactionManager(new JpaTransactionManager(entityManagerFactoryForWidgetGallery().getObject()),
				new JpaTransactionManager(entityManagerFactoryForWidgetFunctionCatalog().getObject()));
	}

	@Bean
	public FunctionCatalogAPI functionCatalogAPI() {
		return new FunctionCatalogAPIImpl();
	}

	@Bean(name = "JWTSecurityFilter")
	public JWTSecurityFilter jwtSecurityFilter() {
		return new JWTSecurityFilter();
	}

	@Bean
	public FilterRegistrationBean<JWTSecurityFilter> jwtSecurityFilterRegistration() {
		FilterRegistrationBean<JWTSecurityFilter> filter = new FilterRegistrationBean<>();

		filter.setFilter(jwtSecurityFilter());
		filter.setOrder(10);

		/*
		 * Add all filter's patterns here.
		 */
		filter.addUrlPatterns("/api/*");

		return filter;
	}
}
