 
package fr.esrf.icat.manager.core.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import fr.esrf.icat.client.ICATClientException;
import fr.esrf.icat.client.SimpleICATClient;
import fr.esrf.icat.manager.core.ICATDataService;
import fr.esrf.icat.manager.core.icatserver.ICATServer;
import fr.esrf.icat.manager.core.part.ConnectionDialog;

public class ConnectHandler {
	
	@Execute
	public void execute(final Shell shell, @Named(IServiceConstants.ACTIVE_SELECTION)@Optional ICATServer server) {
		ConnectHandler.connectServer(server, shell, null);
	}
	
	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION)@Optional ICATServer server) {
		return server != null && !server.isConnected();
	}
	
	public static void connectServer(final ICATServer server, final Shell shell, final Runnable endAction) {
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
		BusyIndicator.showWhile(null, new Runnable(){
			@Override
			public void run() {
				try {
					ICATDataService.getInstance().connect(server);
					if(null != endAction) {
						Display.getCurrent().asyncExec(endAction);
					}
				} catch (ICATClientException e) {
					MessageDialog.openError(shell, "Connection error", "Error connecting to ICAT:\n" + e.getMessage());
				}
			}
		});
	}
		
}