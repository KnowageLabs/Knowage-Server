package it.eng.spagobi.signup.service.rest;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import it.eng.spagobi.services.validation.ExtendedAlphanumeric;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SignupDTO {

	@ExtendedAlphanumeric
	@NotNull
	private String name;
	@ExtendedAlphanumeric
	@NotNull
	private String surname;
	@ExtendedAlphanumeric
	@NotNull
	private String password;
	@ExtendedAlphanumeric
	private String email;
	private String birthDate;

	@ExtendedAlphanumeric
	@NotNull
	private String username;

	@ExtendedAlphanumeric
	private String address;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
