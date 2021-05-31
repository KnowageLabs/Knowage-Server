/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.services.security;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java element interface generated in the it.eng.spagobi.services.security package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the Java representation for XML content. The Java representation of XML content can
 * consist of schema derived interfaces and classes representing the binding of schema type definitions, element declarations and model groups. Factory methods
 * for each of these are provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {

	private final static QName _CheckAuthorization_QNAME = new QName("http://security.services.spagobi.eng.it/", "checkAuthorization");
	private final static QName _GetUserProfile_QNAME = new QName("http://security.services.spagobi.eng.it/", "getUserProfile");
	private final static QName _GetUserProfileResponse_QNAME = new QName("http://security.services.spagobi.eng.it/", "getUserProfileResponse");
	private final static QName _IsAuthorizedResponse_QNAME = new QName("http://security.services.spagobi.eng.it/", "isAuthorizedResponse");
	private final static QName _CheckAuthorizationResponse_QNAME = new QName("http://security.services.spagobi.eng.it/", "checkAuthorizationResponse");
	private final static QName _IsAuthorized_QNAME = new QName("http://security.services.spagobi.eng.it/", "isAuthorized");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: it.eng.spagobi.services.security
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link SpagoBIUserProfile }
	 * 
	 */
	public SpagoBIUserProfile createSpagoBIUserProfile() {
		return new SpagoBIUserProfile();
	}

	/**
	 * Create an instance of {@link GetUserProfileResponse }
	 * 
	 */
	public GetUserProfileResponse createGetUserProfileResponse() {
		return new GetUserProfileResponse();
	}

	/**
	 * Create an instance of {@link IsAuthorizedResponse }
	 * 
	 */
	public IsAuthorizedResponse createIsAuthorizedResponse() {
		return new IsAuthorizedResponse();
	}

	/**
	 * Create an instance of {@link CheckAuthorization }
	 * 
	 */
	public CheckAuthorization createCheckAuthorization() {
		return new CheckAuthorization();
	}

	/**
	 * Create an instance of {@link IsAuthorized }
	 * 
	 */
	public IsAuthorized createIsAuthorized() {
		return new IsAuthorized();
	}

	/**
	 * Create an instance of {@link CheckAuthorizationResponse }
	 * 
	 */
	public CheckAuthorizationResponse createCheckAuthorizationResponse() {
		return new CheckAuthorizationResponse();
	}

	/**
	 * Create an instance of {@link SpagoBIUserProfile.Attributes }
	 * 
	 */
	public SpagoBIUserProfile.Attributes createSpagoBIUserProfileAttributes() {
		return new SpagoBIUserProfile.Attributes();
	}

	/**
	 * Create an instance of {@link GetUserProfile }
	 * 
	 */
	public GetUserProfile createGetUserProfile() {
		return new GetUserProfile();
	}

	/**
	 * Create an instance of {@link SpagoBIUserProfile.Attributes.Entry }
	 * 
	 */
	public SpagoBIUserProfile.Attributes.Entry createSpagoBIUserProfileAttributesEntry() {
		return new SpagoBIUserProfile.Attributes.Entry();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link CheckAuthorization }{@code >}}
	 * 
	 */
	@XmlElementDecl(namespace = "http://security.services.spagobi.eng.it/", name = "checkAuthorization")
	public JAXBElement<CheckAuthorization> createCheckAuthorization(CheckAuthorization value) {
		return new JAXBElement<CheckAuthorization>(_CheckAuthorization_QNAME, CheckAuthorization.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link GetUserProfile }{@code >}}
	 * 
	 */
	@XmlElementDecl(namespace = "http://security.services.spagobi.eng.it/", name = "getUserProfile")
	public JAXBElement<GetUserProfile> createGetUserProfile(GetUserProfile value) {
		return new JAXBElement<GetUserProfile>(_GetUserProfile_QNAME, GetUserProfile.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link GetUserProfileResponse }{@code >}}
	 * 
	 */
	@XmlElementDecl(namespace = "http://security.services.spagobi.eng.it/", name = "getUserProfileResponse")
	public JAXBElement<GetUserProfileResponse> createGetUserProfileResponse(GetUserProfileResponse value) {
		return new JAXBElement<GetUserProfileResponse>(_GetUserProfileResponse_QNAME, GetUserProfileResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link IsAuthorizedResponse }{@code >}}
	 * 
	 */
	@XmlElementDecl(namespace = "http://security.services.spagobi.eng.it/", name = "isAuthorizedResponse")
	public JAXBElement<IsAuthorizedResponse> createIsAuthorizedResponse(IsAuthorizedResponse value) {
		return new JAXBElement<IsAuthorizedResponse>(_IsAuthorizedResponse_QNAME, IsAuthorizedResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link CheckAuthorizationResponse }{@code >}}
	 * 
	 */
	@XmlElementDecl(namespace = "http://security.services.spagobi.eng.it/", name = "checkAuthorizationResponse")
	public JAXBElement<CheckAuthorizationResponse> createCheckAuthorizationResponse(CheckAuthorizationResponse value) {
		return new JAXBElement<CheckAuthorizationResponse>(_CheckAuthorizationResponse_QNAME, CheckAuthorizationResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link IsAuthorized }{@code >}}
	 * 
	 */
	@XmlElementDecl(namespace = "http://security.services.spagobi.eng.it/", name = "isAuthorized")
	public JAXBElement<IsAuthorized> createIsAuthorized(IsAuthorized value) {
		return new JAXBElement<IsAuthorized>(_IsAuthorized_QNAME, IsAuthorized.class, null, value);
	}

}
