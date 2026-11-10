package it.eng.spagobi.commons.services;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.monitor.IKnowageMonitor;
import it.eng.knowage.monitor.KnowageMonitorFactory;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.security.OAuth2.OAuth2Config;
import it.eng.spagobi.services.rest.annotations.PublicService;
import it.eng.spagobi.security.saml.SamlConfiguration;

@Path("/loginconfig")
public class LoginConfigResource extends AbstractSpagoBIResource {

    private static final Logger logger = Logger.getLogger(LoginConfigResource.class);

    @GET
    @Path("/")
    @PublicService
    public Response getLoginConfig(@Context HttpServletRequest req) {
        IKnowageMonitor monitor = KnowageMonitorFactory.getInstance().start("knowage.login.config.load");
        try {

			SingletonConfig config = SingletonConfig.getInstance();

			Map<String, Object> item = new HashMap<>();
			Object ssoActiveValue = config.getConfigValue("SPAGOBI_SSO.ACTIVE");
			boolean ssoActive = Boolean.parseBoolean(String.valueOf(ssoActiveValue));
			String oauth2FlowType = Optional.ofNullable(System.getProperty("oauth2_flow_type", System.getenv("OAUTH2_FLOW_TYPE"))).orElse("");
			item.put("ssoActive", ssoActive);
			item.put("samlActive", SamlConfiguration.enabled());
			if (SamlConfiguration.enabled()) {
				item.put("samlLoginUrl", req.getContextPath() + "/restful-services/saml/login");
				item.put("samlExchangeUrl", req.getContextPath() + "/restful-services/saml/exchange");
				item.put("authenticationMethod", "SAML");
			}
			item.put("defaultLanguage", config.getConfigValue("SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGE.default"));
			item.putAll(LoginBranding.read(config.getConfigValue(LoginBranding.CONFIG_LABEL)));
			item.put("oauth2FlowType", oauth2FlowType);

			item.put("jwtLabel", System.getProperty("JWT_LABEL", System.getenv("JWT_LABEL")));
			item.put("jwtSessionStorage", System.getProperty("JWT_SESSION_STORAGE", System.getenv("JWT_SESSION_STORAGE")));

			if (oauth2FlowType != null && !oauth2FlowType.equalsIgnoreCase("NONE")) {
				OAuth2Config oauth2Config = OAuth2Config.getInstance();
				item.put("authorizeUrl", oauth2Config.getAuthorizeUrl());
				item.put("clientId", oauth2Config.getClientId());
				item.put("redirectUrl", oauth2Config.getRedirectUrl());
				item.put("scopes", oauth2Config.getScopes());

			}

            monitor.stop();
            return Response.ok(Map.of("items", List.of(item))).build();
        } catch (Exception e) {
            logger.error("Unexpected error loading login configuration", e);
            monitor.stop(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Unable to load login configuration"))
                    .build();
        }
    }

    @PUT
    @Path("/branding")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveBranding(Map<String, String> body) {
        UserProfile profile = getUserProfile();
        if (profile == null || !Boolean.TRUE.equals(profile.getIsSuperadmin())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        final Map<String, String> branding;
        try {
            branding = LoginBranding.normalize(body);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            IConfigDAO dao = DAOFactory.getSbiConfigDAO();
            dao.setUserProfile(profile);
            Config config = dao.loadConfigParametersByLabelIfExist(LoginBranding.CONFIG_LABEL).orElseGet(Config::new);
            config.setLabel(LoginBranding.CONFIG_LABEL);
            config.setName("Login page branding");
            config.setCategory("LOGIN");
            config.setActive(true);
            config.setValueCheck(new JSONObject(branding).toString());
            dao.saveConfig(config);
            return Response.ok(branding).build();
        } catch (Exception e) {
            logger.error("Unable to save login branding configuration", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Unable to save login branding configuration")).build();
        }
    }

    @POST
    @Path("/images")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadBrandingImage(@Context HttpServletRequest req) {
        UserProfile profile = getUserProfile();
        if (profile == null || !Boolean.TRUE.equals(profile.getIsSuperadmin())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        List<FileItem> items = null;
        try {
            ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
            upload.setFileSizeMax(LoginBrandingImages.MAX_BYTES);
            upload.setSizeMax(LoginBrandingImages.MAX_BYTES + 65536L);
            items = upload.parseRequest(req);
            FileItem image = null;
            for (FileItem item : items) {
                if (!item.isFormField()) {
                    if (image != null || !"file".equals(item.getFieldName())) {
                        throw new IllegalArgumentException("Upload exactly one image in the file field");
                    }
                    image = item;
                }
            }
            if (image == null) throw new IllegalArgumentException("No image was uploaded");
            String name = brandingImages().save(image.get(), image.getName());
            return Response.ok(Map.of("url", req.getContextPath() + "/restful-services/loginconfig/images/" + name)).build();
        } catch (FileUploadBase.SizeLimitExceededException | FileUploadBase.FileSizeLimitExceededException e) {
            return Response.status(413).entity(Map.of("error", "Maximum image size is 5 MB")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            logger.error("Unable to upload login branding image", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", "Unable to upload login image")).build();
        } finally {
            if (items != null) {
                for (FileItem item : items) item.delete();
            }
        }
    }

    @GET
    @Path("/images/{name}")
    @PublicService
    public Response getBrandingImage(@PathParam("name") String name) {
        try {
            java.nio.file.Path file = brandingImages().find(name);
            if (file == null) return Response.status(Response.Status.NOT_FOUND).build();
            return Response.ok(Files.readAllBytes(file), LoginBrandingImages.mediaType(name))
                    .header("Cache-Control", "public, max-age=31536000, immutable")
                    .header("X-Content-Type-Options", "nosniff")
                    .header("Content-Security-Policy", "sandbox; default-src 'none'; style-src 'unsafe-inline'")
                    .build();
        } catch (Exception e) {
            logger.error("Unable to load login branding image", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private LoginBrandingImages brandingImages() {
        return new LoginBrandingImages(Paths.get(SpagoBIUtilities.getRootResourcePath(), "login-branding"));
    }
}