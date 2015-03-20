/* Copyright 2012 Engineering Ingegneria Informatica S.p.A. – SpagoBI Competency Center
 * The original code of this file is part of Spago java framework, Copyright 2004-2007.

 * This Source Code Form is subject to the term of the Mozilla Public Licence, v. 2.0. If a copy of the MPL was not distributed with this file, 
 * you can obtain one at http://Mozilla.org/MPL/2.0/.

 * Alternatively, the contents of this file may be used under the terms of the LGPL License (the “GNU Lesser General Public License”), in which 
 * case the  provisions of LGPL are applicable instead of those above. If you wish to  allow use of your version of this file only under the 
 * terms of the LGPL  License and not to allow others to use your version of this file under  the MPL, indicate your decision by deleting the 
 * provisions above and  replace them with the notice and other provisions required by the LGPL.  If you do not delete the provisions above, 
 * a recipient may use your version  of this file under either the MPL or the GNU Lesser General Public License. 

 * Spago is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the 
 * Free Software Foundation; either version 2.1 of the License, or any later version. Spago is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * for more details.
 * You should have received a copy of the GNU Lesser General Public License along with Spago. If not, see: http://www.gnu.org/licenses/. The complete text of 
 * Spago license is included in the  COPYING.LESSER file of Spago java framework.
 */
package it.eng.spago.upload;

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
