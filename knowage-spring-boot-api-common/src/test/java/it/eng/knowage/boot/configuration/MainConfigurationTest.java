package it.eng.knowage.boot.configuration;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.web.client.RestTemplate;

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.factory.SecurityServiceFactory;
import it.eng.knowage.boot.factory.SecurityServiceFactoryTest;
import it.eng.spagobi.services.security.SpagoBIUserProfile;
import it.eng.spagobi.services.security.SpagoBIUserProfile.Attributes.Entry;

@Configuration
@Profile("test")
@ComponentScan({"it.eng.knowage.boot"})
public class MainConfigurationTest {

	@Bean("knowage-config")
	public LocalEntityManagerFactoryBean entityManagerFactoryForWidgetConfig() {
		Map<String, Object> properties = getEntityManagerFactoriesProperties();
		LocalEntityManagerFactoryBean factoryBean = new LocalEntityManagerFactoryBean();
		factoryBean.setPersistenceUnitName("knowage-config");
		factoryBean.setJpaPropertyMap(properties);
		return factoryBean;
	}

	private Map<String, Object> getEntityManagerFactoriesProperties() {
		Map<String, Object> properties = new HashMap<>();

		properties.put("javax.persistence.jdbc.url", "jdbc:mariadb://localhost:3310/knowage_master");
		properties.put("javax.persistence.jdbc.driver", "org.mariadb.jdbc.Driver");
		properties.put("javax.persistence.jdbc.user", "root");
		properties.put("javax.persistence.jdbc.password", "root");

		return properties;
	}

	@Lazy
	@Bean
	public SecurityServiceFactory securityService() throws NamingException, MalformedURLException {
		return new SecurityServiceFactoryTest();
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

	@Bean
	public Context context() throws NamingException {
		Hashtable env = new Hashtable();
		env.remove("org.osjava.sj.jndi.ignoreClose");
		env.put("java.naming.factory.initial", "org.osjava.sj.SimpleJndiContextFactory");
		InitialContext initialContext = new InitialContext(env);

		initialContext.bind("java:comp/env/hmacKey", "abc123");
		initialContext.bind("java:comp/env/resource_path", "D:/tmp/resources");

		return initialContext;
	}

	@Bean
	public BusinessRequestContext businessRequestContext(@Value("${application.version}") String version) {
		Entry entry = new Entry();

		entry.setKey("test");
		entry.setValue("test");

		SpagoBIUserProfile.Attributes attributes = new SpagoBIUserProfile.Attributes();

		attributes.getEntry().add(entry);

		SpagoBIUserProfile userProfile = new SpagoBIUserProfile();

		userProfile.setAttributes(attributes);
		userProfile.setIsSuperadmin(true);
		userProfile.setOrganization("DEFAULT_TENANT");
		userProfile.setUniqueIdentifier("biadmin");
		userProfile.setUserId("biadmin");
		userProfile.setUserName("biadmin");
		userProfile.getFunctions().add("WidgetGalleryManagement");

		BusinessRequestContext businessRequestContext = new BusinessRequestContext(version);
		businessRequestContext.setUsername("biadmin");
		businessRequestContext.setOrganization("DEFAULT_TENANT");
		businessRequestContext.setUserProfile(userProfile);
		return businessRequestContext;
	}

	@Bean
	@Qualifier("perUserBe2Be")
	public RestTemplate perUserBe2BeRestTemplate(RestTemplateBuilder builder, Context context, PerUserBe2BeRequestCustomizer customizer) throws NamingException, MalformedURLException {

		// TODO

		return builder.build();
	}

}
