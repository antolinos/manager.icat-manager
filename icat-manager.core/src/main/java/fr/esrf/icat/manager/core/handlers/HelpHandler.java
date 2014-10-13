 
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