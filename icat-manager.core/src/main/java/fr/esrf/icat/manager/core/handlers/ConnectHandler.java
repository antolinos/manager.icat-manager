 
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Shell;

import fr.esrf.icat.client.ICATClientException;
import fr.esrf.icat.client.SimpleICATClient;
import fr.esrf.icat.manager.core.ICATDataService;
import fr.esrf.icat.manager.core.icatserver.ICATServer;
import fr.esrf.icat.manager.core.part.ConnectionDialog;

public class ConnectHandler {
	
	@Execute
	public void execute(final Shell shell, final UISynchronize sync, @Named(IServiceConstants.ACTIVE_SELECTION)@Optional ICATServer server) {
		ConnectHandler.connectServer(server, sync, shell, null);
	}
	
	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION)@Optional ICATServer server) {
		return server != null && !server.isConnected();
	}
	
	public static void connectServer(final ICATServer server, final UISynchronize sync, final Shell shell, final Runnable endAction) {
		ConnectionDialog dlg = new ConnectionDialog(shell, server.getLastAuthnMethod(), server.getLastUserName());
		if(dlg.open() != Window.OK) {
			return;
		}
		SimpleICATClient client = ICATDataService.getInstance().getClient(server);
		final String authenticationMethod = dlg.getAuthenticationMethod();
		client.setIcatAuthnPlugin(authenticationMethod);
		server.setLastAuthnMethod(authenticationMethod);
		final String user = dlg.getUser();
		client.setIcatUsername(user);
		server.setLastUserName(user);
		client.setIcatPassword(dlg.getPassword());
		
		final Cursor original = shell.getCursor();
		final Cursor busyCursor = shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT);

		Job j = new Job("Connection to server"){

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					sync.syncExec(new Runnable(){
						@Override
						public void run() {
							shell.setCursor(busyCursor);
						}
					});
					ICATDataService.getInstance().connect(server);
					if(null != endAction) {
						sync.asyncExec(endAction);
					}
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
		
}