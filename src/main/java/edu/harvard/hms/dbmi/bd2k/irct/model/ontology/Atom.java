package edu.harvard.hms.dbmi.bd2k.irct.model.ontology;

import java.util.Map;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;

public class Atom {
	private long atomUID;
	
	private Map<Resource, Path> paths;

	public long getAtomUID() {
		return atomUID;
	}

	public void setAtomUID(long atomUID) {
		this.atomUID = atomUID;
	}

	public Map<Resource, Path> getPaths() {
		return paths;
	}

	public void setPaths(Map<Resource, Path> paths) {
		this.paths = paths;
	}
	
}
