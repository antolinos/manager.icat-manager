package fr.esrf.icat.manager.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import fr.esrf.icat.client.ICATClient;
import fr.esrf.icat.client.ICATClientException;
import fr.esrf.icat.client.v4_3_1.ICATClientImpl;
import fr.esrf.icat.client.wrapper.WrappedEntityBean;
import fr.esrf.icat.manager.core.icatserver.ICATEntity;
import fr.esrf.icat.manager.core.icatserver.ICATServer;
import fr.esrf.icat.manager.core.part.ConnectionDialog;

public class ICATDataService {

	private final static ICATDataService instance = new ICATDataService();
	
	private List<ICATServer> serverList;
	
	private Map<ICATServer, ICATClient> clientMap;
	
	public static ICATDataService getInstance() {
		return instance;
	}
	
	private ICATDataService() {
		super();
		serverList = new ArrayList<>();
		clientMap = new HashMap<>();
	}

	public List<ICATServer> getServerList() {
		return serverList;
	}
	
	public void addServer(final ICATServer server) {
		serverList.add(server);
	}
	
	public List<ICATEntity> getEntityList(final ICATServer server) {
		ICATClient client = getClient(server);
		try {
			List<String> entityNames = client.getEntityNames();
			List<ICATEntity> entityList = new LinkedList<>();
			for(String name : entityNames) {
				entityList.add(new ICATEntity(server, name));
			}
			return entityList;
		} catch (ICATClientException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ICATClient getClient(final ICATServer server) {
		ICATClient client = clientMap.get(server);
		if(null == client) {
			synchronized (this) {
				client = clientMap.get(server);
				if(null == client) {
					client = makeClient(server);
					clientMap.put(server, client);		
				}
			}
		}
		return client;
	}
	
	private ICATClient makeClient(final ICATServer server) {
		ICATClient client = new ICATClientImpl();
		client.setIcatBaseUrl(server.getServerURL());
		return client;
	}

	public int getEntityCount(final ICATEntity entity) {
		ICATClient client = clientMap.get(entity.getServer());
		List<WrappedEntityBean> result;
		try {
			result = client.search("COUNT(" + entity.getEntityName() + ")");
		} catch (ICATClientException e) {
			e.printStackTrace();
			return 0;
		}
		if(null == result || result.size() == 0) {
			return 0;
		}
		Object number = result.get(0).getWrapped();
		if(!(number instanceof Number)) {
			return 0;
		}
		return ((Number)number).intValue(); 
	}

	public void stop() {
		for(ICATClient client : clientMap.values()) {
			client.stop();
		}
		clientMap.clear();
	}

	public void connect(final ICATServer server, final Shell shell) {
		ConnectionDialog dlg = new ConnectionDialog(shell);
		if(dlg.open() != Window.OK) {
			return;
		}
		ICATClient client = getClient(server);
		client.setIcatAuthnPlugin(dlg.getAuthenticationMethod());
		client.setIcatUsername(dlg.getUser());
		client.setIcatPassword(dlg.getPassword());
		try {
			client.init();
			server.setConnected(true);
		} catch (ICATClientException e) {
			e.printStackTrace();
			MessageDialog.openError(shell, "Connection error", "Error connecting to ICAT:\n" + e.getMessage());
			return;
		}
	}
}
