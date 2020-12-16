package it.eng.spagobi.signup.service.rest;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import it.eng.spagobi.services.validation.EmailValidation;
import it.eng.spagobi.services.validation.ExtendedAlphanumeric;

@JsonIgnoreProperties(ignoreUnknown = true)
class SignupDTO {

	@ExtendedAlphanumeric
	@NotNull
	private String name;
	@ExtendedAlphanumeric
	@NotNull
	private String surname;
	@ExtendedAlphanumeric
	@NotNull
	private String password;
	@EmailValidation
	private String email;

	// properties for creation only

	@ExtendedAlphanumeric
	@NotNull
	private String confirmPassword;

	private String gender;
	private String birthDate;
	private String enterprise;
	private String biography;
	private String language;
	private String useCaptcha;
	private String captcha;

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

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getEnterprise() {
		return enterprise;
	}

	public void setEnterprise(String enterprise) {
		this.enterprise = enterprise;
	}

	public String getBiography() {
		return biography;
	}

	public void setBiography(String biography) {
		this.biography = biography;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getCaptcha() {
		return captcha;
	}

	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}

	public String getUseCaptcha() {
		return useCaptcha;
	}

	public void setUseCaptcha(String useCaptcha) {
		this.useCaptcha = useCaptcha;
	}

}
