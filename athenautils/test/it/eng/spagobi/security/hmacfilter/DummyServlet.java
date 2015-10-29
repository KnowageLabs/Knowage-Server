package it.eng.spagobi.security.hmacfilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * public for Jetty
 *
 * @author fabrizio
 *
 */
@SuppressWarnings("serial")
public class DummyServlet extends HttpServlet {

	public static boolean arrived;
	public static byte[] body;

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		arrived = true;
		ServletInputStream stream = req.getInputStream();
		int c;
		List<Byte> bytes = new ArrayList<Byte>();
		while ((c = stream.read()) != -1) {
			bytes.add((byte) c);
		}
		body = new byte[bytes.size()];
		for (int i = 0; i < body.length; i++) {
			body[i] = bytes.get(i);
		}
	}
}