 
package fr.esrf.icat.manager.core.handlers;

/*
 * #%L
 * icat-manager :: core
 * %%
 * Copyright (C) 2014 ESRF - The European Synchrotron
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.ICATClientException;
import fr.esrf.icat.client.SimpleICATClient;
import fr.esrf.icat.client.wrapper.WrappedEntityBean;
import fr.esrf.icat.manager.core.ICATDataService;
import fr.esrf.icat.manager.core.icatserver.ICATEntity;
import fr.esrf.icat.manager.core.part.DataPart;
import fr.esrf.icat.manager.core.part.EntityEditDialog;

public class NewEntityHandler {
	
	private final static Logger LOG = LoggerFactory.getLogger(NewEntityHandler.class);
	
	@Execute
	public void execute(final Shell shell, @Named(IServiceConstants.ACTIVE_PART) MPart activePart) throws ICATClientException {
		DataPart part;
		if(activePart instanceof DataPart) {
			part = (DataPart) activePart;
		} else {
			part = (DataPart) activePart.getObject();
		}
		final ICATEntity entity = part.getEntity();
		final SimpleICATClient client = ICATDataService.getInstance().getClient(entity.getServer());
		try {
			final WrappedEntityBean newEntity = client.create(entity.getEntityName());
			EntityEditDialog dlg = new EntityEditDialog(shell, newEntity, client);
			if(dlg.open() == Window.OK) {
				client.create(newEntity);
				part.refresh();
				ICATDataService.getInstance().fireContentChanged();
			}
		} catch (ICATClientException e) {
			LOG.error("Error creating new entity", e);
			throw e;
		}
	}
	
	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_PART) MPart activePart) {
		return activePart.getElementId().startsWith(DataPart.DATA_PART_ELEMENT_HEADER);
	}
		
}