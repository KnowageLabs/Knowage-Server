package it.eng.knowage.mailsender.factory;

import it.eng.knowage.mailsender.IMailSender;
import it.eng.knowage.mailsender.impl.JavaApiMailSender;
import it.eng.knowage.mailsender.impl.SirturRestMailSender;

public class FactoryMailSender {

	public static IMailSender getMailSender(String sender) {

		if (sender != null && sender.equalsIgnoreCase(SirturRestMailSender.LABEL_MAILSENDER)) {
			return new SirturRestMailSender();
		}

		return new JavaApiMailSender();

	}

}
