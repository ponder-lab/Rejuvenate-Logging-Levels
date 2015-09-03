package edu.harvard.hms.dbmi.bd2k.irct.model.result;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;

/**
 * Interface used to designate a result set as being able to be persisted
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface Persistable {
	
	/**
	 * Load the Result Set from the location specified
	 * 
	 * @param location Location
	 * @throws PersistableException A persistable exception occurred
	 * @throws ResultSetException  A result set exception occurred
	 */
	public void load(String location) throws ResultSetException, PersistableException;
	
	/**
	 * Returns if the result set can be loaded.
	 * 
	 * @param location Location of the Result Set
	 * @return Available
	 */
	public boolean isAvailable(String location);
	
	/**
	 * Returns if the current result set has been persisted.
	 * 
	 * True if no updates are pending for an update or persist False if an
	 * update is pending
	 * 
	 * @return Current state
	 */
	public boolean isCurrent();

	/**
	 * Returns true if the Result Set has been persisted
	 * 
	 * @return Persisted state
	 */
	public boolean isPersisted();

	/**
	 * Persists the result set to the long term storage
	 * 
	 * @throws PersistableException An error occur persisting the object
	 */
	public void persist() throws PersistableException;
	
	/**
	 * Persists the result set to the long term storage
	 * 
	 * @param location Location
	 * @throws PersistableException An error occur persisting the object
	 */
	public void persist(String location) throws PersistableException;

	/**
	 * Updates the current persisted state with any changes
	 * 
	 * @throws PersistableException An error occur merging the object
	 */
	public void merge() throws PersistableException;

	/**
	 * Refreshes the result set with the persisted state. It will over ride any
	 * entries that have not been merged.
	 *
	 * @throws PersistableException An error occur refreshing the object
	 */
	public void refresh() throws PersistableException;
}
