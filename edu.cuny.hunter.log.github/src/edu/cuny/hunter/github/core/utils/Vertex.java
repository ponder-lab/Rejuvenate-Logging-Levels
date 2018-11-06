package edu.cuny.hunter.github.core.utils;

class Vertex {
	private String method;
	private String file;
	private int commitIndex;

	public Vertex(int commitIndex, String method, String file) {
		this.method = method;
		this.commitIndex = commitIndex;
		this.file = file;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Vertex))
			return false;

		Vertex _obj = (Vertex) obj;
		return _obj.commitIndex == commitIndex && _obj.method == method && _obj.file == file;
	}
}