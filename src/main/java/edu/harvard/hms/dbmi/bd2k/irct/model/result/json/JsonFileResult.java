package edu.harvard.hms.dbmi.bd2k.irct.model.result.json;

import java.nio.file.Path;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.Persistable;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;

/**
 * The JSON File Result allows for a json result to persisted.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class JsonFileResult implements Persistable, JSONResultImpl {
	private JsonStructure jsonStructure;
	private String fileName;
	private Path dataFile;
	private boolean current = false;
	private boolean persisted = false;
	private boolean closed = false;

	/**
	 * Creates a JSON File Result
	 */
	public JsonFileResult() {

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void load(String location) throws ResultSetException,
			PersistableException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isAvailable(String location) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void persist() throws PersistableException {
		// TODO Auto-generated method stub

	}

	@Override
	public void persist(String location) throws PersistableException {
		// TODO Auto-generated method stub

	}

	@Override
	public void merge() throws PersistableException {
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh() throws PersistableException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setObject(JsonObject jsonObject) {
		this.jsonStructure = jsonObject;
	}

	@Override
	public boolean isCurrent() {
		return this.current;
	}

	@Override
	public boolean isPersisted() {
		return this.persisted;
	}

	@Override
	public void setArray(JsonArray jsonArray) {
		this.jsonStructure = jsonArray;
	}

	@Override
	public boolean isClosed() {
		return this.closed;
	}
}
