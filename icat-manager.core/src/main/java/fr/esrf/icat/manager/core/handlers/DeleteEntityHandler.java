 
package fr.esrf.icat.manager.core.handlers;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.ICATClientException;
import fr.esrf.icat.client.SimpleICATClient;
import fr.esrf.icat.client.wrapper.WrappedEntityBean;
import fr.esrf.icat.manager.core.ICATDataService;
import fr.esrf.icat.manager.core.icatserver.ICATEntity;
import fr.esrf.icat.manager.core.part.DataPart;

public class DeleteEntityHandler {
	
	private final static Logger LOG = LoggerFactory.getLogger(DeleteEntityHandler.class);
	
	@Execute
	public void execute(final Shell shell, @Named(IServiceConstants.ACTIVE_SELECTION)@Optional Object selection,
			@Named(IServiceConstants.ACTIVE_PART) MPart activePart) throws ICATClientException {
		// confirm
		if(!MessageDialog.openConfirm(shell, "Really ?", "Delete selected entities ?")) {
			return;
		}
		DataPart part;
		if(activePart instanceof DataPart) {
			part = (DataPart) activePart;
		} else {
			part = (DataPart) activePart.getObject();
		}
		final ICATEntity entity = part.getEntity();
		final SimpleICATClient client = ICATDataService.getInstance().getClient(entity.getServer());
		try {
			if(selection instanceof WrappedEntityBean) {
				client.delete((WrappedEntityBean) selection);
			} else if(selection instanceof List) {
				List<WrappedEntityBean> l = new LinkedList<>();
				for(Object o : (List<?>) selection) {
					l.add((WrappedEntityBean) o);
				}
				client.delete(l);
			} else {
				return;
			}
			part.refresh();
			ICATDataService.getInstance().fireContentChanged();
		} catch (ICATClientException e) {
			LOG.error("Error deleting entity", e);
			throw e;
		}
	}
	
	
	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION)@Optional Object selection) {
		if(null == selection) return false;
		if(selection instanceof List) return !((List<?>)selection).isEmpty();
		return selection instanceof WrappedEntityBean;
	}
		
}