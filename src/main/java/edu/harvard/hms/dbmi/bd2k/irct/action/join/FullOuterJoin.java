/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action.join;

import java.util.Map;

import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinActionSetupException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.FileResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Joinable;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

/**
 * Performs a full outer join between two result sets
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class FullOuterJoin implements JoinAction {
	private ResultSet leftResultSet;
	private int leftColumnIndex;
	private ResultSet rightResultSet;
	private int rightColumnIndex;
	private LeftOuterJoin leftOuterJoin;

	private ResultSet results;

	@Override
	public void setup(Map<String, Object> parameters)
			throws JoinActionSetupException {
		try {
			this.leftResultSet = (ResultSet) parameters.get("LeftResultSet");
			this.rightResultSet = (ResultSet) parameters.get("RightResultSet");
			this.leftColumnIndex = this.leftResultSet
					.findColumn((String) parameters.get("LeftColumn"));
			this.rightColumnIndex = this.rightResultSet
					.findColumn((String) parameters.get("RightColumn"));

			this.leftOuterJoin = new LeftOuterJoin();
			leftOuterJoin.setup(parameters);

		} catch (Exception e) {
			e.printStackTrace();
			throw new JoinActionSetupException("Unable to setup join columns");
		}

	}

	@Override
	public void run() {
		try {
			FileResultSet computedResults = new FileResultSet();

			int baseColumn = leftResultSet.getColumnSize() - 1;

			// Perform a left outer join to get all the left, and matches

			leftOuterJoin.run();
			computedResults = (FileResultSet) leftOuterJoin.getResults();

			// Loop through Result 2 make sure that right joins occur bringing
			// in all right matches
			rightResultSet.beforeFirst();

			while (rightResultSet.next()) {
				Object rs2RowMatchObj = ((Joinable) rightResultSet)
						.getObject(leftColumnIndex);
				boolean match = false;

				// Reset resultset before looping through it
				leftResultSet.beforeFirst();
				while (leftResultSet.next()) {
					if (((Joinable) leftResultSet).getObject(leftColumnIndex)
							.equals(rs2RowMatchObj)) {
						match = true;
						break;
					}
				}

				// If a match isn't found then add it to the results
				if (!match) {
					// Add a new row
					computedResults.appendRow();
					// Set the join column value
					computedResults.updateObject(leftColumnIndex,
							rs2RowMatchObj);

					// Copy RS2 values over
					for (int rsColumnIterator = 0; rsColumnIterator < rightResultSet
							.getColumnSize(); rsColumnIterator++) {
						if (rsColumnIterator != rightColumnIndex) {
							computedResults.updateObject(baseColumn
									+ rsColumnIterator,
									((Joinable) rightResultSet)
											.getObject(rsColumnIterator));
						}
					}
				}
			}
			computedResults.beforeFirst();

			this.results = computedResults;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public ResultSet getResults() {
		return this.results;
	}

	@Override
	public String getType() {
		return "Full Outer Join";
	}
}
