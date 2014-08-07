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
	
	
	public IcatServerLabelProvider() {
		super();
	    Bundle bundle = FrameworkUtil.getBundle(IcatServerLabelProvider.class);
	    URL url = FileLocator.find(bundle, new Path("icons/server_unknown.gif"), null);
	    ImageDescriptor imageDcr = ImageDescriptor.createFromURL(url);
	    this.unknown_server_image = imageDcr.createImage();
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
		      } else {
		    	  cell.setImage(unknown_server_image);
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
