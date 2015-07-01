/*
 *  This file is part of Inter-Resource Communication Tool (IRCT).
 *
 *  IRCT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  IRCT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with IRCT.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.harvard.hms.dbmi.bd2k.irct.model.query;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;

/**
 * The Join clause allows the user to combine 2 different subqueries on given
 * set of fields, or relationship using a given type of join.
 * 
 * A join can have either join two subQueries, or two fields.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class JoinClause extends ClauseAbstract {
	private SubQuery subQuery1;
	private SubQuery subQuery2;
	private JoinType joinType;
	private Path field1;
	private Path field2;
	private String relationship;

	/**
	 * Returns a JSONObject representation of the object. This returns only the
	 * attributes associated with this object and not their representation.
	 * 
	 * This is equivalent of toJson(1);
	 * 
	 * @return JSON Representation
	 */
	public JsonObject toJson() {
		return toJson(1);
	}

	/**
	 * Returns a JSONObject representation of the object. This returns only the
	 * attributes associated with this object and not their representation.
	 * 
	 * 
	 * @param depth
	 *            Depth to travel
	 * @return JSON Representation
	 */
	public JsonObject toJson(int depth) {
		depth--;
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		// TODO: FILL IN
		return jsonBuilder.build();
	}

	// -------------------------------------------------------------------------
	// SETTERS AND GETTERS
	// -------------------------------------------------------------------------

	/**
	 * Returns the first subQuery if it exists
	 * 
	 * @return SubQuery 1
	 */
	public SubQuery getSubQuery1() {
		return subQuery1;
	}

	/**
	 * Sets the first subQuery
	 * 
	 * @param subQuery1 SubQuery 1
	 */
	public void setSubQuery1(SubQuery subQuery1) {
		this.subQuery1 = subQuery1;
	}

	/**
	 * Returns the second subQuery if it exists
	 * 
	 * @return SubQuery 2
	 */
	public SubQuery getSubQuery2() {
		return subQuery2;
	}

	/**
	 * Sets the second subQuery
	 * @param subQuery2
	 */
	public void setSubQuery2(SubQuery subQuery2) {
		this.subQuery2 = subQuery2;
	}

	/**
	 * Returns the Join type
	 * 
	 * @return Join Type
	 */
	public JoinType getJoinType() {
		return joinType;
	}

	/**
	 * Sets the Join type
	 * 
	 * @param joinType Join Type
	 */
	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}

	/**
	 * Returns the first field if it exists
	 * 
	 * @return Field 1 
	 */
	public Path getField1() {
		return field1;
	}

	/**
	 * Sets the first field
	 * 
	 * @param field1 Field 1
	 */
	public void setField1(Path field1) {
		this.field1 = field1;
	}

	/**
	 * Returns the second field if it exists
	 * @return Field 2
	 */
	public Path getField2() {
		return field2;
	}

	/**
	 * Sets the second field
	 * 
	 * @param field2 Field 2
	 */
	public void setField2(Path field2) {
		this.field2 = field2;
	}

	/**
	 * Returns the relationship if it exists
	 * 
	 * @return Relationship
	 */
	public String getRelationship() {
		return relationship;
	}

	/**
	 * Sets the relationship if it exists
	 * 
	 * @param relationship Relationship
	 */
	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

}
