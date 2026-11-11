package it.eng.spagobi.api.common;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class DocumentModificationAuthorizationTest {

	@Test
	public void allowsCreatorEvenForReleasedDocuments() {
		DocumentModificationAuthorization.checkCanModify(document("owner", "REL"), profile("owner"));
	}

	@Test
	public void allowsDocumentAdministratorAndSuperadmin() {
		UserProfile administrator = profile("administrator");
		administrator.setFunctionalities(Arrays.asList(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN));
		DocumentModificationAuthorization.checkCanModify(document("owner", "REL"), administrator);

		UserProfile superadmin = profile("superadmin");
		superadmin.setIsSuperadmin(true);
		DocumentModificationAuthorization.checkCanModify(document("owner", "REL"), superadmin);
	}

	@Test
	public void rejectsOtherUsersAndMissingCreator() {
		assertDenied(document("owner", "REL"), profile("reader"));
		assertDenied(document(null, "REL"), profile("reader"));
		assertDenied(document("owner", "DEV"), profile("reader"));
	}

	@Test
	public void rejectsMissingSourceAndAnonymousProfile() {
		UserProfile superadmin = profile("superadmin");
		superadmin.setIsSuperadmin(true);
		assertDenied(null, superadmin);
		assertDenied(document("owner", "REL"), null);
	}

	@Test
	public void rejectsOtherTenantsEvenForOwnerAndAdministrator() {
		BIObject document = document("owner", "REL");
		document.setTenant("other-tenant");
		assertDenied(document, profile("owner"));

		UserProfile administrator = profile("administrator");
		administrator.setFunctionalities(Arrays.asList(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN));
		assertDenied(document, administrator);
		administrator.setIsSuperadmin(true);
		assertDenied(document, administrator);
	}

	@Test
	public void allowsDeveloperOnlyForDevDocumentsWithDevelopmentAccess() {
		final boolean[] hasDevelopmentAccess = { true };
		new MockUp<ObjectsAccessVerifier>() {
			@Mock
			public boolean canDevBIObject(BIObject document, IEngUserProfile profile) {
				return hasDevelopmentAccess[0];
			}
		};

		UserProfile developer = profile("developer");
		developer.setFunctionalities(Arrays.asList(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_DEV));
		BIObject document = document("owner", "DEV");
		DocumentModificationAuthorization.checkCanModify(document, developer);

		document.setStateCode("REL");
		assertDenied(document, developer);
		document.setStateCode("DEV");
		hasDevelopmentAccess[0] = false;
		assertDenied(document, developer);
		hasDevelopmentAccess[0] = true;
		developer.setFunctionalities(Collections.emptyList());
		assertDenied(document, developer);
	}

	private BIObject document(String owner, String state) {
		BIObject document = new BIObject();
		document.setCreationUser(owner);
		document.setStateCode(state);
		document.setTenant("tenant");
		return document;
	}

	private UserProfile profile(String userId) {
		return new UserProfile(userId, "tenant");
	}

	private void assertDenied(BIObject document, UserProfile profile) {
		try {
			DocumentModificationAuthorization.checkCanModify(document, profile);
			fail("Expected document modification to be denied");
		} catch (SpagoBIRuntimeException expected) {
		}
	}
}
