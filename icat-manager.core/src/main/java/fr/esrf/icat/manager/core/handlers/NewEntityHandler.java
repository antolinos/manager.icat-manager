 
package fr.esrf.icat.manager.core.handlers;

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
		final DataPart part = (DataPart) activePart.getObject();
		final ICATEntity entity = part.getEntity();
		final SimpleICATClient client = ICATDataService.getInstance().getClient(entity.getServer());
		try {
			final WrappedEntityBean newEntity = client.create(entity.getEntityName());
			EntityEditDialog dlg = new EntityEditDialog(shell, newEntity, client);
			if(dlg.open() == Window.OK) {
				client.create(newEntity);
				part.refresh();
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