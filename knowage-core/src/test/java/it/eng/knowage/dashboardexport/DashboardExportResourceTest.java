package it.eng.knowage.dashboardexport;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class DashboardExportResourceTest {

    @Test
    public void shouldResolveDescriptionPlaceholderFromDashboardDrivers() throws Exception {
        JSONArray drivers = new JSONArray()
                .put(new JSONObject()
                        .put("urlName", "country")
                        .put("value", "IT")
                        .put("description", "Italy"));

        String resolved = invokeResolveDashboardExportFileName("dashboard-$P{country_description}", "dashboard", drivers, new JSONArray());

        assertEquals("dashboard-Italy", resolved);
    }

    @Test
    public void shouldResolveDescriptionPlaceholderFromPdfStructuredParameters() throws Exception {
        JSONArray parameters = new JSONArray()
                .put(new JSONObject()
                        .put("urlName", "country")
                        .put("value", new JSONArray().put(new JSONObject().put("value", "IT").put("description", "Italy"))));

        String resolved = invokeResolveDashboardExportFileName("dashboard-$P{country_description}", "dashboard", parameters, new JSONArray());

        assertEquals("dashboard-Italy", resolved);
    }

    private String invokeResolveDashboardExportFileName(String exportFileNameTemplate, String defaultFileName, JSONArray drivers, JSONArray variables) throws Exception {
        Method resolveMethod = DashboardExportResource.class.getDeclaredMethod("resolveDashboardExportFileName", String.class, String.class, JSONArray.class, JSONArray.class);
        resolveMethod.setAccessible(true);
        return (String) resolveMethod.invoke(null, exportFileNameTemplate, defaultFileName, drivers, variables);
    }
}
