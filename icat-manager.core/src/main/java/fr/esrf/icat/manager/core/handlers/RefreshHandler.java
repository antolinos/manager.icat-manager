 
package fr.esrf.icat.manager.core.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;

import fr.esrf.icat.manager.core.part.DataPart;

public class RefreshHandler {

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_PART) MPart activePart) {
		((DataPart) activePart.getObject()).refresh();
	}
	
	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_PART) MPart activePart) {
		return activePart.getElementId().startsWith(DataPart.DATA_PART_ELEMENT_HEADER);
	}
		
}