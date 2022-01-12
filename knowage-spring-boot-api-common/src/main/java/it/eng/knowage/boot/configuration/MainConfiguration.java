package it.eng.knowage.boot.configuration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.factory.SecurityServiceFactory;

@Configuration
@Profile("production")
@ComponentScan({"it.eng.knowage.boot"})
public class MainConfiguration {

	@Bean("knowage-config")
	public LocalEntityManagerFactoryBean entityManagerFactoryForWidgetConfig() {
		LocalEntityManagerFactoryBean factoryBean = new LocalEntityManagerFactoryBean();
		factoryBean.setPersistenceUnitName("knowage-config");
		return factoryBean;
	}

	@Lazy
	@Bean
	public SecurityServiceFactory securityService() {
		return new SecurityServiceFactory();
	}

	@Bean
	@Scope(scopeName = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public BusinessRequestContext businessRequestContext(@Value("${application.version}") String version) {
		return new BusinessRequestContext(version);
	}

	@Bean
	public static BeanFactoryPostProcessor beanFactoryPostProcessor() {
		return new BeanFactoryPostProcessor() {
			@Override
			public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
				beanFactory.registerScope("thread", new SimpleThreadScope());
			}
		};
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
