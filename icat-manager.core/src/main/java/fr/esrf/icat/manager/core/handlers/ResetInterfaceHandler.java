 
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


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import fr.esrf.icat.manager.core.ICATManagerActivator;

public class ResetInterfaceHandler {
	@Execute
	public void execute( final Shell parent, final UISynchronize sync, final IWorkbench workbench) {
        sync.syncExec(new Runnable() {
          @Override
          public void run() {
              boolean restart = MessageDialog.openQuestion(parent, "Reset interface ?",
                      "Do you want to reset the interface ?\nAnswering Yes will restart ICAT Manager.");
			    if (restart) {
					try {
						new File(new URL(Platform.getInstanceLocation().getURL(), ICATManagerActivator.UPDATE_FLAG_FILENAME).toURI()).createNewFile();
				    	workbench.restart();
					} catch (IOException | URISyntaxException e) {
//		    			LOG.error("Unable to create file", e);
					}
			    }
			  }
        });

	}
		
}