package fr.esrf.icat.manager.core.icatserver;

public class ICATEntity {

	private ICATServer server;
	private String entityName;
	
	public ICATEntity(ICATServer server, String entityName) {
		super();
		this.server = server;
		this.entityName = entityName;
	}

	public ICATServer getServer() {
		return server;
	}

	public String getEntityName() {
		return entityName;
	}

}
