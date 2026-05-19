package it.eng.knowage.engine.api.export.dashboard.excel;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DashboardExcelExporterTest {

    @Test
    public void shouldKeepXlsxStyleEnabledByDefaultForSingleWidgetExports() {
        assertTrue(DashboardExcelExporter.isXlsxStyleEnabled(new JSONObject(), true));
    }

    @Test
    public void shouldReadXlsxStyleEnabledFromSingleWidgetRoot() throws JSONException {
        JSONObject body = new JSONObject().put("xlsxStyleEnabled", false);

        assertFalse(DashboardExcelExporter.isXlsxStyleEnabled(body, true));
    }

    @Test
    public void shouldKeepXlsxStyleEnabledByDefaultForDashboardExports() {
        assertTrue(DashboardExcelExporter.isXlsxStyleEnabled(new JSONObject(), false));
    }

    @Test
    public void shouldReadXlsxStyleEnabledFromDashboardMenuWidgetsConfiguration() throws JSONException {
        JSONObject body = new JSONObject()
                .put("configuration", new JSONObject()
                        .put("menuWidgets", new JSONObject()
                                .put("xlsxStyleEnabled", false)));

        assertFalse(DashboardExcelExporter.isXlsxStyleEnabled(body, false));
    }
}
