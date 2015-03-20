/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.utilities;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.httpchannel.upload.IUploadHandler;
import it.eng.spago.dispatching.service.DefaultRequestContext;

import org.apache.commons.fileupload.FileItem;

public class UploadManager extends DefaultRequestContext implements IUploadHandler {

	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.httpchannel.upload.IUploadHandler#upload(org.apache.commons.fileupload.FileItem)
	 */
	public void upload(FileItem item) throws Exception {
		if (item != null) {
			SourceBean serviceRequest = getServiceRequest();
			serviceRequest.setAttribute("UPLOADED_FILE", item);
		}
		
//		long size = item.getSize();
//		
//		if(size>0) {
//			UploadedFile uploadedFile = new UploadedFile();
//			uploadedFile.setFileContent(item.get());
//			uploadedFile.setFieldNameInForm(item.getFieldName());
//			uploadedFile.setSizeInBytes(item.getSize());
//			uploadedFile.setFileName(GeneralUtilities.getRelativeFileNames(item.getName()));
//			SourceBean serviceRequest = getServiceRequest();
//			serviceRequest.setAttribute("UPLOADED_FILE", uploadedFile);
//		}
	}

}
