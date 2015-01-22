 
package fr.esrf.icat.manager.core.handlers;

/*
 * #%L
 * icat-manager :: core
 * %%
 * Copyright (C) 2014 - 2015 ESRF - The European Synchrotron
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.ICATClientException;
import fr.esrf.icat.manager.core.ICATDataService;
import fr.esrf.icat.manager.core.icatserver.ICATEntity;
import fr.esrf.icat.manager.core.part.ServerPart;

public class CountEntitiesInServerHandler {
	
	private final static Logger LOG = LoggerFactory.getLogger(CountEntitiesInServerHandler.class);
	
	@Execute
	public void execute(final @Named(IServiceConstants.ACTIVE_SELECTION)@Optional ICATEntity entity, final Shell shell, 
			final UISynchronize sync, final @Named(IServiceConstants.ACTIVE_PART)@Optional MPart part) {
		
		final Cursor original = shell.getCursor();
		final Cursor busyCursor = shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT);
		
		ServerPart sPart;
		if(part instanceof ServerPart) {
			sPart = (ServerPart) part;
		} else {
			sPart = (ServerPart) part.getObject();
		}
		
		if(null == sPart) {
			LOG.error("Unable to retrieve Server part");
			return;
		}
		final ServerPart serverPart = sPart;
		
		Job j = new Job("Counting entities"){

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					sync.syncExec(new Runnable(){
						@Override
						public void run() {
							shell.setCursor(busyCursor);
						}
					});
					
					final long entityCount = ICATDataService.getInstance().getEntityCount(entity);
					
					sync.syncExec(new Runnable(){
						@Override
						public void run() {
							serverPart.setEntityCount(entity, entityCount);
						}
					});
					return Status.OK_STATUS;
				} catch (final ICATClientException e) {
					sync.syncExec(new Runnable(){
						@Override
						public void run() {
							MessageDialog.openError(shell, "Connection error", "Error connecting to ICAT:\n" + e.getMessage());
						}
					});
					return Status.CANCEL_STATUS;
				} finally {
					sync.syncExec(new Runnable(){
						@Override
						public void run() {
							shell.setCursor(original);
						}
					});
				}
			}
			
		};
		j.schedule();
	}
	
	
	@CanExecute
	public boolean canExecute(final @Named(IServiceConstants.ACTIVE_SELECTION)@Optional ICATEntity entity) {
		return entity != null;
	}
		
}