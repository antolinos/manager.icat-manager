package fr.esrf.icat.manager.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.SimpleICATClient;
import fr.esrf.icat.client.ICATClientException;
import fr.esrf.icat.client.dynamic.DynamicSimpleICATClient;
import fr.esrf.icat.client.wrapper.WrappedEntityBean;
import fr.esrf.icat.manager.core.icatserver.ICATEntity;
import fr.esrf.icat.manager.core.icatserver.ICATServer;
import fr.esrf.icat.manager.core.icatserver.ICATServer.Status;

public class ICATDataService {
	
	public final static String DATA_SERVICE_CONTENT = "fr.esrf.icat.manager.core.ICATDataService.CONTENT";

	private final File configfile = new File(System.getProperty("user.home"), ".icat_servers");

	private final static Logger LOG = LoggerFactory.getLogger(ICATDataService.class);

	private final static ICATDataService instance = new ICATDataService();
	
	private final PropertyChangeSupport propertyChangeSupport;
	
	private List<ICATServer> serverList;
	
	private Map<ICATServer, SimpleICATClient> clientMap;
	
	private boolean modified = true;
	
	public static ICATDataService getInstance() {
		return instance;
	}
	
	private ICATDataService() {
		super();
		serverList = new ArrayList<>();
		clientMap = new HashMap<>();
		propertyChangeSupport = new PropertyChangeSupport(this);
	}
	
	public void init() {
		LOG.debug("Configuration file at: " + configfile.getAbsolutePath());
		if(configfile.exists()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(configfile));
				String line;
				while((line = reader.readLine()) != null) {
					serverList.add(makeServer(line));
				}
				LOG.debug("Loaded " + serverList.size() + " server URLs");
			} catch (IOException e) {
				LOG.error("Error reading server file " + configfile.getAbsolutePath(), e);
			} finally {
				if(null != reader) {
					try {
						reader.close();
					} catch (IOException e) {
						LOG.warn("Error closing file " + configfile.getAbsolutePath(), e);
					}
				}
			}
		} else {
			LOG.debug(configfile.getAbsolutePath() + " file not found");
		}
	}

	public List<ICATServer> getServerList() {
		return Collections.unmodifiableList(serverList);
	}
	
	public void addServer(final ICATServer server) {
		serverList.add(server);
		fireContentChanged();
//		modified = true;
	}
	
	public void removeServer(final ICATServer server) {
		if(serverList.remove(server)) {
			fireContentChanged();
//			modified = true;
		}
	}
	
	public List<ICATEntity> getEntityList(final ICATServer server) {
		SimpleICATClient client = getClient(server);
		try {
			List<String> entityNames = client.getEntityNames();
			List<ICATEntity> entityList = new LinkedList<>();
			for(String name : entityNames) {
				entityList.add(new ICATEntity(server, name));
			}
			return entityList;
		} catch (ICATClientException e) {
			LOG.error("Unable to retrieve entity list from " + server.getServerURL(), e);
			return null;
		}
	}
	
	public SimpleICATClient getClient(final ICATServer server) {
		SimpleICATClient client = clientMap.get(server);
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
	
	private SimpleICATClient makeClient(final ICATServer server) {
		SimpleICATClient client = new DynamicSimpleICATClient();
		client.setIcatBaseUrl(server.getServerURL());
		return client;
	}

	public int getEntityCount(final ICATEntity entity) {
		SimpleICATClient client = clientMap.get(entity.getServer());
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
		
		// stop clients
		for(SimpleICATClient client : clientMap.values()) {
			client.stop();
		}
		// clear client map
		clientMap.clear();
		// save server list
		if(modified) {
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(configfile));
				for(ICATServer server : serverList) {
					writer.write(makeLine(server));
					writer.newLine();
				}
				LOG.debug("Saved " + serverList.size() + " server URLs");
			} catch (IOException e) {
				LOG.error("Error reading server file " + configfile.getAbsolutePath(), e);
			} finally {
				if(null != writer) {
					try {
						writer.close();
					} catch (IOException e) {
						LOG.warn("Error closing file " + configfile.getAbsolutePath(), e);
					}
				}
			}
		}
	}

	public void connect(final ICATServer server) throws ICATClientException {
		SimpleICATClient client = getClient(server);
		try {
			client.init();
			server.setConnected(true);
			server.setStatus(Status.CONNECTED);
			server.setVersion(client.getServerVersion());
			fireContentChanged();
		} catch (ICATClientException e) {
			LOG.warn("Unable to connect user " + client.getIcatUsername(), e);
			server.setConnected(false);
			server.setVersion(null);
			server.setStatus(Status.FAILED);
			fireContentChanged();
			throw e;
		}
	}

	public void fireContentChanged() {
		propertyChangeSupport.firePropertyChange(DATA_SERVICE_CONTENT, null, serverList);
	}
	
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

	public void disconnect(final ICATServer server) {
		getClient(server).stop();
		server.setConnected(false);
		server.setVersion(null);
		server.setStatus(Status.UNKNOWN);
		fireContentChanged();
	}
	
	private ICATServer makeServer(final String line) {
		final int auth_sep = line.indexOf('@');
		if(auth_sep < 0) {
			return new ICATServer(line.trim());
		}
		String auth_str = line.substring(0, auth_sep);
		ICATServer server = new ICATServer(line.substring(auth_sep + 1));
		final int user_sep = auth_str.indexOf(':');
		server.setLastAuthnMethod(auth_str.substring(0, user_sep));
		server.setLastUserName(auth_str.substring(user_sep + 1));
		return server;
	}

	private String makeLine(final ICATServer server) {
		return server.getLastAuthnMethod() + ":" + server.getLastUserName() + "@" + server.getServerURL();
	}

}
