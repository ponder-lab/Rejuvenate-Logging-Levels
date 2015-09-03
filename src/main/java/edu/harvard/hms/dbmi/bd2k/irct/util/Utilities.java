package edu.harvard.hms.dbmi.bd2k.irct.util;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;

/**
 * A set of utilities for the IRCT
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class Utilities {
	
	/**
	 * Traverses the paths
	 * 
	 * @param currentPosition Current position
	 * @param components Components 
	 * @return Path
	 */
	public static Path traversePath(Path currentPosition, String[] components){
		
		if(currentPosition.getName().equals(components[0])) {
			if(components.length == 1) {
				return currentPosition;
			} 
			
			components = ArrayUtils.remove(components, 0);
			
			return traversePath(currentPosition, components);
		}
		
		String next = components[0];
		components = ArrayUtils.remove(components, 0);
		
		for(List<Path> potentialPaths : currentPosition.getRelationships().values()) {
			
			for(Path path : potentialPaths) {
				if(path.getName().equals(next)) {
					if(components.length == 0) {
						return path;
					}
					return traversePath(path, components);
				}
			}
			
		}
		
		return null;
		
	}
}
