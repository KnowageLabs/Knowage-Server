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
