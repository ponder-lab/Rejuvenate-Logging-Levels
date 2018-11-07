package edu.cuny.hunter.github.core.utils;

public class Vertex {
	private String method;
	private String file;
	private Vertex next;
	private Vertex head;

	public Vertex(String method, String file) {
		this.method = method;
		this.file = file;
		this.next = null;
		this.head = null;
	}

	public void setNextVertex(Vertex next, Vertex head) {
		this.next = next;
		this.head = head;
	}

	public void setNextVertex(Vertex next) {
		this.next = next;
	}

	public Vertex getHead() {
		return this.head;
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
		return _obj.method == method && _obj.file == file;
	}

	public String getFile() {
		return this.file;
	}

	public String getMethod() {
		return method;
	}
}