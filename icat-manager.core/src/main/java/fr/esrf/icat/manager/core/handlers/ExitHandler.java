package fr.esrf.icat.manager.core.handlers;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.eclipse.e4.core.di.annotations.Execute;

import fr.esrf.icat.manager.core.part.DataPart;

public class ExitHandler implements IWindowCloseHandler {
    
	private EPartService service;

	public ExitHandler() {
		super();
	}

	public ExitHandler(EPartService service) {
		super();
		this.service = service;
	}
	
	@Execute
	  public void execute(IWorkbench workbench, EPartService partService) {
		manageExit(partService);
	    workbench.close();
	  }

	@Override
	public boolean close(MWindow window) {
		manageExit(service);
		return true;
	}

	private void manageExit(final EPartService partService) {
		for(MPart part : partService.getParts()) {
			if(part.getElementId().startsWith(DataPart.DATA_PART_ELEMENT_HEADER)) {
				partService.hidePart(part);
			}
		}
	}

}
