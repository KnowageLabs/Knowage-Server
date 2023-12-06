package it.eng.knowage.privacymanager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.knowage.pm.dto.PrivacyDTO;
import it.eng.knowage.pm.dto.PrivacyEventType;

public class PrivacyManagerClient {

	private static PrivacyManagerClient singleton = null;

	private final HashMap<PrivacyEventType, Session> sessionMap = new HashMap<>();
	private final HashMap<PrivacyEventType, Queue> queues = new HashMap<>();

	private static final Logger LOGGER = LogManager.getLogger(PrivacyManagerClient.class);

	private PrivacyManagerClient() {

	}

	public static synchronized PrivacyManagerClient getInstance() {
		if (singleton == null) {
			singleton = new PrivacyManagerClient();
			singleton.initialize();

		}

		return singleton;
	}

	private void initialize() {

		InputStreamReader isr = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("PrivacyManagerClient.properties"));
		Properties prop = new Properties();
		try {
			prop.load(isr);
			LOGGER.info("Initializing activeMQ connection...");
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(prop.getProperty("activemq.host"));
			Connection connection = connectionFactory.createConnection();
			for (PrivacyEventType eType : PrivacyEventType.values()) {
				Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
				sessionMap.put(eType, session);
				Queue q = session.createQueue(prop.getProperty(eType.toString()));
				queues.put(eType, q);
			}
			LOGGER.info("activeMQ connections initialized...");

		} catch (IOException e) {
			LOGGER.error("Error while loading properties from PrivacyManagerClient.properties", e);
		} catch (JMSException e) {
			LOGGER.error("Error while initializing activeMQ connection", e);
		}

	}

	public void sendMessage(PrivacyDTO dto) {

		Session session = sessionMap.get(dto.getEventType());
		Queue queue = queues.get(dto.getEventType());

		try {
			MessageProducer producer = session.createProducer(queue);
			ObjectMessage msg = session.createObjectMessage();
			msg.setObject(dto);
			producer.send(msg);
			session.commit();
		} catch (JMSException e) {
			LOGGER.error("Error while sending message [" + dto.getEventType() + "]", e);
		}

	}

}
