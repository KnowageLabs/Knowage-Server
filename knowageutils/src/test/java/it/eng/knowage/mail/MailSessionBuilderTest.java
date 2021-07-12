package it.eng.knowage.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.junit.Before;
import org.junit.Test;

import it.eng.knowage.mail.MailSessionBuilder.SessionFacade;

/**
 * Execute tests on {@link MailSessionBuilder}.
 *
 * You need to provide some environment variables to execute the tests:
 * <pre>
 * <ul>
 * <li>USER_%02d</li>
 * <li>FROM_%02d</li>
 * <li>PASSWORD_%02d</li>
 * <li>TO_%02d</li>
 * <li>HOST_%02d</li>
 * <li>PORT_%02d</li>
 * <li>SECURITY_%02d</li>
 * </ul>
 * </pre>
 *
 * Starting from 1 to infinite like:
 * <pre>
 * <ul>
 * <li>USER_01=myuser</li>
 * <li>FROM_01=myuser</li>
 * <li>PASSWORD_01=mypass</li>
 * <li>TO_01=to@domain.com</li>
 * <li>HOST_01=myhost</li>
 * <li>PORT_01=465</li>
 * <li>SECURITY_01=SSL</li>
 * </ul>
 * </pre>
 *
 * @author Marco Libanori
 *
 */
public class MailSessionBuilderTest {

	private final List<Data> dataList = new ArrayList<Data>();

	private static class Data {
		String user = null;
		String from = null;
		String password = null;
		String to = null;
		String host = null;
		String port = null;
		String security = null;
	}

	@Before
	public void init() throws AddressException {

		Map<String, String> getenv = System.getenv();

		int i = 1;

		while (true) {
			String suffix = String.format("%02d", i);

			String userKey = "USER_" + suffix;
			String fromKey = "FROM_" + suffix;
			String passwordKey = "PASSWORD_" + suffix;
			String toKey = "TO_" + suffix;
			String hostKey = "HOST_" + suffix;
			String portKey = "PORT_" + suffix;
			String securityKey = "SECURITY_" + suffix;

			if (getenv.containsKey(userKey)) {
				Data data = new Data();

				data.user = getenv.get(userKey);
				data.from = Optional.ofNullable(getenv.get(fromKey)).orElse(data.user);
				data.password = getenv.get(passwordKey);
				data.to = getenv.get(toKey);
				data.host = getenv.get(hostKey);
				data.port = getenv.get(portKey);
				data.security = getenv.get(securityKey);

				dataList.add(data);
			} else {
				break;
			}

			i++;
		}

	}

	@Test
	public void testAllConfigs() throws MessagingException {

		for (Data data : dataList) {
			SessionFacade sessionFacade = MailSessionBuilder.newInstance()
				.setHost(data.host)
				.setPort(data.port)
				.setFromAddress(data.from)
				.setUser(data.user)
				.setPassword(data.password)
				.setSecurityMode(data.security)
				.enableDebug()
				.build();

			Message newMessage = sessionFacade.createNewMimeMessage();
			newMessage.addRecipient(RecipientType.TO, new InternetAddress(data.to));
			newMessage.setSubject(String.format("Email from a JUnit test using %s as host and %s as security mode", data.host, data.security));
			newMessage.setText(String.format("Email from a JUnit test using %s as host and %s as security mode", data.host, data.security));

			sessionFacade.sendMessage(newMessage);
		}
	}

}
