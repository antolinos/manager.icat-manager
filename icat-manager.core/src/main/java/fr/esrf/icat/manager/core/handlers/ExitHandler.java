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
				partService.hidePart(part, true);
			}
		}
	}

}
