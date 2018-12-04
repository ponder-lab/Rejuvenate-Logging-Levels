package edu.cuny.hunter.mylyngit.core.utils;

public class Vertex {
	private String method;
	private String file;
	private Vertex next;
	private Vertex prior;
	private int commit;

	public Vertex(String method, String file, int commit) {
		this.method = method;
		this.file = file;
		this.next = null;
		this.prior = null;
		this.commit = commit;
	}

	public void setNextVertex(Vertex next) {
		this.next = next;
	}

	public Vertex getNextVertex() {
		return this.next;
	}
	
	public void setPriorVertex(Vertex prior) {
		this.prior = prior;
	}

	public Vertex getPriorVertex() {
		return this.prior;
	}

	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * Because we can use method signature and file path to identify a method.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Vertex))
			return false;

		Vertex _obj = (Vertex) obj;
		return _obj.method.equals(method) && _obj.file.equals(file);
	}

	public String getFile() {
		return this.file;
	}

	public String getMethod() {
		return method;
	}
}