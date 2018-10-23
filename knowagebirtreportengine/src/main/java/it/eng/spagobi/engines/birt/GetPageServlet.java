package it.eng.spagobi.engines.birt;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.eng.spagobi.engines.birt.utilities.Utils;

public class GetPageServlet extends HttpServlet {

	public static final String PAGE_NUMBER = "page";
	public static final String REPORT_EXECUTION_ID = "report_execution_id";

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) {
		int pageNumber = Integer.parseInt(request.getParameter(PAGE_NUMBER));
		String reportExecutionId = request.getParameter(REPORT_EXECUTION_ID);

		// check reportExecutionID is alfanumeric
		String pattern = "^[a-zA-Z0-9]*$";
		if (!reportExecutionId.matches(pattern)) {
			throw new RuntimeException("Security Exception: Report Execution Id passed to servlet[" + reportExecutionId + "] is not alfanumeric.");
		}

		Utils.sendPage(response, pageNumber, reportExecutionId);
	}
}
