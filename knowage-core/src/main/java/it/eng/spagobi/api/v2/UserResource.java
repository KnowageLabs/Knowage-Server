/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.api.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.eng.knowage.commons.security.FileContentValidator;
import it.eng.knowage.security.OwaspDefaultEncoderFactory;
import it.eng.knowage.security.ProductProfiler;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.utilities.FileUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.commons.validation.PasswordChecker;
import it.eng.spagobi.dao.QueryFilters;
import it.eng.spagobi.profiling.PublicProfile;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bean.SbiUserAttributesId;
import it.eng.spagobi.profiling.bo.UserBO;
import it.eng.spagobi.profiling.bo.UserBOResult; //new
import it.eng.spagobi.profiling.dao.ISbiAttributeDAO;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.profiling.dao.SbiUserDAOHibImpl;
import it.eng.spagobi.profiling.dao.filters.FinalUsersFilter;
import it.eng.spagobi.security.Password;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.owasp.esapi.Encoder;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;

import static it.eng.knowage.commons.security.FilesValidator.validateStringFilenameUsingContains;
import static it.eng.spagobi.commons.utilities.FileUtilities.getFileName;
import static it.eng.spagobi.commons.utilities.UserDataParsing.parseWithApachePOI;
import static it.eng.spagobi.commons.utilities.UserDataParsing.parseWithOpenCSV;

@Path("/2.0/users")
@ManageAuthorization
public class UserResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = LogManager.getLogger(UserResource.class);
	private static final String[] ADMIN_ROLES = { "admin", "dev_role", "model_admin" };
	private static final String[] USER_ROLES = { "user", "test_role" };
	private static final String CHARSET = "; charset=UTF-8";

	@GET
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PROFILE_MANAGEMENT,
			CommunityFunctionalityConstants.FINAL_USERS_MANAGEMENT })
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + CHARSET)
	public Response getUserList(@QueryParam("dateFilter") String dateFilter) {
		ISbiUserDAO usersDao = null;
		List<UserBO> fullList = null;
		ISbiAttributeDAO objDao = null;
		ArrayList<Integer> hiddenAttributesIds = new ArrayList<>();
		ProfileAttributeResourceRoleProcessor roleFilter = new ProfileAttributeResourceRoleProcessor();
		try {
			IEngUserProfile profile = getUserProfile();
			QueryFilters qp = new QueryFilters();
			objDao = DAOFactory.getSbiAttributeDAO();
			objDao.setUserProfile(getUserProfile());
			if (profile.isAbleToExecuteAction(CommunityFunctionalityConstants.PROFILE_MANAGEMENT)) {
				// administrator: he can see every user
			} else {
				// user with FINAL_USERS_MANAGEMENT (users with neither
				// FINAL_USERS_MANAGEMENT nor PROFILE_MANAGEMENT are blocked by the
				// business_map.xml therefore they cannot execute this action)
				qp.add(new FinalUsersFilter());
			}

			usersDao = DAOFactory.getSbiUserDAO();
			usersDao.setUserProfile(getUserProfile());

			if (dateFilter != null) {
				fullList = usersDao.loadUsers(qp, dateFilter);
			} else {
				fullList = usersDao.loadUsers(qp);
			}

			for (UserBO user : fullList) {
				if (!UserUtilities.isTechnicalUser(getUserProfile())) {
					hiddenAttributesIds = roleFilter.getHiddenAttributesIds();
					roleFilter.removeHiddenAttributes(hiddenAttributesIds, user);
				}

			}

			return Response.ok(fullList).build();
		} catch (Exception e) {
			LOGGER.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("Error with loading resource", getLocale(), e);
		}
	}

	@GET
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PROFILE_MANAGEMENT,
			CommunityFunctionalityConstants.FINAL_USERS_MANAGEMENT })
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + CHARSET)
	public Response getUserById(@PathParam("id") Integer id) {
		ISbiUserDAO usersDao = null;
		SbiUserDAOHibImpl hib = new SbiUserDAOHibImpl();
		ISbiAttributeDAO objDao = null;
		ArrayList<Integer> hiddenAttributesIds = new ArrayList<>();
		ProfileAttributeResourceRoleProcessor roleFilter = new ProfileAttributeResourceRoleProcessor();
		try {

			SbiUser sbiUser = null;
			UserBO user = null;
			usersDao = DAOFactory.getSbiUserDAO();
			usersDao.setUserProfile(getUserProfile());
			sbiUser = usersDao.loadSbiUserById(id);
			user = hib.toUserBO(sbiUser);

			objDao = DAOFactory.getSbiAttributeDAO();
			objDao.setUserProfile(getUserProfile());

			if (!UserUtilities.isTechnicalUser(getUserProfile())) {
				hiddenAttributesIds = roleFilter.getHiddenAttributesIds();
				roleFilter.removeHiddenAttributes(hiddenAttributesIds, user);
			}
			return Response.ok(user).build();
		} catch (Exception e) {
			LOGGER.error("User with selected id: {} doesn't exists", id, e);
			throw new SpagoBIRestServiceException("Item with selected id: " + id + " doesn't exists",
					getLocale(), e);
		}
	}

    @POST
    @Path("/massive")
    @UserConstraint(functionalities = { CommunityFunctionalityConstants.PROFILE_MANAGEMENT,
            CommunityFunctionalityConstants.FINAL_USERS_MANAGEMENT })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Inserimento massivo utenti",
            description = "Elabora una lista di utenti per l'inserimento nel sistema. Ritorna un report dettagliato per ogni utente inviato.",
            tags = {"Gestione Utenti"}
    )
    @ApiResponses(value = {
            @ApiResponse(
             responseCode = "200",
             description = "Operazione completata (anche in caso di fallimenti parziali dei singoli utenti)",
             content = @Content(
                            mediaType = "application_json",
                            array = @ArraySchema(schema = @Schema(implementation = UserBOResult.class))
                        )
                       ),
            @ApiResponse(responseCode = "403", description = "Accesso negato: permessi insufficienti"),
            @ApiResponse(responseCode = "500", description = "Errore generico del server")
   })

    public Response insertUserAll(@Parameter(description = "Lista di oggetti UserBO da processare", required = true) List <UserBO> requestDTO) {

        MessageBuilder msgBuilder = new MessageBuilder();
        Locale locale = msgBuilder.getLocale(request);

        ISbiUserDAO usersDao = DAOFactory.getSbiUserDAO();
        usersDao.setUserProfile(getUserProfile());
        List<UserBOResult> results = new ArrayList<>();

        for (UserBO user : requestDTO) {

            UserBOResult result = new UserBOResult();
            try {

                if (user.getUserId() == null || user.getUserId().isBlank()) {
                    LOGGER.error("The userid is required.");
                    throw new Exception("The userid is required.");
                }

            } catch (Exception e) {

                result.setSuccess(false);
                result.setMessage(e.getMessage());
                results.add (result);

                continue;
            }

            String userId = user.getUserId();
            boolean isAdmin = UserUtilities.userRequestDtoIsAdmin(user);
            SbiUser existingUser = usersDao.loadSbiUserByUserId(userId);

            try {

                if (!userCanBeAdded(usersDao, isAdmin)) {
                    LOGGER.error("The limit for creating {} users has been reached.", isAdmin ? "admin " : "end ");
                    throw new Exception("The limit for creating " + (isAdmin ? "admin " : "end ") + "users has been reached.");

                }

            } catch (Exception e) {
                result.setSuccess(false);
                result.setUserId(user.getUserId());
                result.setMessage(e.getMessage());
                results.add (result);

                continue;
            }

            SbiUser sbiUser = new SbiUser();
            sbiUser.setUserId(user.getUserId());
            sbiUser.setFullName(user.getFullName());
            sbiUser.setPassword(user.getPassword());
            sbiUser.setDefaultRoleId(user.getDefaultRoleId());

            List<Integer> list = user.getSbiExtUserRoleses();
            Set<SbiExtRoles> roles = new HashSet<>(0);
            for (Integer id : list) {
                SbiExtRoles role = new SbiExtRoles(id);

                roles.add(role);
            }
            sbiUser.setSbiExtUserRoleses(roles);

            HashMap<Integer, HashMap<String, String>> map = user.getSbiUserAttributeses();
            Set<SbiUserAttributes> attributes = new HashSet<>(0);

            for (Entry<Integer, HashMap<String, String>> entry : map.entrySet()) {
                SbiUserAttributes attribute = new SbiUserAttributes();
                SbiUserAttributesId attid = new SbiUserAttributesId(entry.getKey());
                attribute.setId(attid);
                for (Entry<String, String> value : entry.getValue().entrySet()) {

                    attribute.setAttributeValue(value.getValue());

                }
                attributes.add(attribute);
            }
            sbiUser.setSbiUserAttributeses(attributes);

            String password;
            if (sbiUser.getPassword() == null || sbiUser.getPassword().isBlank()) {
                IConfigDAO configDAO = DAOFactory.getSbiConfigDAO();

                try {
                    //Config config = configDAO.loadConfigParametersById(199);
                    Config config = configDAO.loadConfigParametersByLabel(SpagoBIConstants.KNOWAGE_USERDEFAULT_PASSWORD);
                    password = config.getValueCheck();
                } catch (Exception e) {
                    LOGGER.debug("Impossible to retrive from the configuration the property ["
                            + SpagoBIConstants.JNDI_THREAD_MANAGER + "]");
                    throw new SpagoBIRuntimeException("Impossible to retrive from the configuration the property ["
                            + SpagoBIConstants.JNDI_THREAD_MANAGER + "]");
                }

            } else {
                password = sbiUser.getPassword();

                try {
                    PasswordChecker.getInstance().checkPwd(password);
                } catch (Exception e) {
                    LOGGER.error("Password is not valid", e);

                    result.setSuccess(false);
                    result.setUserId(user.getUserId());
                    result.setMessage(msgBuilder.getMessage("signup.check.pwdInvalid", "messages", locale));
                    results.add (result);

                    continue;
                }
            }

            try {
                sbiUser.setPassword(Password.hashPassword(password));
            } catch (Exception e) {
                LOGGER.error("Impossible to encrypt Password", e);
                throw new SpagoBIServiceException("SPAGOBI_SERVICE", "Impossible to encrypt Password", e);
            }

            try {
                Integer id = usersDao.fullSaveOrUpdateSbiUser(sbiUser);
                Encoder encoder = OwaspDefaultEncoderFactory.getInstance().getEncoder();
                String encodedUser = encoder.encodeForURL("" + id);

                result.setSuccess(true);
                result.setUserId(user.getUserId());
                result.setCreatedUserId(id);
                result.setMessage("User processed successfully");
                results.add (result);

            } catch (Exception e) {
                LOGGER.error("Error while inserting resource", e);

                result.setSuccess(false);
                result.setUserId(user.getUserId());
                result.setMessage(e.getMessage());
                results.add (result);

            }

        }

        return Response.ok(results).build();
    }

    @POST
    @Path("/uploadFileForInsert")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Caricamento file per inserimento utenti",
            description = "Permette di caricare un file CSV o Excel (.xls, .xlsx) contenente i dati degli utenti da inserire.",
            requestBody = @RequestBody(
                    description = "File da caricare",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA,
                            schema = @Schema(implementation = FileUtilities.FileUploadWrapper.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "File processato e utenti inseriti",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserBOResult.class)))  ),
                            @ApiResponse(responseCode = "500", description = "Errore durante il processamento del file")
                    }
                    )
    public Response uploadFileForInsert(MultipartFormDataInput multipartFormDataInput) throws SpagoBIRuntimeException {

        Map<String, List<InputPart>> formDataMap = multipartFormDataInput.getFormDataMap();
        List<UserBO> users = new ArrayList<>();

        if (!formDataMap.containsKey("file")) {
            throw new SpagoBIRuntimeException("Cannot find the file part in input");
        }

        try {
            InputPart inputPart = formDataMap.get("file").get(0);
            String fileName = getFileName(inputPart.getHeaders());

            if (fileName == null || !validateStringFilenameUsingContains(fileName)) {
                throw new SpagoBIRuntimeException("Invalid file name or content");
            }

            try (InputStream is = inputPart.getBody(InputStream.class, null)) {
                InputStream validatedStream = FileContentValidator.validateFileContent(is, fileName);

                if (fileName.toLowerCase().endsWith(".csv")) {
                    users = parseWithOpenCSV(validatedStream);
                } else if (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls")) {
                    users = parseWithApachePOI(validatedStream);

                } else {
                    throw new SpagoBIRuntimeException("Unsupported file format. Please upload CSV or Excel.");
                }

                return insertUserAll(users);
            }
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Error processing file: " + e.getMessage());
        }
    }



	@POST
	@Path("/")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PROFILE_MANAGEMENT,
			CommunityFunctionalityConstants.FINAL_USERS_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertUser(@Valid UserBO requestDTO) {

		MessageBuilder msgBuilder = new MessageBuilder();
		Locale locale = msgBuilder.getLocale(request);

		String userId = requestDTO.getUserId();
		if (userId.startsWith(PublicProfile.PUBLIC_USER_PREFIX)) {
			LOGGER.error("public is reserved prefix for user id");
			throw new SpagoBIServiceException("SPAGOBI_SERVICE", "public_ is a reserved prefix for user name", null);
		}
		ISbiUserDAO usersDao = DAOFactory.getSbiUserDAO();

		boolean isAdmin = UserUtilities.userRequestDtoIsAdmin(requestDTO);

		if (!userCanBeAdded(usersDao, isAdmin)) {
			LOGGER.error("The limit for creating {} users has been reached.", isAdmin ? "admin " : "end ");
			throw new SpagoBIServiceException("Create user", "The limit for creating " + (isAdmin ? "admin " : "end ") + "users has been reached.");
		}

		usersDao.setUserProfile(getUserProfile());
		SbiUser existingUser = usersDao.loadSbiUserByUserId(userId);
		if (existingUser != null && userId.equals(existingUser.getUserId())) {
			LOGGER.error("User already exists. User_ID is unique");
			throw new SpagoBIRestServiceException("User with provided ID already exists.", getLocale(),
					new Throwable());
		}

		SbiUser sbiUser = new SbiUser();
		sbiUser.setUserId(requestDTO.getUserId());
		sbiUser.setFullName(requestDTO.getFullName());
		sbiUser.setPassword(requestDTO.getPassword());
		sbiUser.setDefaultRoleId(requestDTO.getDefaultRoleId());

		List<Integer> list = requestDTO.getSbiExtUserRoleses();
		Set<SbiExtRoles> roles = new HashSet<>(0);
		for (Integer id : list) {
			SbiExtRoles role = new SbiExtRoles(id);

			roles.add(role);
		}
		sbiUser.setSbiExtUserRoleses(roles);

		HashMap<Integer, HashMap<String, String>> map = requestDTO.getSbiUserAttributeses();
		Set<SbiUserAttributes> attributes = new HashSet<>(0);

		for (Entry<Integer, HashMap<String, String>> entry : map.entrySet()) {
			SbiUserAttributes attribute = new SbiUserAttributes();
			SbiUserAttributesId attid = new SbiUserAttributesId(entry.getKey());
			attribute.setId(attid);
			for (Entry<String, String> value : entry.getValue().entrySet()) {

				attribute.setAttributeValue(value.getValue());

			}
			attributes.add(attribute);
		}
		sbiUser.setSbiUserAttributeses(attributes);

		String password = sbiUser.getPassword();

		try {
			PasswordChecker.getInstance().checkPwd(password);
		} catch (Exception e) {
			LOGGER.error("Password is not valid", e);
			String message = msgBuilder.getMessage("signup.check.pwdInvalid", "messages", locale);
			if (e instanceof EMFUserError) {
				throw new SpagoBIServiceException(((EMFUserError) e).getDescription(), message);
			} else {
				throw new SpagoBIServiceException(message, e);
			}
		}

			try {
				sbiUser.setPassword(Password.hashPassword(password));
			} catch (Exception e) {
				LOGGER.error("Impossible to encrypt Password", e);
				throw new SpagoBIServiceException("SPAGOBI_SERVICE", "Impossible to encrypt Password", e);
			}

		try {
			Integer id = usersDao.fullSaveOrUpdateSbiUser(sbiUser);
			Encoder encoder = OwaspDefaultEncoderFactory.getInstance().getEncoder();
			String encodedUser = encoder.encodeForURL("" + id);
			return Response.created(new URI("2.0/users/" + encodedUser)).entity(encodedUser).build();
		} catch (Exception e) {
			LOGGER.error("Error while inserting resource", e);
			throw new SpagoBIRestServiceException("Error while inserting resource", getLocale(), e);
		}
	}

	@PUT
	@Path("/{id}")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PROFILE_MANAGEMENT,
			CommunityFunctionalityConstants.FINAL_USERS_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUser(@PathParam("id") Integer id, @Valid UserBO requestDTO) {

		MessageBuilder msgBuilder = new MessageBuilder();
		Locale locale = msgBuilder.getLocale(request);

		SbiUser sbiUserOriginal = null;
		ISbiUserDAO usersDao = null;
		ProfileAttributeResourceRoleProcessor roleFilter = new ProfileAttributeResourceRoleProcessor();

		String userId = requestDTO.getUserId();
		if (userId.startsWith(PublicProfile.PUBLIC_USER_PREFIX)) {
			LOGGER.error("public is reserved prefix for user id");
			throw new SpagoBIServiceException("SPAGOBI_SERVICE", "public_ is a reserved prefix for user name", null);
		}

		usersDao = DAOFactory.getSbiUserDAO();
		boolean isAdmin = UserUtilities.userRequestDtoIsAdmin(requestDTO);

		if (isChangingRoles(isAdmin, id) && !userCanBeAdded(usersDao, isAdmin)) {
			LOGGER.error("The limit for creating {} users has been reached.", isAdmin ? "admin " : "end ");
			throw new SpagoBIServiceException("Update user", "The limit for creating " + (isAdmin ? "admin " : "end ") + "users has been reached.");
		}

		SbiUser sbiUser = new SbiUser();
		sbiUser.changeId(id);
		sbiUser.setUserId(requestDTO.getUserId());
		sbiUser.setFullName(requestDTO.getFullName());
		sbiUser.setPassword(requestDTO.getPassword());
		sbiUser.setDefaultRoleId(requestDTO.getDefaultRoleId());
		sbiUser.setFailedLoginAttempts(requestDTO.getFailedLoginAttempts());

		// This reset the account lock enabled in case of too much failed login attempts
		// sbiUser.setFailedLoginAttempts(0);

		List<Integer> list = requestDTO.getSbiExtUserRoleses();
		Set<SbiExtRoles> roles = new HashSet<>(0);
		for (Integer i : list) {
			SbiExtRoles role = new SbiExtRoles(i);

			roles.add(role);
		}
		sbiUser.setSbiExtUserRoleses(roles);

		HashMap<Integer, HashMap<String, String>> map = requestDTO.getSbiUserAttributeses();
		Set<SbiUserAttributes> attributes = new HashSet<>(0);
		List<SbiAttribute> attrList = null;
		ISbiAttributeDAO objDao = null;

		try {
			usersDao.setUserProfile(getUserProfile());
			sbiUserOriginal = usersDao.loadSbiUserById(sbiUser.getId());
			objDao = DAOFactory.getSbiAttributeDAO();
			objDao.setUserProfile(getUserProfile());
			attrList = objDao.loadSbiAttributes();
		} catch (EMFUserError e1) {
			LOGGER.error("Impossible get attributes", e1);
		}

		/* KNOWAGE-8687: CheckPassword if changed */
		if (requestDTO.getPassword() != null) {
			try {
				PasswordChecker.getInstance().isValid(sbiUserOriginal, sbiUserOriginal.getPassword(), true, requestDTO.getPassword(), requestDTO.getPassword());
			} catch (Exception e) {
				LOGGER.error("Password is not valid", e);
				String message = msgBuilder.getMessage("signup.check.pwdInvalid", "messages", locale);
				if (e instanceof EMFUserError) {
					throw new SpagoBIServiceException(((EMFUserError) e).getDescription(), message);
				} else {
					throw new SpagoBIServiceException(message, e);
				}
			}
		}


		for (Entry<Integer, HashMap<String, String>> entry : map.entrySet()) {
			SbiUserAttributes attribute = new SbiUserAttributes();
			SbiUserAttributesId attid = new SbiUserAttributesId(entry.getKey());
			attribute.setId(attid);
			for (Entry<String, String> value : entry.getValue().entrySet()) {

				attribute.setAttributeValue(value.getValue());

			}
			attributes.add(attribute);
		}

		// This method get hidden attributes from user and sets their value to last known in DB
		// By this we are avoiding changing that value if user change some other attributes
		try {
			if (objDao.getUserProfile().getRoles().size() == 1
					&& objDao.getUserProfile().getRoles().toArray()[0].equals("user")) {
				roleFilter.setAttributeHiddenFromUser(sbiUserOriginal, attributes, attrList);
			}
		} catch (EMFInternalError e1) {
			LOGGER.error(e1.getMessage(), e1);
		}

		sbiUser.setSbiUserAttributeses(attributes);

		String password = sbiUser.getPassword();
		if (password != null && password.length() > 0) {
			try {
				sbiUser.setPassword(Password.hashPassword(password));
			} catch (Exception e) {
				LOGGER.error("Impossible to encrypt Password", e);
				throw new SpagoBIServiceException("SPAGOBI_SERVICE", "Impossible to encrypt Password", e);
			}
		} else {
			sbiUser.setPassword(null);
		}

		try {
			usersDao = DAOFactory.getSbiUserDAO();
			usersDao.setUserProfile(getUserProfile());
			Integer idToReturn = usersDao.fullSaveOrUpdateSbiUser(sbiUser);
			Encoder encoder = OwaspDefaultEncoderFactory.getInstance().getEncoder();
			String encodedUser = encoder.encodeForURL("" + idToReturn);
			return Response.created(new URI("2.0/users/" + encodedUser)).entity(encodedUser).build();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new SpagoBIRestServiceException(e.getMessage(), getLocale(), e);
		}
	}

    private boolean isChangingRoles(boolean isAdmin, Integer userId) {
        ISbiUserDAO userDAO = DAOFactory.getSbiUserDAO();
        IRoleDAO rolesDAO = DAOFactory.getRoleDAO();
        List<SbiExtRoles> roles = userDAO.loadSbiUserRolesById(userId);

        if (isAdmin) {
            try {
                for (int i = 0; i < roles.size(); i++) {
                    SbiExtRoles role = rolesDAO.loadSbiExtRoleById(roles.get(i).getExtRoleId());
                    if (UserUtilities.isRoleApplicable(role, true)) {
                        return false;
                    }
                }
            } catch (EMFUserError ue) {
                LOGGER.error("Impossible to get roles", ue);
                throw new SpagoBIRuntimeException("Impossible to get roles", ue);
            }
            return true;
        } else {
            try {
                for (int i = 0; i < roles.size(); i++) {
                    SbiExtRoles role = rolesDAO.loadSbiExtRoleById(roles.get(i).getExtRoleId());
                    if (UserUtilities.isRoleApplicable(role, true)) {
                        return true;
                    }
                }
            } catch (EMFUserError ue) {
                LOGGER.error("Impossible to get roles", ue);
                throw new SpagoBIRuntimeException("Impossible to get roles", ue);
            }
            return false;
        }
    }


    public boolean userCanBeAdded(ISbiUserDAO usersDao, boolean isAdmin) {
		List<SbiUser> usersToCheck = UserUtilities.getAlreadyCreatedUsers(usersDao, isAdmin);
		return ProductProfiler.canAddAUser(usersToCheck.size(), isAdmin);
	}


	@PUT
	@Path("/{id}/resetOtp")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PROFILE_MANAGEMENT, CommunityFunctionalityConstants.FINAL_USERS_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response resetOtp(@PathParam("id") Integer id) {
		try {
			ISbiUserDAO usersDao = DAOFactory.getSbiUserDAO();
			usersDao.resetOtpSecret(id);
		} catch (Exception e) {
			Response.serverError().build();
		}
		return Response.ok().build();
	}

	@DELETE
	@Path("/{id}")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PROFILE_MANAGEMENT,
			CommunityFunctionalityConstants.FINAL_USERS_MANAGEMENT })
	public Response deleteCheck(@PathParam("id") Integer id) {

		ISbiUserDAO usersDao = null;

		try {
			usersDao = DAOFactory.getSbiUserDAO();
			usersDao.setUserProfile(getUserProfile());
			usersDao.deleteSbiUserById(id);
			Encoder encoder = OwaspDefaultEncoderFactory.getInstance().getEncoder();
			String encodedUser = encoder.encodeForURL("" + id);
			return Response.ok().entity(encodedUser).build();
		} catch (Exception e) {
			LOGGER.error("Error with deleting resource with id: {}", id, e);
			throw new SpagoBIRestServiceException("Error with deleting resource with id: " + id,
					getLocale(), e);
		}
	}

}
