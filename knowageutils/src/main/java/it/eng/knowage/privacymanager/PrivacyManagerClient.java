/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2023 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.knowage.privacymanager;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

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

	private static final Logger LOGGER = LogManager.getLogger(PrivacyManagerClient.class);

	private static PrivacyManagerClient singleton = null;

	private final Map<PrivacyEventType, Session> sessionMap = new EnumMap<>(PrivacyEventType.class);
	private final Map<PrivacyEventType, Queue> queues = new EnumMap<>(PrivacyEventType.class);

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

		PMConfiguration prop = PMConfiguration.getInstance();
		try {

			LOGGER.info("Initializing activeMQ connection...");

			String activeMqHost = prop.getProperty("activemq.host");

			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(activeMqHost);
			connectionFactory.setTrustedPackages(Arrays.asList("it.eng.knowage"));

			Connection connection = connectionFactory.createConnection();
			for (PrivacyEventType eType : PrivacyEventType.values()) {
				Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
				sessionMap.put(eType, session);
				Queue q = session.createQueue(prop.getProperty(eType.toString()));
				queues.put(eType, q);
			}
			LOGGER.info("activeMQ connections initialized...");

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
			LOGGER.error("Error while sending message [{}]", dto.getEventType(), e);
		}

	}

}
