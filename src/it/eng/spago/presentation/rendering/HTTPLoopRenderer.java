/* SpagoBI, the Open Source Business Intelligence suite

* © 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this file,
* You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spago.presentation.rendering;

import it.eng.spago.base.Constants;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.service.RequestContextIFace;
import it.eng.spago.navigation.LightNavigationManager;
import it.eng.spago.navigation.Navigator;
import it.eng.spago.presentation.PublisherConfiguration;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spago.util.ContextScooping;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * This implementation is specific for HTTP channel.
 */
public class HTTPLoopRenderer extends AbstractServletModelRenderer implements RenderIFace {
	
	private static SourceBean itemSourceBean = null;
	
	/* (non-Javadoc)
	 * @see it.eng.spago.view.AbstractServletModelRenderer#prepareRender(it.eng.spago.dispatching.service.RequestContextIFace, it.eng.spago.presentation.PublisherConfiguration, java.lang.Object)
	 */
	public void prepareRender(RequestContextIFace requestContext, PublisherConfiguration publisher, Object additionalResources) throws Exception {
		super.prepareRender(requestContext, publisher, additionalResources);

		RequestContainer requestContainer = requestContext.getRequestContainer();
		ResponseContainer responseContainer = requestContext.getResponseContainer();

		// Prepare service request for loopback management
        try {
            SourceBean loopbackServiceRequest = new SourceBean(Constants.SERVICE_REQUEST);
            loopbackServiceRequest.setAttribute(Navigator.NAVIGATOR_DISABLED, "TRUE");
            
            SourceBean renderingConfig = publisher.getRenderingConfig();
            List resourcesConfig = renderingConfig.getAttributeAsList("RESOURCES.PARAMETER");
            for (int j = 0; j < resourcesConfig.size(); j++) {
                SourceBean consequence = (SourceBean) resourcesConfig.get(j);
                String parameterName = (String) consequence.getAttribute("NAME");
                String parameterScope = (String) consequence.getAttribute("SCOPE");
                String parameterType = (String) consequence.getAttribute("TYPE");
                String parameterValue = (String) consequence.getAttribute("VALUE");
                Object inParameterValue = null;
                if (parameterType.equalsIgnoreCase("ABSOLUTE"))
                    inParameterValue = parameterValue;
                else {
                	inParameterValue = ContextScooping.getScopedParameter(requestContainer,
                    						responseContainer, 
                    						parameterValue, parameterScope, consequence);
                }
                
                if (inParameterValue == null)
                    continue;
                if (inParameterValue instanceof SourceBean)
                    loopbackServiceRequest.setAttribute((SourceBean) inParameterValue);
                else
                    loopbackServiceRequest.setAttribute(parameterName, inParameterValue);
            } // for (int j = 0; j < consequences.size(); j++)
            // light navigator id propagation
            SourceBean serviceRequest = requestContainer.getServiceRequest();
            String lightNavigatorId = (String) serviceRequest.getAttribute(LightNavigationManager.LIGHT_NAVIGATOR_ID);
            if (lightNavigatorId != null && !lightNavigatorId.trim().equals("")) {
            	loopbackServiceRequest.setAttribute(LightNavigationManager.LIGHT_NAVIGATOR_ID, lightNavigatorId);
            }
            
            responseContainer.setLoopbackServiceRequest(loopbackServiceRequest);
        } // try
        catch (SourceBeanException sbe) {
            TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL,
                    "Publisher::getPublisher:", sbe);
        } // catch (SourceBeanException sbe)
	}

	/* (non-Javadoc)
	 * @see it.eng.spago.view.AbstractServletModelRenderer#render(it.eng.spago.dispatching.service.RequestContextIFace, it.eng.spago.presentation.PublisherConfiguration, java.lang.Object)
	 */
	public void render(RequestContextIFace requestContext, PublisherConfiguration publisher, Object additionalResources)
			throws Exception {
		RequestContainer requestContainer = requestContext.getRequestContainer();
		HttpServletRequest request = (HttpServletRequest)requestContainer.getInternalRequest();
		
		// Allow to recognize that we are executing a loopback
    	request.setAttribute(Constants.PUBLISHING_MODE_LOOPBACK, "TRUE");
        
        // The resource list should be empty
        if (publisher.getResources().size() != 0) {
            TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING,
                    "LoopRenderer::render: Resource list not empty");
        }
        // This code allows to avoid, for LOOP mode, to define in the configuration
        // file the resource AdapterHTTP, but only to define the service to invoke
        publisher.addResource(itemSourceBean);
        
        // Use servlet model renderer (with forward or redirect)
        super.render(requestContext, publisher, additionalResources);
	}
	
	static {
		// For performance reasons build only once this SourceBean
		try {
	        // Build SourceBean ITEM containing the attribute resource="/servlet/AdapterHTTP"
	        itemSourceBean = new SourceBean("ITEM");
	        itemSourceBean.setAttribute("prog", "0");
	        itemSourceBean.setAttribute("resource", "/servlet/AdapterHTTP");
		} catch (SourceBeanException sbe) {
            TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL,
                    "LoopRenderer::static initializer:", sbe);
		}
	}
	
}
