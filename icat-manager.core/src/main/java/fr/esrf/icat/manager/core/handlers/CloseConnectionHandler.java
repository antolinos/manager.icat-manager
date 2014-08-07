 
package fr.esrf.icat.manager.core.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import fr.esrf.icat.manager.core.ICATDataService;
import fr.esrf.icat.manager.core.icatserver.ICATServer;

public class CloseConnectionHandler {
	
	@Execute
	public void execute(final Shell shell, @Named(IServiceConstants.ACTIVE_SELECTION)@Optional ICATServer server, final EPartService partService) {
		if(MessageDialog.openConfirm(shell, "Really ?", "Close connection to ICAT ?")) {
			ICATDataService.getInstance().disconnect(server);
			// close parts for server
			final String url = server.getServerURL();
			for(MPart part : partService.getParts()) {
				if(part.getElementId().contains(url)) {
					partService.hidePart(part);
				}
			}
		}
	}
	
	
	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION)@Optional ICATServer server) {
		return server != null && server.isConnected();
	}
		
}