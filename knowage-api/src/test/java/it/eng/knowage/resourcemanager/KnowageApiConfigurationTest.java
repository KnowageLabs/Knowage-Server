package it.eng.knowage.resourcemanager;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
@ComponentScan("it.eng.knowage.resourcemanager")
public class KnowageApiConfigurationTest {

}
