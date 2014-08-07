 
package fr.esrf.icat.manager.core.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;

import fr.esrf.icat.manager.core.ICATDataService;
import fr.esrf.icat.manager.core.icatserver.ICATServer;

public class ConnectHandler {
	
	@Execute
	public void execute(final Shell shell, @Named(IServiceConstants.ACTIVE_SELECTION)@Optional ICATServer server) {
		ICATDataService.getInstance().connect(server, shell);
	}
	
	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION)@Optional ICATServer server) {
		return server != null && !server.isConnected();
	}
		
}