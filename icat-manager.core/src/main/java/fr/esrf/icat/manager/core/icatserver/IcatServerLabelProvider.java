package fr.esrf.icat.manager.core.icatserver;

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

import fr.esrf.icat.manager.core.ICATDataService;

public class IcatServerLabelProvider extends StyledCellLabelProvider {

	private ICATDataService service = ICATDataService.getInstance();
	private Image unknown_server_image;
	private Image connected_server_image;
	private Image failed_server_image;	
	
	public IcatServerLabelProvider() {
		super();
	    Bundle bundle = FrameworkUtil.getBundle(IcatServerLabelProvider.class);
	    URL url = FileLocator.find(bundle, new Path("icons/server_unknown.gif"), null);
	    this.unknown_server_image = ImageDescriptor.createFromURL(url).createImage();
	    url = FileLocator.find(bundle, new Path("icons/passed.png"), null);
	    this.connected_server_image = ImageDescriptor.createFromURL(url).createImage();
	    url = FileLocator.find(bundle, new Path("icons/fail.gif"), null);
	    this.failed_server_image = ImageDescriptor.createFromURL(url).createImage();
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
		      text.append(" (" + service.getEntityCount(entity) + ") ", StyledString.COUNTER_STYLER);
	      } else {
	    	  text.append(element.toString());
	      }
	      cell.setText(text.toString());
	      cell.setStyleRanges(text.getStyleRanges());
	      super.update(cell);
	}

}
