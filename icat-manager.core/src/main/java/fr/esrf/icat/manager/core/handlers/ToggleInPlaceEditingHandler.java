 
package fr.esrf.icat.manager.core.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import fr.esrf.icat.manager.core.part.DataPart;

public class ToggleInPlaceEditingHandler {
	
	@Execute
	public void execute(final Shell shell, @Named(IServiceConstants.ACTIVE_PART) MPart activePart, MHandledItem handledItem) {
		DataPart part;
		if(activePart instanceof DataPart) {
			part = (DataPart) activePart;
		} else {
			part = (DataPart) activePart.getObject();
		}
		if(handledItem.isSelected()) {
			if(MessageDialog.openConfirm(shell, "Really ?",
				"Do you really want to enable in place editing ?"+
				"\nNote that you cannot edit associated entities directly in the table.")) {
				part.toggleInPLaceEditing();
			} else {
				handledItem.setSelected(false);
			}
		} else {
			part.toggleInPLaceEditing();
		}
	}
		
}