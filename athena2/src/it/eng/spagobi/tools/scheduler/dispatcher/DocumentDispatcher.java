/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.dispatcher;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public class DocumentDispatcher {

	private boolean disposed;
	private DispatchContext dispatchContext;
	private final List<IDocumentDispatchChannel> documentDispatchChannels;

	// logger component
	private static Logger logger = Logger.getLogger(DocumentDispatcher.class);

	public DocumentDispatcher(DispatchContext dispatchContext) {
		this.disposed = false;
		this.dispatchContext = dispatchContext;

		this.documentDispatchChannels = new ArrayList<IDocumentDispatchChannel>();

		IDocumentDispatchChannel dispatchChannel;
		if (dispatchContext.isSnapshootDispatchChannelEnabled()) {
			dispatchChannel = new SnapshootDocumentDispatchChannel(dispatchContext);
			documentDispatchChannels.add(dispatchChannel);
		}

		if (dispatchContext.isFunctionalityTreeDispatchChannelEnabled()) {
			dispatchChannel = new FunctionalityTreeDocumentDispatchChannel(dispatchContext);
			documentDispatchChannels.add(dispatchChannel);
		}

		if (dispatchContext.isGlobalUniqueMail()) {
			dispatchChannel = new UniqueMailDocumentDispatchChannel(dispatchContext);
			documentDispatchChannels.add(dispatchChannel);
		}

		if (dispatchContext.isMailDispatchChannelEnabled() && !dispatchContext.isGlobalUniqueMail()) {
			dispatchChannel = new MailDocumentDispatchChannel(dispatchContext);
			documentDispatchChannels.add(dispatchChannel);
		}
		if (dispatchContext.isDistributionListDispatchChannelEnabled()) {
			dispatchChannel = new DistributionListDocumentDispatchChannel(dispatchContext);
			documentDispatchChannels.add(dispatchChannel);
		}

		if (dispatchContext.isJavaClassDispatchChannelEnabled()) {
			dispatchChannel = new JavaClassDocumentDispatchChannel(dispatchContext);
			documentDispatchChannels.add(dispatchChannel);
		}

		if (dispatchContext.isFileSystemDispatchChannelEnabled()) {
			dispatchChannel = new FileSystemDocumentDispatcher(dispatchContext);
			documentDispatchChannels.add(dispatchChannel);
		}
	}

	public boolean dispatch(BIObject document, byte[] executionOutput) {
		boolean dispatchedToAllChannels = true;
		if (disposed) {
			throw new SpagoBIRuntimeException("Impossible to dispatch document [" + document + "]. The dispatcher has been disposed.");
		}

		for (IDocumentDispatchChannel dispatchChannel : documentDispatchChannels) {
			boolean succesfullyDispatched = dispatchChannel.dispatch(document, executionOutput);
			dispatchedToAllChannels = dispatchedToAllChannels && succesfullyDispatched;
		}
		return dispatchedToAllChannels;
	}

	public boolean canDispatch(BIObject document) {
		boolean canDispatch = false;
		for (IDocumentDispatchChannel dispatchChannel : documentDispatchChannels) {
			canDispatch = canDispatch || dispatchChannel.canDispatch(document);

		}
		return canDispatch;
	}

	public void dispose() {
		for (IDocumentDispatchChannel dispatchChannel : documentDispatchChannels) {
			dispatchChannel.close();
		}
		this.disposed = true;
	}

	public void setDispatchContext(DispatchContext dispatchContext) {
		this.dispatchContext = dispatchContext;
		for (IDocumentDispatchChannel dispatchChannel : documentDispatchChannels) {
			dispatchChannel.setDispatchContext(dispatchContext);
		}
	}
}
