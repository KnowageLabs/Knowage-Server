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
package it.eng.spagobi.commons.presentation.tags;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.commons.utilities.urls.IUrlBuilder;
import it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

public class InfoTag extends TagSupport {

	static private Logger logger = Logger.getLogger(InfoTag.class);
	private HttpServletRequest httpRequest = null;
	protected RequestContainer requestContainer = null;
	protected ResponseContainer responseContainer = null;
	protected IUrlBuilder urlBuilder = null;
	protected IMessageBuilder msgBuilder = null;
	private String fileName = null;
	private String infoTitle = null;
	private String buttonId = null;
	String readonly = "readonly";
	boolean isreadonly = true;
	String disabled = "disabled";

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
	 */
	public int doEndTag() throws JspException {
		logger.debug("");
		return super.doEndTag();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException{
		logger.debug("IN");
		httpRequest = (HttpServletRequest) pageContext.getRequest();
		requestContainer = ChannelUtilities.getRequestContainer(httpRequest);
		responseContainer = ChannelUtilities.getResponseContainer(httpRequest);
		urlBuilder = UrlBuilderFactory.getUrlBuilder();
		msgBuilder = MessageBuilderFactory.getMessageBuilder();
	
		StringBuffer output = new StringBuffer();
		
		output.append("<script>\n");
		output.append("Ext.onReady(function(){\n");
		output.append("    var win;\n");
		output.append("    var button = Ext.get('" + getButtonId() + "');\n");
		output.append("\n");
		output.append("button.on('click', function(){\n");
		output.append("        if(!win){\n");
		output.append("            win = new Ext.Window({\n");
		output.append("                applyTo     : '"+ getButtonId() +"content',\n");
		output.append("                layout      : 'fit',\n");
		output.append("                width       : 500,\n");
		output.append("                height      : 150,\n");
		output.append("                closeAction :'hide',\n");
		output.append("                plain       : true,\n");
		output.append("                items       : new Ext.TabPanel({\n");
		output.append("                    applyTo        : '" + getButtonId()+"body',\n");
		output.append("                    autoTabs       : true,\n");
		output.append("                    activeTab      : 0,\n");
		output.append("                    deferredRender : false,\n");
		output.append("                    border         : false\n");
		output.append("                }),\n");
		output.append("                buttons: [{\n");
		output.append("                    text     : 'Close',\n");
		output.append("                    handler  : function(){\n");
		output.append("                        win.hide();\n");
		output.append("                    }\n");
		output.append("                }]\n");
		output.append("            });\n");
		output.append("        }\n");
		output.append("        win.show(button);\n");
		output.append("    });\n");
		output.append("});\n");
		output.append("</script>\n");
				
		
		output.append("<div id='"+ getButtonId() +"content' class='x-hidden'>\n");
		output.append("<div class='x-window-header'>"+ getInfoTitle() +"</div>\n");
		output.append("<div id='"+ getButtonId() +"body'>\n");

		output.append(msgBuilder.getMessageTextFromResource("it/eng/spagobi/commons/presentation/tags/info/"+getFileName(), httpRequest));
//		output.append("<div class='x-tab' title='Hello World 1'>\n");
//		output.append("<p><b>H</b>ello...</p>\n");
//		output.append("</div>\n");
//
//		output.append("<div class='x-tab' title='Hello World 2'>\n");
//		output.append("<p>... World!</p>\n");
//		output.append("</div>\n");

		output.append("</div>\n");
		output.append("</div>\n");
		
	
        try {
            pageContext.getOut().print(output.toString());
        }
        catch (Exception ex) {
            logger.error(ex);
            throw new JspException(ex.getMessage());
        }
		return SKIP_BODY;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getInfoTitle() {
		return infoTitle;
	}

	public void setInfoTitle(String infoTitle) {
		this.infoTitle = infoTitle;
	}

	public String getButtonId() {
		return buttonId;
	}

	public void setButtonId(String buttonId) {
		this.buttonId = buttonId;
	}

}
