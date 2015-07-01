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
package edu.harvard.hms.dbmi.bd2k.irct.model.ontology;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Provides a base set of data type "primitives" that can be used to model data
 * values.
 * 
 * @author Jeremy R. Easton-Marks
 * @version 1.0
 */
public enum DataType {
	BOOLEAN {
		@Override
		public byte[] toBytes(Object value) {
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
			ByteBuffer buf = ByteBuffer.wrap(bytes);
			if (buf.getChar() == 'T') {
				return true;
			} else {
				return false;
			}
		}
	},
	BYTE {
		@Override
		public byte[] toBytes(Object value) {
			return new byte[] { (Byte) value };
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			return bytes[0];
		}
	},
	DOUBLE {
		@Override
		public byte[] toBytes(Object value) {
			byte b[] = new byte[8];

			ByteBuffer buf = ByteBuffer.wrap(b);
			buf.putDouble((Double) value);
			return b;
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			ByteBuffer buf = ByteBuffer.wrap(bytes);
			return buf.getDouble();
		}
	},
	FLOAT {
		@Override
		public byte[] toBytes(Object value) {
			byte b[] = new byte[4];

			ByteBuffer buf = ByteBuffer.wrap(b);
			buf.putFloat((Float) value);
			return b;
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			ByteBuffer buf = ByteBuffer.wrap(bytes);
			return buf.getFloat();
		}
	},
	INTEGER {
		@Override
		public byte[] toBytes(Object value) {
			byte b[] = new byte[4];

			ByteBuffer buf = ByteBuffer.wrap(b);
			buf.putInt((Integer) value);
			return buf.array();
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			ByteBuffer buf = ByteBuffer.wrap(bytes);
			return buf.getInt();
		}
	},
	LONG {
		@Override
		public byte[] toBytes(Object value) {
			byte b[] = new byte[8];

			ByteBuffer buf = ByteBuffer.wrap(b);
			buf.putLong((Long) value);
			return b;
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			ByteBuffer buf = ByteBuffer.wrap(bytes);
			return buf.getLong();
		}
	},
	STRING {
		@Override
		public byte[] toBytes(Object value) {
			String data = ((String) value).trim();
			return (data).getBytes(StandardCharsets.UTF_16);
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			return new String(bytes, StandardCharsets.UTF_16).trim();
		}
	},
	DATE {
		@Override
		public byte[] toBytes(Object value) {
			return ((String) value).getBytes();
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			return new String(bytes);
		}
	},
	DATETIME {
		@Override
		public byte[] toBytes(Object value) {
			return ((String) value).getBytes();
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			return new String(bytes);
		}
	},
	TIME {
		@Override
		public byte[] toBytes(Object value) {
			return ((String) value).getBytes();
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			return new String(bytes);
		}
	};

	/**
	 * Returns the byte array representation of the give object
	 * 
	 * @param value Object to convert
	 * @return Byte array
	 */
	public abstract byte[] toBytes(Object value);

	/**
	 * Returns the object representation of the given byte array
	 * 
	 * @param bytes Byte array
	 * @return Value
	 */
	public abstract Object fromBytes(byte[] bytes);
}
