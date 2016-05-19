package edu.harvard.hms.dbmi.bd2k.irct.model.result.json;

import javax.json.JsonArray;
import javax.json.JsonObject;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.Data;

/**
 * The json result implementation interface provides an extension of the data
 * interface to allow a json object to be stored.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface JSONResultImpl extends Data {
	/**
	 * Close the json result implementation
	 */
	void close();

	/**
	 * Is the json result implementation closed
	 * @return If it is closed
	 */
	boolean isClosed();

	/**
	 * Set the json object to save
	 * 
	 * @param jsonObject JSON Object
	 */
	void setObject(JsonObject jsonObject);

	/**
	 * Sets the json array to save
	 * @param jsonArray JSON Array
	 */
	void setArray(JsonArray jsonArray);

}
