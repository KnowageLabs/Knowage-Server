package it.eng.knowage.boot.dao.dto;

import java.time.Instant;

public interface IEntity {

	String getSbiVersionDe();

	void setSbiVersionDe(String sbiVersionDe);

	String getSbiVersionIn();

	void setSbiVersionIn(String sbiVersionIn);

	String getSbiVersionUp();

	void setSbiVersionUp(String sbiVersionUp);

	Instant getTimeDe();

	void setTimeDe(Instant timeDe);

	Instant getTimeIn();

	void setTimeIn(Instant timeIn);

	Instant getTimeUp();

	void setTimeUp(Instant timeUp);

	String getUserDe();

	void setUserDe(String userDe);

	String getUserIn();

	void setUserIn(String userIn);

	String getUserUp();

	void setUserUp(String userUp);

}