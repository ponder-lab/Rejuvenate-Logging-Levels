package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ProcessResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;

@Stateless
public class ResourceController {
	@Inject
	private IRCTApplication ipctApp;
	
	public List<Resource> getResources() {
		return new ArrayList<Resource>(ipctApp.getResources().values());
	}

	public Resource getResource(String resource) {
		return ipctApp.getResources().get(resource);
	}

	public List<Resource> getQueryResources() {
		List<Resource> queryResources = new ArrayList<Resource>();
		for(Resource resource : ipctApp.getResources().values()) {
			if(resource.getImplementingInterface() instanceof QueryResourceImplementationInterface) {
				queryResources.add(resource);
				
			}
		}
		return queryResources;
	}
	
	public List<Resource> getProcessResources() {
		List<Resource> processResources = new ArrayList<Resource>();
		for(Resource resource : ipctApp.getResources().values()) {
			if(resource.getImplementingInterface() instanceof ProcessResourceImplementationInterface) {
				processResources.add(resource);
			}
		}
		return processResources;
	}
	
}
