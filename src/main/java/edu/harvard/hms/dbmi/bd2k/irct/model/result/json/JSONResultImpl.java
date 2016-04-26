package edu.harvard.hms.dbmi.bd2k.irct.model.result.json;

import javax.json.JsonArray;
import javax.json.JsonObject;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.Data;

public interface JSONResultImpl extends Data {
	// Administrative
	void close();
	boolean isClosed();
	void setObject(JsonObject jsonObject);
	void setArray(JsonArray jsonArray);
	
}
