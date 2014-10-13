package fr.esrf.icat.manager.core.icatserver;

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


import java.util.List;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import fr.esrf.icat.manager.core.ICATDataService;

public class IcatServerContentProvider implements ITreeContentProvider {

	ICATDataService service;
	
	public IcatServerContentProvider() {
		super();
		service = ICATDataService.getInstance();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List) {
			return ((List<?>) inputElement).toArray();
		} 
		if(inputElement instanceof ICATDataService) {
			return ((ICATDataService)inputElement).getServerList().toArray();
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return (parentElement instanceof ICATServer) && ((ICATServer)parentElement).isConnected() 
						? service.getEntityList((ICATServer)parentElement).toArray()
						: null;
	}

	@Override
	public Object getParent(Object element) {
		return element instanceof ICATEntity ? ((ICATEntity)element).getServer() : null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return (element instanceof ICATServer) && ((ICATServer)element).isConnected();
	}

}
