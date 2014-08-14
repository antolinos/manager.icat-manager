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

	@Override
	public int hashCode() {
		return serverURL.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof ICATServer)) return false;
		ICATServer other = (ICATServer) obj;
		if (serverURL == null) {
			return other.serverURL == null;
		}
		return serverURL.equals(other.serverURL);
	}

}
