 
package fr.esrf.icat.manager.core.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

import fr.esrf.icat.manager.core.part.DataPart;

public class HelpHandler {
	
	public final static String HELP_VIEW_ID = "icat-manager.core.partdescriptor.browserhelp";
	
	@Execute
	public void execute(final EPartService partService, final EModelService modelService, final MWindow window) {
		MPart mPart = (MPart) modelService.find(HELP_VIEW_ID, window);
		if(null != mPart) {
		    partService.showPart(mPart, PartState.ACTIVATE);
		    return;
		}
		mPart = partService.createPart(HELP_VIEW_ID);
		mPart.setElementId(HELP_VIEW_ID);
		MPartStack partstack = (MPartStack) modelService.find(DataPart.ICAT_MANAGER_MAINSTACK, window);
		partstack.getChildren().add(mPart);
		partService.showPart(mPart, PartState.ACTIVATE);
	}
		
}