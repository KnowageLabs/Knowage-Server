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
package it.eng.spagobi.filters;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.validator.UrlValidator;
import org.apache.log4j.Logger;

import it.eng.spagobi.utilities.WhiteList;

public class XSSRequestWrapper extends HttpServletRequestWrapper {

	private static transient Logger logger = Logger.getLogger(XSSRequestWrapper.class);
	private static WhiteList whitelist = WhiteList.getInstance();

	public XSSRequestWrapper(HttpServletRequest servletRequest) {
		super(servletRequest);
	}

	@Override
	public String[] getParameterValues(String parameter) {
		String[] values = super.getParameterValues(parameter);

		if (values == null) {
			return null;
		}

		int count = values.length;
		String[] encodedValues = new String[count];
		for (int i = 0; i < count; i++) {
			encodedValues[i] = stripXSS(values[i]);
		}

		return encodedValues;
	}

	@Override
	public String getParameter(String parameter) {
		String value = super.getParameter(parameter);

		return stripXSS(value);
	}

	@Override
	public String getHeader(String name) {
		String value = super.getHeader(name);
		return stripXSS(value);
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {

		return super.getInputStream();
	}

	public static String stripXSS(String value) {
		logger.debug("IN");
		String initialValue = value;

		if (value != null) {
			// NOTE: It's highly recommended to use the ESAPI library and uncomment the following line to
			// avoid encoded attacks.
			// value = ESAPI.encoder().canonicalize(value);

			// Avoid null characters
			value = value.replaceAll("", "");

			// Avoid anything between script tags
			Pattern scriptPattern = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			scriptPattern = Pattern.compile("&lt;script&gt;(.*?)&lt;/script&gt;", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid anything in a src='...' type of expression
			// Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			// value = scriptPattern.matcher(value).replaceAll("");
			//
			// scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			// value = scriptPattern.matcher(value).replaceAll("");

			value = checkImgTags(value);
			value = checkIframeTags(value);
			value = checkAnchorTags(value);
			value = checkVideoTags(value);
			value = checkCSS(value);

			// Remove any lonesome </script> tag
			scriptPattern = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
			value = scriptPattern.matcher(value).replaceAll("");

			scriptPattern = Pattern.compile("&lt;/script&gt;", Pattern.CASE_INSENSITIVE);
			value = scriptPattern.matcher(value).replaceAll("");

			// Remove any lonesome <script ...> tag
			scriptPattern = Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			scriptPattern = Pattern.compile("&lt;script(.*?)&gt;", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid eval(...) expressions
			scriptPattern = Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid expression(...) expressions
			scriptPattern = Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid javascript:... expressions
			scriptPattern = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid vbscript:... expressions
			scriptPattern = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid onload= expressions
			scriptPattern = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid onClick= expressions
			scriptPattern = Pattern.compile("onClick(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid anything between form tags
			Pattern formPattern = Pattern.compile("<form(.*?)</form>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = formPattern.matcher(value).replaceAll("");

			// Avoid anything between a tags
			// Pattern aPattern = Pattern.compile("<a(.*?)</a>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			// value = aPattern.matcher(value).replaceAll("");

			// aPattern = Pattern.compile("<a(.*?/)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			// value = aPattern.matcher(value).replaceAll("");

			Pattern aPattern = Pattern.compile("&lt;a(.*?)&lt;/a&gt;", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = aPattern.matcher(value).replaceAll("");

			// Avoid anything between button tags
			Pattern buttonPattern = Pattern.compile("<button(.*?)</button>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = buttonPattern.matcher(value).replaceAll("");

			buttonPattern = Pattern.compile("<button(.*?/)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = buttonPattern.matcher(value).replaceAll("");

			buttonPattern = Pattern.compile("&lt;button(.*?)&lt;/button&gt;", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = buttonPattern.matcher(value).replaceAll("");

			// Example value ="<object data=\"javascript:alert('XSS')\"></object>"
			// Avoid anything between script tags
			Pattern objectPattern = Pattern.compile("<object(.*?)</object>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = objectPattern.matcher(value).replaceAll("");

			objectPattern = Pattern.compile("&lt;object(.*?)&lt;/object&gt;", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = objectPattern.matcher(value).replaceAll("");

			// Remove any lonesome </object> tag
			objectPattern = Pattern.compile("</object>", Pattern.CASE_INSENSITIVE);
			value = objectPattern.matcher(value).replaceAll("");

			objectPattern = Pattern.compile("&lt;/object&gt;", Pattern.CASE_INSENSITIVE);
			value = objectPattern.matcher(value).replaceAll("");

			// Remove any lonesome <object ...> tag
			objectPattern = Pattern.compile("<object(.*?/)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = objectPattern.matcher(value).replaceAll("");

			objectPattern = Pattern.compile("&lt;object(.*?/)&gt;", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = objectPattern.matcher(value).replaceAll("");

			if (!value.equalsIgnoreCase(initialValue)) {
				logger.warn("Message: detected a web attack through injection");
			}

		}

		logger.debug("OUT");
		return value;
	}

	private static String checkImgTags(String value) {
		logger.debug("IN");
		Pattern maliciousImgPattern = Pattern.compile("&lt;img(.*?)&gt;", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		value = maliciousImgPattern.matcher(value).replaceAll("");

		Pattern scriptPattern = Pattern.compile("<img[^>]+(src\\s*=\\s*['\"]([^'\"]+)['\"])[^>]*>",
				Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Pattern dataPattern = Pattern.compile("data:image\\/(gif|jpeg|pjpeg|webp|png|wmf|svg\\+xml|tiff|vnd\\.microsoft\\.icon);(utf-8;|utf8;)?base64",
				Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Matcher scriptMatcher = scriptPattern.matcher(value);

		while (scriptMatcher.find()) {
			String img = scriptMatcher.group();
			String link = scriptMatcher.group(2);

			Matcher dataMatcher = dataPattern.matcher(link);

			if (!dataMatcher.find()) {
				try {
					URL url = new URL(link);
					String baseUrl = url.getProtocol() + "://" + url.getHost();

					if (!whitelist.getExternalServices().contains(baseUrl)) {
						logger.warn("Provided image's src is: " + url + ". Image base url is not in Whitelist and therefore Image will be deleted");
						value = value.replace(img, "");
					}

				} catch (MalformedURLException e) {
					logger.debug("URL [" + link + "] is malformed. Trying to see if it is a valid relative URL...");
					if (isValidRelativeURL(link) && isTrustedRelativePath(link)) {
						logger.debug("URL " + link + " is recognized to be a valid URL");
					} else {
						logger.error("Malformed URL [" + link + "]", e);
						value = value.replace(img, "");
					}
				}

			}

		}

		logger.debug("OUT");
		return value;
	}

	private static String checkIframeTags(String value) {
		logger.debug("IN");
		Pattern maliciousTagPattern = Pattern.compile("&lt;iframe(.*?)iframe\\s*&gt;", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		value = maliciousTagPattern.matcher(value).replaceAll("");

		Pattern scriptPattern = Pattern.compile("<iframe[^>]*?(?:\\/>|>[^<]*?<\\/iframe>)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Matcher scriptMatcher = scriptPattern.matcher(value);

		while (scriptMatcher.find()) {
			String iframe = scriptMatcher.group();
			String s = "src=\"";
			int ix = iframe.indexOf(s) + s.length();
			String link = iframe.substring(ix, iframe.indexOf("\"", ix + 1));

			try {
				URL url = new URL(link);
				String baseUrl = url.getProtocol() + "://" + url.getHost();

				if (!whitelist.getExternalServices().contains(baseUrl)) {
					logger.warn("Provided iframe's src is: " + url + ". Iframe base url is not in Whitelist and therefore iframe will be deleted");
					value = value.replace(iframe, "");
				}

			} catch (MalformedURLException e) {
				logger.debug("URL [" + link + "] is malformed. Trying to see if it is a valid relative URL...");
				if (isValidRelativeURL(link) && isTrustedRelativePath(link)) {
					logger.debug("URL " + link + " is recognized to be a valid URL");
				} else {
					logger.error("Malformed URL [" + link + "]", e);
					value = value.replace(iframe, "");
				}
			}

		}

		logger.debug("OUT");
		return value;
	}

	private static String checkAnchorTags(String value) {
		logger.debug("IN");
		Pattern aPattern = Pattern.compile("<a([^>]+)>(.+?)</a>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Pattern hrefPattern = Pattern.compile("\\s*href\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

		Matcher aTagMatcher = aPattern.matcher(value);

		while (aTagMatcher.find()) {
			String aTag = aTagMatcher.group();
			String href = aTagMatcher.group(1);

			// In <a> tag find href attribute
			Matcher hrefMatcher = hrefPattern.matcher(href);

			while (hrefMatcher.find()) {
				String link = hrefMatcher.group(1);

				try {
					URL url = new URL(link);
					String baseUrl = url.getProtocol() + "://" + url.getHost();

					if (!whitelist.getExternalServices().contains(baseUrl)) {
						logger.warn("Provided anchor's href is: " + url + ". Anchor base url is not in Whitelist and therefore anchor will be deleted");
						value = value.replace(aTag, "");
					}

				} catch (MalformedURLException e) {
					logger.debug("URL [" + link + "] is malformed. Trying to see if it is a valid relative URL...");
					if (isValidRelativeURL(link) && isTrustedRelativePath(link)) {
						logger.debug("URL " + link + " is recognized to be a valid URL");
					} else {
						logger.error("Malformed URL [" + link + "]", e);
						value = value.replace(aTag, "");
					}
				}
			}

		}

		logger.debug("OUT");
		return value;
	}

	private static String checkVideoTags(String value) {
		logger.debug("IN");
		Pattern maliciousPattern = Pattern.compile("&lt;video(.*?)video&gt;", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		value = maliciousPattern.matcher(value).replaceAll("");

		Pattern scriptPattern = Pattern.compile("<video(.+?)</video\\s*>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Pattern srcAttributePattern = Pattern.compile("\\s*src\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Matcher matcher = scriptPattern.matcher(value);

		while (matcher.find()) {
			String video = matcher.group();
			String betweenVideoTags = matcher.group(1);

			Matcher srcMatcher = srcAttributePattern.matcher(betweenVideoTags);

			while (srcMatcher.find()) {
				String link = srcMatcher.group(1);

				try {
					URL url = new URL(link);
					String baseUrl = url.getProtocol() + "://" + url.getHost();

					if (!whitelist.getExternalServices().contains(baseUrl)) {
						logger.warn("Provided anchor's href is: " + url + ". Anchor base url is not in Whitelist and therefore anchor will be deleted");
						value = value.replace(video, "");
					}

				} catch (MalformedURLException e) {
					logger.debug("URL [" + link + "] is malformed. Trying to see if it is a valid relative URL...");
					if (isValidRelativeURL(link) && isTrustedRelativePath(link)) {
						logger.debug("URL " + link + " is recognized to be a valid URL");
					} else {
						logger.error("Malformed or untrusted URL [" + link + "]", e);
						value = value.replace(video, "");
					}
				}
			}

		}

		logger.debug("OUT");
		return value;
	}

	private static String checkCSS(String value) {
		logger.debug("IN");
		Pattern cssUrlPattern = Pattern.compile("url\\s*\\(['\"]?([^'\"\\)]+)['\"]?\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Pattern cssUrlDataPattern = Pattern.compile("data:image\\/(gif|jpeg|pjpeg|png|svg\\+xml|tiff|vnd\\.microsoft\\.icon);(utf-8;)?base64",
				Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Pattern domElementID = Pattern.compile("(#[a-zA-Z0-9\\_\\-]+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		String domId = "";

		Matcher urlMatcher = cssUrlPattern.matcher(value);

		while (urlMatcher.find()) {
			String cssUrl = urlMatcher.group();
			String link = urlMatcher.group(1);

			Matcher dataMatcher = cssUrlDataPattern.matcher(link);
			Matcher domIdMatcher = domElementID.matcher(link);

			if (domIdMatcher.find()) {
				domId = domIdMatcher.group();
				if (domId.length() > 50) {
					logger.warn("Provided url attribute with Id is: " + domId + ". Its lenght is grater than 50 characters and therefore it will be delete");
					value = value.replace(cssUrl, "");
				}
			}

			if (!dataMatcher.find()) {
				try {
					URL url = new URL(link);
					String baseUrl = url.getProtocol() + "://" + url.getHost();

					if (!whitelist.getExternalServices().contains(baseUrl)) {
						logger.warn("Provided CSS url attribute is: " + url + ". Base url is not in Whitelist and therefore it will be deleted");
						value = value.replace(cssUrl, "");
					}
				} catch (MalformedURLException e) {
					logger.debug("URL [" + link + "] is malformed. Trying to see if it is a valid relative URL...");
					if (isValidRelativeURL(link) && isTrustedRelativePath(link)) {
						logger.debug("URL " + link + " is recognized to be a valid URL");
					} else if (link.equals(domId)) {
						return value;
					} else {
						logger.error("Malformed or untrusted URL [" + link + "]", e);
						value = value.replace(cssUrl, "");
					}
				}
			}

		}

		logger.debug("OUT");
		return value;
	}

	private static boolean isValidRelativeURL(String url) {
		String absoluteUrl = "http://mynonexistingserver.something.smt:99999" + url;
		UrlValidator urlValidator = new UrlValidator();
		if (urlValidator.isValid(absoluteUrl)) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isTrustedRelativePath(String url) {
		List<String> relativePaths = whitelist.getRelativePaths();
		Iterator<String> it = relativePaths.iterator();

		while (it.hasNext()) {
			if (url.startsWith(it.next())) {
				return true;
			}
		}
		return false;
	}

}