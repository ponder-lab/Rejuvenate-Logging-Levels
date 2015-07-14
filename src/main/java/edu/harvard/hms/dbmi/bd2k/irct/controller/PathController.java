package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.ri.exception.ResourceInterfaceException;

@Stateless
public class PathController {

	public Path getPathRoot(Resource resource) {
		return ((PathResourceImplementationInterface) resource.getImplementingInterface()).getPathRoot();
	}
	
	public List<Path> getPathRelationship(Resource resource, Path path, OntologyRelationship relationship) throws ResourceInterfaceException {
		if(path == null) {
			return new ArrayList<Path>();
		}
		return ((PathResourceImplementationInterface) resource.getImplementingInterface()).getPathRelationship(path, relationship);
	}
	
	public OntologyType getPathType(Resource resource) {
		return ((PathResourceImplementationInterface) resource.getImplementingInterface()).getOntologyType();	
	}

	public Path getPathFromString(Resource resource, String path) {
		return ((PathResourceImplementationInterface) resource.getImplementingInterface()).getPathFromString(path);
	}
}
