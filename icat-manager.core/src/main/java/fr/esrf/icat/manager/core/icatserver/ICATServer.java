package fr.esrf.icat.manager.core.icatserver;

public class ICATServer {

	private String serverURL;
	
	private String version;
	
	private boolean connected;
	
	private ICATServer.Status status;
	
	public static enum Status {
		UNKNOWN,
		CONNECTED,
		FAILED
	}
	
	public ICATServer(String serverURL) {
		super();
		this.serverURL = serverURL;
		this.version = null;
		this.connected = false; 
		this.status = Status.UNKNOWN;
	}
		
	public String getServerURL() {
		return serverURL;
	}

	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isConnected(){
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public ICATServer.Status getStatus() {
		return status;
	}

	public void setStatus(ICATServer.Status status) {
		this.status = status;
	}

}
