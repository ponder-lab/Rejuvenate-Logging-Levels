/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.join;

import java.util.Map;

import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinActionSetupException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.FileResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSetImpl;

/**
 * Performs a right outer join between two result sets
 * 
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class RightOuterJoin implements JoinImplementation {
	private ResultSet leftResultSet;
	private int leftColumnIndex;
	private ResultSet rightResultSet;
	private int rightColumnIndex;

	private Result results;

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
		} catch (Exception e) {
			e.printStackTrace();
			throw new JoinActionSetupException("Unable to setup join columns");
		}

	}

	@Override
	public Result run(Result result) throws ResultSetException, PersistableException {
		this.results = result;
		FileResultSet computedResults = (FileResultSet) result.getData();
		computedResults.appendColumn(leftResultSet.getColumn(leftColumnIndex));

		for (int rsColumnIterator = 0; rsColumnIterator < leftResultSet
				.getColumnSize(); rsColumnIterator++) {
			if (rsColumnIterator != leftColumnIndex) {
				computedResults.appendColumn(leftResultSet
						.getColumn(rsColumnIterator));
			}
		}
		for (int rsColumnIterator = 0; rsColumnIterator < rightResultSet
				.getColumnSize(); rsColumnIterator++) {
			if (rsColumnIterator != rightColumnIndex) {
				computedResults.appendColumn(rightResultSet
						.getColumn(rsColumnIterator));
			}
		}
		int baseColumn = leftResultSet.getColumnSize() - 1;

		rightResultSet.beforeFirst();
		while (rightResultSet.next()) {
			Object rs2RowMatchObj = ((ResultSetImpl) rightResultSet)
					.getObject(leftColumnIndex);

			// Add a new row
			computedResults.appendRow();
			// Set the join column value
			computedResults.updateObject(leftColumnIndex, rs2RowMatchObj);

			// Copy RS2 values over
			for (int rsColumnIterator = 0; rsColumnIterator < rightResultSet
					.getColumnSize(); rsColumnIterator++) {
				if (rsColumnIterator != rightColumnIndex) {
					computedResults.updateObject(baseColumn + rsColumnIterator,
							((ResultSetImpl) rightResultSet)
									.getObject(rsColumnIterator));
				}
			}

			leftResultSet.beforeFirst();
			while (leftResultSet.next()) {
				if (((ResultSetImpl) leftResultSet).getObject(leftColumnIndex)
						.equals(rs2RowMatchObj)) {

					// Copy RS1 values over
					for (int rsColumnIterator = 0; rsColumnIterator < leftResultSet
							.getColumnSize(); rsColumnIterator++) {

						if (rsColumnIterator != leftColumnIndex) {
							computedResults.updateObject(rsColumnIterator,
									((ResultSetImpl) leftResultSet)
											.getObject(rsColumnIterator));
						}
					}

				}
			}
		}
		computedResults.beforeFirst();

		this.results.setResultStatus(ResultStatus.COMPLETE);
		this.results.setData(computedResults);
		return this.results;
	}

	@Override
	public Result getResults(Result result) {
		return this.results;
	}

	@Override
	public String getType() {
		return "Right Outer Join";
	}

	@Override
	public ResultDataType getJoinDataType() {
		return ResultDataType.TABULAR;
	}
}
