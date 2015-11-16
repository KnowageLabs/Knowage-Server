/* SpagoBI, the Open Source Business Intelligence suite

* © 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this file,
* You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spago.dispatching.httpchannel;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.eng.spago.base.Constants;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spago.util.JavaScript;

public class Router {
    public Router(String publisher) {
        this(publisher, true);
    } // public Router(String publisher)

    public Router(String publisher, boolean isForward) {
        _publisher = publisher;
        _isForward = isForward;
        _parameters = new Hashtable();
    } // public Router(String publisher, boolean isForward)

    public static Router getDefaultRouter() {
        return new Router(Constants.DEFAULT_PUBLISHER);
    } // public static ActionRouter getDefaultRouter()

    public Object getParameter(String key) {
        if (key == null)
            return null;
        return _parameters.get(key);
    } // public Object getParameter(String key)

    public void setParameter(String key, Object value) {
        if (key == null)
            return;
        if (value == null)
            delParameter(key);
        else
            _parameters.put(key, value);
    } // public void setParameter(String key, Object value)

    public void delParameter(String key) {
        if (key == null)
            return;
        _parameters.remove(key);
    } // public void delParameter(String key)

    public void route(
        ServletContext servletContext,
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException, IOException {
        TracerSingleton.log(
            Constants.NOME_MODULO,
            TracerSingleton.DEBUG,
            "Router::route: request.getContextPath() ["
                + request.getContextPath()
                + "]");
        TracerSingleton.log(
            Constants.NOME_MODULO,
            TracerSingleton.DEBUG,
            "Router::Router: _publisher [" + _publisher + "]");
        TracerSingleton.log(
            Constants.NOME_MODULO,
            TracerSingleton.DEBUG,
            "Router::Router: _isForward [" + _isForward + "]");
        String publishingParameters = "";
        Enumeration parameterNames = _parameters.keys();
        while (parameterNames.hasMoreElements()) {
            String parameterName = (String) parameterNames.nextElement();
            String parameterValue =
                String.valueOf(_parameters.get(parameterName));
            if (_publisher.indexOf(parameterName) == -1)
                publishingParameters += JavaScript.escape(parameterName)
                    + "="
                    + JavaScript.escape(parameterValue)
                    + "&";
        } // while (parameterNames.hasMoreElements())
        TracerSingleton.log(
            Constants.NOME_MODULO,
            TracerSingleton.DEBUG,
            "Router::route: publishingParameters ["
                + publishingParameters
                + "]");
        String publishingURL = _publisher;
        ConfigSingleton configure = ConfigSingleton.getInstance();
        String appendContextRoot =
            (String) configure.getAttribute("PUBLISHING.APPEND_CONTEXT_ROOT");
        if ((appendContextRoot == null)
            || (appendContextRoot.equalsIgnoreCase("TRUE")))
            if (_publisher.startsWith("/")) {
                TracerSingleton.log(
                    Constants.NOME_MODULO,
                    TracerSingleton.DEBUG,
                    "Router::route: publisher assoluto");
                if (_isForward)
                    publishingURL = _publisher;
                else
                    publishingURL = request.getContextPath() + _publisher;
            } // if (_publisher.startsWith("/"))
        else {
            TracerSingleton.log(
                Constants.NOME_MODULO,
                TracerSingleton.DEBUG,
                "Router::route: publisher relativo");
            publishingURL = _publisher;
        } // if (_publisher.startsWith("/")) else
        if (publishingParameters!=null && publishingParameters.length()>0) {
            if (_publisher.indexOf('?') == -1)
                publishingURL += "?" + publishingParameters;
            else
                publishingURL += "&" + publishingParameters;
        }
        TracerSingleton.log(
            Constants.NOME_MODULO,
            TracerSingleton.DEBUG,
            "Router::route: publishingURL [" + publishingURL + "]");
        if (_isForward) {
            RequestDispatcher requestDispatcher =
                servletContext.getRequestDispatcher(publishingURL);
            requestDispatcher.forward(request, response);
        } // if (_isForward)
        else {
            response.sendRedirect(response.encodeRedirectURL(publishingURL));
        } // if (_isForward) else
    } // public void route(ServletContext servletContext, HttpServletRequest request,

    // HttpServletResponse response) throws ServletException, IOException
    private String _publisher = null;
    private boolean _isForward = false;
    private Hashtable _parameters = null;
} // public class ActionRouter
