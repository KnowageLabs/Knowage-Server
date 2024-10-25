package it.eng.knowage.mailsender;

import it.eng.knowage.mailsender.dto.MessageMailDto;

public interface IMailSender {

	String MAIL_SENDER = "MAIL_SENDER";

	void sendMail(MessageMailDto messageMailDto) throws Exception;

}
