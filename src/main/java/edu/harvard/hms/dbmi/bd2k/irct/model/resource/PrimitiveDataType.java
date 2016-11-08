/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.resource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;

/**
 * Provides a base set of data type "primitives" that can be used to model data
 * values.
 * 
 * @author Jeremy R. Easton-Marks
 * @version 1.0
 */
public enum PrimitiveDataType implements DataType {
	BOOLEAN {
		@Override
		public byte[] toBytes(Object value) {
			if (value == null) {
				return new byte[] { (byte) Character.MIN_VALUE };
			}

			byte b[] = new byte[1];

			ByteBuffer buf = ByteBuffer.wrap(b);
			if ((Boolean) value) {
				buf.putChar('T');
			} else {
				buf.putChar('F');
			}
			return b;
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if ((bytes.length == 1)
					&& (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}

			ByteBuffer buf = ByteBuffer.wrap(bytes);
			if (buf.getChar() == 'T') {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public String getName() {
			return "boolean";
		}

		@Override
		public Pattern getPattern() {
			return Pattern.compile("^(true|false)$", Pattern.CASE_INSENSITIVE);
		}

		@Override
		public String getDescription() {
			return "A return can either be true or false";
		}

		@Override
		public DataType typeOf() {
			return null;
		}

		@Override
		public byte[] fromString(String value) {
			if (this.getPattern().matcher(value).matches()) {
				if (value.equalsIgnoreCase("true")) {
					return toBytes(true);
				} else {
					return toBytes(false);
				}
			}
			return null;
		}

		@Override
		public String toString(byte[] bytes) {
			if ((Boolean) fromBytes(bytes)) {
				return "true";
			} else {
				return "false";
			}
		}
	},
	BYTE {
		@Override
		public byte[] toBytes(Object value) {
			if (value == null) {
				return new byte[] { (byte) Character.MIN_VALUE };
			}

			return new byte[] { (Byte) value };
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if ((bytes.length == 1)
					&& (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}

			return bytes[0];
		}

		@Override
		public String getName() {
			return "byte";
		}

		@Override
		public Pattern getPattern() {
			return Pattern.compile("^.{1,1}$", Pattern.CASE_INSENSITIVE);
		}

		@Override
		public String getDescription() {
			return "A single character";
		}

		@Override
		public DataType typeOf() {
			return null;
		}

		@Override
		public byte[] fromString(String value) {
			if (this.getPattern().matcher(value).matches()) {
				return value.getBytes();
			}
			return null;
		}

		@Override
		public String toString(byte[] bytes) {
			return new String(bytes);
		}
	},
	DOUBLE {
		@Override
		public byte[] toBytes(Object value) {
			if (value == null) {
				return new byte[] { (byte) Character.MIN_VALUE };
			}

			byte b[] = new byte[8];

			ByteBuffer buf = ByteBuffer.wrap(b);
			buf.putDouble((Double) value);
			return b;
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if ((bytes.length == 1)
					&& (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}

			ByteBuffer buf = ByteBuffer.wrap(bytes);
			return buf.getDouble();
		}

		@Override
		public String getName() {
			return "double";
		}

		@Override
		public Pattern getPattern() {
			return Pattern.compile("^[0-9]{1,13}(\\.[0-9]*)$",
					Pattern.CASE_INSENSITIVE);
		}

		@Override
		public String getDescription() {
			return "A double value";
		}

		@Override
		public DataType typeOf() {
			return null;
		}

		@Override
		public byte[] fromString(String value) {
			if (this.getPattern().matcher(value).matches()) {
				return toBytes(value.getBytes());
			}
			return null;
		}

		@Override
		public String toString(byte[] bytes) {
			return String.valueOf((double) fromBytes(bytes));
		}
	},
	FLOAT {
		@Override
		public byte[] toBytes(Object value) {
			if (value == null) {
				return new byte[] { (byte) Character.MIN_VALUE };
			}

			byte b[] = new byte[4];

			ByteBuffer buf = ByteBuffer.wrap(b);
			buf.putFloat((Float) value);
			return b;
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if ((bytes.length == 1)
					&& (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}

			ByteBuffer buf = ByteBuffer.wrap(bytes);
			return buf.getFloat();
		}

		@Override
		public String getName() {
			return "float";
		}

		@Override
		public Pattern getPattern() {
			return Pattern.compile("^([+-]?\\d*\\.?\\d*)$",
					Pattern.CASE_INSENSITIVE);
		}

		@Override
		public String getDescription() {
			return "A float value";
		}

		@Override
		public DataType typeOf() {
			return null;
		}

		@Override
		public byte[] fromString(String value) {
			if (this.getPattern().matcher(value).matches()) {
				return toBytes(value.getBytes());
			}
			return null;
		}

		@Override
		public String toString(byte[] bytes) {
			return String.valueOf((float) fromBytes(bytes));
		}
	},
	INTEGER {
		@Override
		public byte[] toBytes(Object value) {
			if (value == null) {
				return new byte[] { (byte) Character.MIN_VALUE };
			}

			byte b[] = new byte[4];

			ByteBuffer buf = ByteBuffer.wrap(b);
			buf.putInt((Integer) value);
			return buf.array();
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if ((bytes.length == 1)
					&& (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}

			ByteBuffer buf = ByteBuffer.wrap(bytes);
			return buf.getInt();
		}

		@Override
		public String getName() {
			return "integer";
		}

		@Override
		public Pattern getPattern() {
			return Pattern.compile("^\\d+$", Pattern.CASE_INSENSITIVE);
		}

		@Override
		public String getDescription() {
			return "An integer value";
		}

		@Override
		public DataType typeOf() {
			return null;
		}

		@Override
		public byte[] fromString(String value) {
			if (this.getPattern().matcher(value).matches()) {
				return toBytes(value.getBytes());
			}
			return null;
		}

		@Override
		public String toString(byte[] bytes) {
			return String.valueOf((int) fromBytes(bytes));
		}
	},
	LONG {
		@Override
		public byte[] toBytes(Object value) {
			if (value == null) {
				return new byte[] { (byte) Character.MIN_VALUE };
			}

			byte b[] = new byte[8];

			ByteBuffer buf = ByteBuffer.wrap(b);
			buf.putLong((Long) value);
			return b;
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if ((bytes.length == 1)
					&& (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}

			ByteBuffer buf = ByteBuffer.wrap(bytes);
			return buf.getLong();
		}

		@Override
		public String getName() {
			return "long";
		}

		@Override
		public Pattern getPattern() {
			return Pattern.compile("^-?\\d{1,19}$", Pattern.CASE_INSENSITIVE);
		}

		@Override
		public String getDescription() {
			return "A long value";
		}

		@Override
		public DataType typeOf() {
			return null;
		}

		@Override
		public byte[] fromString(String value) {
			if (this.getPattern().matcher(value).matches()) {
				return toBytes(value.getBytes());
			}
			return null;
		}

		@Override
		public String toString(byte[] bytes) {
			return String.valueOf((long) fromBytes(bytes));
		}
	},
	STRING {
		@Override
		public byte[] toBytes(Object value) {
			if (value == null) {
				return new byte[] { (byte) Character.MIN_VALUE };
			}
			String data = ((String) value).trim();
			return (data).getBytes(StandardCharsets.UTF_16);
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if ((bytes.length == 1)
					&& (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}
			return new String(bytes, StandardCharsets.UTF_16).trim();
		}

		@Override
		public String getName() {
			return "string";
		}

		@Override
		public Pattern getPattern() {
			return Pattern.compile("^.*$", Pattern.CASE_INSENSITIVE);
		}

		@Override
		public String getDescription() {
			return "A string value";
		}

		@Override
		public DataType typeOf() {
			return null;
		}

		@Override
		public byte[] fromString(String value) {
			return value.getBytes();
		}

		@Override
		public String toString(byte[] bytes) {
			return new String(bytes);
		}
	},
	RESULTSET {
		@Override
		public byte[] toBytes(Object value) {
			if (value == null) {
				return new byte[] { (byte) Character.MIN_VALUE };
			}

			byte b[] = new byte[8];

			ByteBuffer buf = ByteBuffer.wrap(b);
			buf.putLong((Long) value);
			return b;
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if ((bytes.length == 1)
					&& (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}

			ByteBuffer buf = ByteBuffer.wrap(bytes);
			return buf.getLong();
		}

		@Override
		public String getName() {
			return "resultSet";
		}

		@Override
		public Pattern getPattern() {
			return Pattern.compile("^-?\\d{1,19}$", Pattern.CASE_INSENSITIVE);
		}

		@Override
		public String getDescription() {
			return "A resultset identifier";
		}

		@Override
		public DataType typeOf() {
			return null;
		}

		@Override
		public byte[] fromString(String value) {
			if (this.getPattern().matcher(value).matches()) {
				return toBytes(value.getBytes());
			}
			return null;
		}

		@Override
		public String toString(byte[] bytes) {
			return String.valueOf((long) fromBytes(bytes));
		}
	},
	COLUMN {
		@Override
		public byte[] toBytes(Object value) {
			if (value == null) {
				return new byte[] { (byte) Character.MIN_VALUE };
			}

			String data = ((String) value).trim();
			return (data).getBytes(StandardCharsets.UTF_16);
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if ((bytes.length == 1)
					&& (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}

			return new String(bytes, StandardCharsets.UTF_16).trim();
		}

		@Override
		public String getName() {
			return "column";
		}

		@Override
		public Pattern getPattern() {
			return Pattern.compile("^.*$", Pattern.CASE_INSENSITIVE);
		}

		@Override
		public String getDescription() {
			return "A column identifier";
		}

		@Override
		public DataType typeOf() {
			return null;
		}

		@Override
		public byte[] fromString(String value) {
			return value.getBytes();
		}

		@Override
		public String toString(byte[] bytes) {
			return new String(bytes);
		}
	},
	DATE {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");

		@Override
		public byte[] toBytes(Object value) {
			if (value == null) {
				return new byte[] { (byte) Character.MIN_VALUE };
			}

			// Convert to Date into String in correct format
			String dateString = dateFormat.format((Date) value);
			return fromString(dateString);
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if ((bytes.length == 1)
					&& (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}

			try {
				return dateFormat.parse(toString(bytes));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public String getName() {
			return "date";
		}

		@Override
		public Pattern getPattern() {
			return Pattern.compile(
					"^\\d{4}\\-(0?[1-9]|1[012])\\-(0?[1-9]|[12][0-9]|3[01])$",
					Pattern.CASE_INSENSITIVE);
		}

		@Override
		public String getDescription() {
			return "Date in yyyy-mm-dd format";
		}

		@Override
		public DataType typeOf() {
			return DATETIME;
		}

		@Override
		public byte[] fromString(String value) {

			if (this.getPattern().matcher(value).matches()) {
				return value.getBytes();
			}
			return null;
		}

		@Override
		public String toString(byte[] bytes) {
			return new String(bytes);
		}
	},
	DATETIME {

		DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

		@Override
		public byte[] toBytes(Object value) {
			if (value == null) {
				return new byte[] { (byte) Character.MIN_VALUE };
			}

			// Convert to Date into String in correct format
			String dateString = dateTimeFormat.format((Date) value);
			return fromString(dateString);
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if ((bytes.length == 1)
					&& (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}

			try {
				return dateTimeFormat.parse(toString(bytes));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public String getName() {
			return "dateTime";
		}

		@Override
		public Pattern getPattern() {
			return Pattern.compile(
					"^(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})$",
					Pattern.CASE_INSENSITIVE);
		}

		@Override
		public String getDescription() {
			return "Date in yyyy-mm-dd hh:mm:ss format. With hours in 24 hour format";
		}

		@Override
		public DataType typeOf() {
			return null;
		}

		@Override
		public byte[] fromString(String value) {

			if (this.getPattern().matcher(value).matches()) {
				return value.getBytes();
			}
			return null;
		}

		@Override
		public String toString(byte[] bytes) {
			return new String(bytes);
		}
	},
	TIME {
		DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");

		@Override
		public byte[] toBytes(Object value) {
			if (value == null) {
				return new byte[] { (byte) Character.MIN_VALUE };
			}

			// Convert to Date into String in correct format
			String dateString = timeFormat.format((Date) value);
			return fromString(dateString);
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if ((bytes.length == 1)
					&& (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}

			try {
				return timeFormat.parse(toString(bytes));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public String getName() {
			return "time";
		}

		@Override
		public Pattern getPattern() {
			return Pattern.compile("^(\\d{2}):(\\d{2}):(\\d{2})$",
					Pattern.CASE_INSENSITIVE);
		}

		@Override
		public String getDescription() {
			return "Time in hh:mm:ss format. With hours in 24 hour format";
		}

		@Override
		public DataType typeOf() {
			return DATETIME;
		}

		@Override
		public byte[] fromString(String value) {

			if (this.getPattern().matcher(value).matches()) {
				return value.getBytes();
			}
			return null;
		}

		@Override
		public String toString(byte[] bytes) {
			return new String(bytes);
		}
	},
	SUBQUERY {

		@Override
		public boolean validate(String value) {
			return true;
		}

		@Override
		public byte[] toBytes(Object value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getName() {
			return "subQuery";
		}

		@Override
		public Pattern getPattern() {
			return Pattern.compile(".*", Pattern.CASE_INSENSITIVE);
		}

		@Override
		public String getDescription() {
			return "A IRCT subquery";
		}

		@Override
		public DataType typeOf() {
			return null;
		}

		@Override
		public byte[] fromString(String value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString(byte[] bytes) {
			// TODO Auto-generated method stub
			return null;
		}
	},
	ARRAY {

		@Override
		public boolean validate(String value) {
			return true;
		}

		@Override
		public byte[] toBytes(Object value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getName() {
			return "array";
		}

		@Override
		public Pattern getPattern() {
			return Pattern.compile(".*", Pattern.CASE_INSENSITIVE);
		}

		@Override
		public String getDescription() {
			return "An array";
		}

		@Override
		public DataType typeOf() {
			return null;
		}

		@Override
		public byte[] fromString(String value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString(byte[] bytes) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	@Override
	public boolean validate(String value) {
		return getPattern().matcher(value).matches();
	}

	@Override
	public JsonObject toJson() {
		JsonObjectBuilder build = Json.createObjectBuilder();
		build.add("name", getName());
		build.add("pattern", getPattern().toString());
		build.add("description", getDescription());
		if (typeOf() != null) {
			build.add("typeof", typeOf().getName());
		}
		return build.build();
	}

}
