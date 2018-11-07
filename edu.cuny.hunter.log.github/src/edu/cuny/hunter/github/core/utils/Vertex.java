package edu.cuny.hunter.github.core.utils;

public class Vertex {
	private String method;
	private String file;
	private int commitIndex;
	private Vertex next;

	public Vertex(int commitIndex, String method, String file) {
		this.method = method;
		this.commitIndex = commitIndex;
		this.file = file;
		this.next = null;
	}
	
	public void setNextVertex(Vertex next) {
		this.next = next;
	}
	
	public Vertex getNextVertex() {
		return this.next;
	}
	
	public void setFile(String file) {
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
	
	public String getFile() {
		return this.file;
	}
	
	public String getMethod() {
		return method;
	}
}