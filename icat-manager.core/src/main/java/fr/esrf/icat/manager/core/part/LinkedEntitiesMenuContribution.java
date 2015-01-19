 
package fr.esrf.icat.manager.core.part;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.wrapper.WrappedEntityBean;

public class LinkedEntitiesMenuContribution {
	
	private static final String ICAT_MANAGER_CORE_COMMAND_OPENENTITY = "icat-manager.core.command.openentity";

	private final static Logger LOG = LoggerFactory.getLogger(LinkedEntitiesMenuContribution.class);

	@Inject
    protected MApplication application;

	@AboutToShow
	public void aboutToShow(
			final List<MMenuElement> items,
			final @Named(IServiceConstants.ACTIVE_PART) MPart activePart,
			final @Named(IServiceConstants.ACTIVE_SELECTION)@Optional  List<WrappedEntityBean> selection) {
		
		if(null == selection || selection.isEmpty()) {
			return;
		}
		
	    MCommand command = null;
	    for(MCommand c : application.getCommands()) {
	    	if(ICAT_MANAGER_CORE_COMMAND_OPENENTITY.equals(c.getElementId())) {
	    		command = c;
	    		break;
	    	}
	    }
	    
	    if(null == command) {
	    	LOG.error("Command {} not found", ICAT_MANAGER_CORE_COMMAND_OPENENTITY);
	    	return;
	    }

		DataPart part;
		if(activePart instanceof DataPart) {
			part = (DataPart) activePart;
		} else {
			part = (DataPart) activePart.getObject();
		}
		
	    for(String entityName : selection.get(0).getEntityFields()) {
			MHandledMenuItem dynamicItem = MMenuFactory.INSTANCE.createHandledMenuItem();
			dynamicItem.setCommand(command);
		    final String entityLabel = StringUtils.capitalize(entityName);
			dynamicItem.setLabel(entityLabel);
		    MParameter p = MCommandsFactory.INSTANCE.createParameter();
		    p.setName("icat-manager.core.commandparameter.filter");
		    p.setValue("id < 100");
		    dynamicItem.getParameters().add(p);
		    MParameter pEntity = MCommandsFactory.INSTANCE.createParameter();
		    pEntity.setName("icat-manager.core.commandparameter.entity");
		    pEntity.setValue(entityLabel);
		    dynamicItem.getParameters().add(pEntity);
		    MParameter pServer = MCommandsFactory.INSTANCE.createParameter();
		    pServer.setName("icat-manager.core.commandparameter.server");
		    pServer.setValue(part.getEntity().getServer().getServerURL());
		    dynamicItem.getParameters().add(pServer);
		    dynamicItem.setEnabled(true);
		    dynamicItem.setToBeRendered(true);
		    dynamicItem.setVisible(true);
		    items.add(dynamicItem);
		}
		
	}
		
}