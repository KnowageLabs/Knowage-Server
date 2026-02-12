package it.eng.knowage.dashboardexport;

import com.google.common.collect.Iterables;
import it.eng.knowage.commons.multitenant.OrganizationImageManager;
import it.eng.knowage.engine.api.export.dashboard.excel.DashboardExcelExporter;
import it.eng.knowage.engine.api.export.dashboard.pdf.DashboardPdfExporter;
import it.eng.knowage.engine.api.export.nodejs.PdfExporterV2;
import it.eng.knowage.export.wrapper.beans.RenderOptions;
import it.eng.knowage.export.wrapper.beans.ViewportDimensions;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.mime.MimeUtils;
import it.eng.spagobi.utilities.rest.RestUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.*;

import static it.eng.knowage.commons.security.KnowageSystemConfiguration.getKnowageVueContext;
import static java.nio.charset.StandardCharsets.UTF_8;

@Path("/1.0/dashboardExport")
public class DashboardExportResource {

    protected static Logger logger = Logger.getLogger(DashboardExportResource.class);

    private static final String OUTPUT_TYPE = "outputType";
    private static final String PDF_FRONT_PAGE = "pdfFrontPage";
    private static final String PDF_PAGE_ORIENTATION = "pdfPageOrientation";
    private static final String PDF_BACK_PAGE = "pdfBackPage";
    private static final String PDF_WIDTH = "pdfWidth";
    private static final String PDF_HEIGHT = "pdfHeight";
    private static final String PDF_DEVICE_SCALE_FACTOR = "pdfDeviceScaleFactor";
    private static final String PDF_WAIT_TIME = "pdfWaitTime";
    private static final String IS_MULTI_SHEET = "isMultiSheet";

    @Context
    protected HttpServletRequest request;
    @Context
    protected HttpServletResponse response;

    private static final String TOKEN_HEADER = "x-kn-authorization";

    private final Base64.Encoder base64Encoder = Base64.getEncoder().withoutPadding();

    @POST
    @Path("/spreadsheet")
    public void downloadExcel(@Context HttpServletRequest req) {
        logger.debug("IN");
        response.setCharacterEncoding(UTF_8.name());
        try {
            JSONObject body = RestUtilities.readBodyAsJSONObject(req);
            String token = request.getHeader(TOKEN_HEADER);
            String userId = token.substring(7);
            DashboardExcelExporter excelExporter = new DashboardExcelExporter(userId, body, OrganizationImageManager.getOrganizationB64ImageWide(TenantManager.getTenant().getName()));
            String mimeType = excelExporter.getMimeType();

            if (!MimeUtils.isValidMimeType(mimeType))
                throw new SpagoBIRuntimeException("Invalid mime type: " + mimeType);

            if (mimeType != null) {
                byte[] data;
                boolean isDashboardSingleWidgetExport = !body.has("widgets");
                if (isDashboardSingleWidgetExport && body.getString("type").equals("static-pivot-table")) {
                    data = excelExporter.getPivotBinaryData(body);
                } else {
                    data = excelExporter.getDashboardBinaryData(body, isDashboardSingleWidgetExport);
                }
                if (!isDashboardSingleWidgetExport) {
                    String documentLabel = body.getJSONObject("document").getString("label");
                    response.setHeader("Content-Disposition", "attachment; fileName=" + documentLabel + ".xlsx");
                } else {
                    String widgetName = body.getJSONObject("settings").getJSONObject("style").getJSONObject("title")
                            .optString("text");
                    widgetName = getWidgetName(widgetName, body);
                    response.setHeader("Content-Disposition", "attachment; fileName=" + widgetName + "." + "xlsx");
                }
                populateResponse(mimeType, data);
            }
        } catch (Exception e) {
            logger.error("Cannot export to Excel", e);
            throw new SpagoBIRuntimeException("Cannot export to Excel", e);
        } finally {
            logger.debug("OUT");
        }
    }

    private static String getWidgetName(String widgetName, JSONObject body) throws JSONException {

        if (widgetName == null || widgetName.trim().isEmpty()) {
            switch (body.getString("type")) {
                case "highcharts":
                    widgetName = "chart";
                    break;
                case "static-pivot-table":
                    widgetName = "pivot";
                    break;
                case "map":
                    widgetName = "map";
                    break;
                case "table":
                    widgetName = "table";
                    break;
                case "customchart":
                    widgetName = "custom_chart";
                    break;
                default:
                    widgetName = "widget";
            }
        }
        return widgetName;
    }

    @POST
    @Path("/pdf")
    public void downloadPdf(@Context HttpServletRequest req) {
        logger.debug("IN");
        response.setCharacterEncoding(UTF_8.name());
        Locale locale = req.getLocale();
        try {
            JSONObject body = RestUtilities.readBodyAsJSONObject(req);
            String token = request.getHeader(TOKEN_HEADER);
            String userId = token.substring(7);
            DashboardPdfExporter dashboardPdfExporter = new DashboardPdfExporter(userId, locale);
            String mimeType = dashboardPdfExporter.getMimeType();

            if (!MimeUtils.isValidMimeType(mimeType))
                throw new SpagoBIRuntimeException("Invalid mime type: " + mimeType);

            if (mimeType != null) {
                byte[] data;
                data = dashboardPdfExporter.getBinaryData(body);
                String widgetName = body.getJSONObject("settings").getJSONObject("style").getJSONObject("title")
                        .optString("text");
                if (widgetName != null && widgetName.startsWith("$P{")) {
                    // Extract the value between {} - e.g., "$P{country}" -> "country"
                    int startIndex = widgetName.indexOf('{') + 1;
                    int endIndex = widgetName.indexOf('}');
                    String placeholderToReplace = (startIndex > 0 && endIndex > startIndex)
                            ? widgetName.substring(startIndex, endIndex)
                            : "";
                    String remainingPart = widgetName.substring(endIndex + 1);
                    try {
                        for (int i = 0; i < body.optJSONArray("drivers").length(); i++) {
                            JSONObject driver = body.optJSONArray("drivers").getJSONObject(i);
                            if (driver.getString("urlName").equals(placeholderToReplace)) {
                                widgetName = driver.getString("value") + remainingPart;
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                response.setHeader("Content-Disposition", "attachment; fileName=" + widgetName + "." + "pdf");
                populateResponse(mimeType, data);
            }
        } catch (Exception e) {
            logger.error("Cannot export to PDF", e);
            throw new SpagoBIRuntimeException("Cannot export to PDF", e);
        } finally {
            logger.debug("OUT");
        }
    }

    private void populateResponse(String mimeType, byte[] data) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        response.setHeader("Content-length", Integer.toString(data.length));
        response.setHeader("Content-Type", mimeType);

        response.getOutputStream().write(data, 0, data.length);
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }

    @GET
    @Path("/callPuppeteer")
    public Object callPuppeteer() {
        try {
            response.setContentType(MediaType.TEXT_HTML);
            response.setCharacterEncoding(UTF_8.name());

            String outputType = request.getParameter(OUTPUT_TYPE);

            String documentLabel = request.getParameter("DOCUMENT_LABEL");
            BIObject biObject;
            try {
                biObject = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(documentLabel);
            } catch (EMFUserError e) {
                throw new SpagoBIRuntimeException("Error retrieving document with label " + documentLabel, e);
            }
            Engine eng = biObject.getEngine();
            URIBuilder externalUrl = GeneralUtilities.getBE2BEEngineUrl(eng);

            if ("xls".equalsIgnoreCase(outputType) || "xlsx".equalsIgnoreCase(outputType)) {
                return createSpreadsheet(biObject, documentLabel, externalUrl);
            } else if ("pdf".equalsIgnoreCase(outputType)) {
                return createPdf(biObject, documentLabel, externalUrl);
            } else {
                throw new SpagoBIRuntimeException("Unsupported output type: " + outputType);
            }
        } catch (Exception e) {
            logger.error("Error generating file", e);
            throw new SpagoBIRuntimeException("Error generating file", e);
        } finally {
            logger.debug("OUT");
        }
    }

    private Object createSpreadsheet(BIObject biObject, String documentLabel, URIBuilder externalUrl) throws IOException, InterruptedException, JSONException {

        String requestURL = getRequestUrl(biObject, documentLabel, externalUrl, true);

        String role = getProfileRole();

        String organization = UserProfileManager.getProfile().getOrganization();

        String userId = request.getParameter("user_id");

        DashboardExcelExporter excelExporter = new DashboardExcelExporter(new JSONObject(biObject.getActiveTemplate()), role, requestURL, organization, userId, OrganizationImageManager.getOrganizationB64ImageWide(TenantManager.getTenant().getName()));
        String mimeType = excelExporter.getMimeType();
        byte[] data = excelExporter.getScheduledBinaryData(documentLabel);

        return Response.ok(data, mimeType).header("Content-length", Integer.toString(data.length))
                .header("Content-Disposition", "attachment; fileName=" + documentLabel + ".xlsx").build();
    }

    private Object createPdf(BIObject biObject, String documentLabel, URIBuilder externalUrl) throws JSONException, EMFUserError, IOException, InterruptedException {

        String requestURL = getRequestUrl(biObject, documentLabel, externalUrl, false);

        String role = getProfileRole();
        String organization = UserProfileManager.getProfile().getOrganization();

        RenderOptions renderOptions = getRenderOptionsForPdfExporter(request);

        int documentId = Integer.parseInt(request.getParameter("document"));
        String userId = request.getParameter("user_id");
        String pdfPageOrientation = request.getParameter(PDF_PAGE_ORIENTATION);
        boolean pdfFrontPage = Boolean.parseBoolean(request.getParameter(PDF_FRONT_PAGE));
        boolean pdfBackPage = Boolean.parseBoolean(request.getParameter(PDF_BACK_PAGE));
        String params = request.getParameter("parameters");

        PdfExporterV2 pdfExporter = new PdfExporterV2(documentId, userId, requestURL, renderOptions, pdfPageOrientation,
                pdfFrontPage, pdfBackPage, role, organization, params);
        byte[] data = pdfExporter.getBinaryData();

        return Response.ok(data, "application/pdf").header("Content-Length", Integer.toString(data.length))
                .header("Content-Disposition",
                        "attachment; fileName=" + request.getParameter("DOCUMENT_LABEL") + ".pdf")
                .build();
    }

    private String getRequestUrl(BIObject biObject, String documentLabel, URIBuilder externalUrl, boolean isXlsx) throws JSONException {

        manageParameters(biObject, documentLabel, externalUrl);

        if (isXlsx) {
            externalUrl.setParameter("scheduledexport", "true");
        } else {
            externalUrl.setParameter("export", "true");
        }
        return externalUrl.toString();
    }


    private void manageParameters(BIObject biObject, String documentLabel, URIBuilder uriBuilder)
            throws JSONException {

        uriBuilder.setPath(getKnowageVueContext() + "/dashboard/" + documentLabel);
        uriBuilder.setParameter("params", createJsonFromParameters(biObject));
        uriBuilder.setParameter("role", getExecutionRoleForDashboard());
        addParametersToHideToolbarAndMenuInVue(uriBuilder);
    }


    private String createJsonFromParameters(BIObject biObject) throws JSONException {
        List<BIObjectParameter> drivers = biObject.getDrivers();
        // We wrap parameters map because it could be updated here below
        Map<String, String[]> parameterMap = new HashMap<>(request.getParameterMap());
        JSONArray parametersAsJson = new JSONArray();

        reconcileParametersWithParamsV2FromUrl(parameterMap);

        for (BIObjectParameter driver : drivers) {
            String urlName = driver.getParameterUrlName();

            boolean isMultivalue = driver.isMultivalue();

            List<String> values = Optional.ofNullable(parameterMap.get(urlName)).map(Arrays::asList)
                    .orElse(Collections.emptyList());
            List<String> descriptions = Optional.ofNullable(parameterMap.get(urlName + "_description"))
                    .map(Arrays::asList).orElse(Collections.emptyList());

            List<String> splitValues = new ArrayList<>();
            if (biObject.getEngine().getLabel().equals("knowagedashboardengine")) {
                List<String> finalSplitValues = splitValues;
                values.forEach(v -> finalSplitValues.addAll(Arrays.asList(v.split(";"))));
            }

            if (OUTPUT_TYPE.equals(urlName)) {
                logger.debug("Forcing outputType to HTML");
                splitValues = List.of("HTML");
                descriptions = List.of("HTML");
            }

            JSONObject currentDriverJson = new JSONObject();

            JSONArray valuesAsJSONArray = new JSONArray();

            for (int i = 0; i < Math.max(splitValues.size(), descriptions.size()); i++) {
                Object value = Iterables.get(splitValues, i, "");
                Object description = Iterables.get(descriptions, i, "");

                JSONObject currValue = new JSONObject();

                currValue.put("value", value);
                currValue.put("description", description);

                valuesAsJSONArray.put(currValue);
            }

            currentDriverJson.put("value", valuesAsJSONArray);
            currentDriverJson.put("urlName", urlName);
            currentDriverJson.put("multivalue", isMultivalue);

            parametersAsJson.put(currentDriverJson);
        }

        String parametersAsString = parametersAsJson.toString();
        return java.util.Base64.getEncoder().withoutPadding().encodeToString(parametersAsString.getBytes());
    }


    private String getExecutionRoleForDashboard() {
        Map<String, String[]> parameterMap = request.getParameterMap();
        String role = Optional.ofNullable(parameterMap.get("SBI_EXECUTION_ROLE")).map(e -> e[0]).orElse("");
        if (StringUtils.isEmpty(role)) {
            role = Optional.ofNullable(parameterMap.get("role")).map(e -> e[0]).orElse("");
        }
        return role;
    }

    private void reconcileParametersWithParamsV2FromUrl(Map<String, String[]> parameterMap) throws JSONException {
        // Manage new parameters format in Base64
        String parametersV2FromUrl = Optional.ofNullable(request.getParameter("params"))
                .map(e -> new String(java.util.Base64.getDecoder().decode(e))).orElse("[]");
        JSONArray parametersV2FromUrlAsJSONArray = new JSONArray(parametersV2FromUrl);
        for (int i = 0; i < parametersV2FromUrlAsJSONArray.length(); i++) {
            JSONObject currParameterFromParametersV2 = (JSONObject) parametersV2FromUrlAsJSONArray.get(i);

            String urlName = currParameterFromParametersV2.getString("urlName");

            if (!parameterMap.containsKey(urlName)) {
                List<String> values = new ArrayList<>();
                List<String> descriptions = new ArrayList<>();

                JSONArray value = currParameterFromParametersV2.getJSONArray("value");

                for (int k = 0; k < value.length(); k++) {
                    JSONObject currentParameterValue = (JSONObject) value.get(k);

                    String cValue = currentParameterValue.getString("value");
                    String cDesc = currentParameterValue.getString("description");

                    values.add(cValue);
                    descriptions.add(cDesc);
                }

                parameterMap.put(urlName, values.toArray(new String[0]));
                parameterMap.put(urlName + "_description", descriptions.toArray(new String[0]));
            }
        }
    }

    private void addParametersToHideToolbarAndMenuInVue(URIBuilder uriBuilder) {
        uriBuilder.setParameter("toolbar", "false");
        uriBuilder.setParameter("menu", "false");
        uriBuilder.setParameter("finalUser", "true");
    }

    private String getProfileRole() {

        if (request.getParameter("SBI_EXECUTION_ROLE") != null) {
            return request.getParameter("SBI_EXECUTION_ROLE");
        }

        IEngUserProfile profile = (IEngUserProfile) request.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
        Collection<String> roles;
        try {
            roles = profile.getRoles();
        } catch (EMFInternalError emfE) {
            logger.error("Error retrieving user roles", emfE);
            throw new SpagoBIRuntimeException("Error retrieving user roles", emfE);
        }

        if (roles.size() != 1) {
            logger.error("User has more than one role, cannot export to excel");
            throw new SpagoBIRuntimeException("User has more than one role, cannot export to excel");
        }

        return Iterables.get(roles, 0);
    }
    private RenderOptions getRenderOptionsForPdfExporter(HttpServletRequest request) {
        String userId = (String) UserProfileManager.getProfile().getUserUniqueIdentifier();
        String encodedUserId = new String(base64Encoder.encode(userId.getBytes(UTF_8)));
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", "Direct " + encodedUserId);

        RenderOptions defaultRenderOptions = RenderOptions.defaultOptions();
        ViewportDimensions defaultDimensions = defaultRenderOptions.getDimensions();
        long defaultJsRenderingWait = defaultRenderOptions.getJsRenderingWait();
        int pdfWidth = Integer.parseInt(defaultDimensions.getWidth());
        int pdfHeight = Integer.parseInt(defaultDimensions.getHeight());
        double pdfDeviceScaleFactor = Double.parseDouble(defaultDimensions.getDeviceScaleFactor());
        long pdfRenderingWaitTime = defaultJsRenderingWait;

        String widthParameterVal = request.getParameter(PDF_WIDTH);
        String heightParameterVal = request.getParameter(PDF_HEIGHT);
        String deviceScaleFactorVal = request.getParameter(PDF_DEVICE_SCALE_FACTOR);
        String jsRenderingWaitParameterVal = request.getParameter(PDF_WAIT_TIME);

        boolean isMultiSheet = Boolean.parseBoolean(request.getParameter(IS_MULTI_SHEET));

        if (widthParameterVal != null) {
            pdfWidth = Integer.parseInt(widthParameterVal);
        }
        if (heightParameterVal != null) {
            pdfHeight = Integer.parseInt(heightParameterVal);
        }
        if (jsRenderingWaitParameterVal != null) {
            pdfRenderingWaitTime = 1000 * Long.parseLong(jsRenderingWaitParameterVal);
        }

        if (deviceScaleFactorVal != null) {
            pdfDeviceScaleFactor = Double.parseDouble(deviceScaleFactorVal);
        }

        ViewportDimensions dimensions = ViewportDimensions.builder().withWidth(pdfWidth).withHeight(pdfHeight)
                .withDeviceScaleFactor(pdfDeviceScaleFactor).withIsMultiSheet(isMultiSheet).build();
        return defaultRenderOptions.withDimensions(dimensions).withJavaScriptExecutionDetails(pdfRenderingWaitTime,
                5000L);
    }

}
