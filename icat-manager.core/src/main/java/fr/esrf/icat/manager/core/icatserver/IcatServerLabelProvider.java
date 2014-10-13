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


import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

//import fr.esrf.icat.manager.core.ICATDataService;

public class IcatServerLabelProvider extends StyledCellLabelProvider {

//	private ICATDataService service = ICATDataService.getInstance();
	private final static Image unknown_server_image;
	private final static Image connected_server_image;
	private final static Image failed_server_image;	
	
	static {
	    Bundle bundle = FrameworkUtil.getBundle(IcatServerLabelProvider.class);
	    URL url = FileLocator.find(bundle, new Path("icons/server_unknown.gif"), null);
	    unknown_server_image = ImageDescriptor.createFromURL(url).createImage();
	    url = FileLocator.find(bundle, new Path("icons/passed.png"), null);
	    connected_server_image = ImageDescriptor.createFromURL(url).createImage();
	    url = FileLocator.find(bundle, new Path("icons/fail.gif"), null);
	    failed_server_image = ImageDescriptor.createFromURL(url).createImage();
	}
	
	public IcatServerLabelProvider() {
		super();
	}

	@Override
	public void update(ViewerCell cell) {
	      Object element = cell.getElement();
	      StyledString text = new StyledString();
	      if(element instanceof ICATServer) {
		      ICATServer icatServer = (ICATServer)element;
		      text.append(icatServer.getServerURL());
		      if(icatServer.isConnected()) {
		    	  cell.setImage(null);
			      text.append(" (" + icatServer.getVersion() + ") ", StyledString.QUALIFIER_STYLER);
		      }
		      switch (icatServer.getStatus()) {
		      	case CONNECTED: cell.setImage(connected_server_image); break;
		      	case FAILED: cell.setImage(failed_server_image); break;
		      	default: cell.setImage(unknown_server_image); break;
		      }
		    	  
	      } else if(element instanceof ICATEntity){
	    	  ICATEntity entity = (ICATEntity)element;
	    	  text.append(entity.getEntityName());
//		      text.append(" (" + service.getEntityCount(entity) + ") ", StyledString.COUNTER_STYLER);
	      } else {
	    	  text.append(element.toString());
	      }
	      cell.setText(text.toString());
	      cell.setStyleRanges(text.getStyleRanges());
	      super.update(cell);
	}

}
