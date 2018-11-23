package edu.cuny.hunter.mylyngit.core.utils;

import java.util.HashSet;
import java.util.Set;

public class Vertex {
	private String method;
	private String file;
	private Set<Vertex> next;
	private Vertex head;

	public Vertex(String method, String file) {
		this.method = method;
		this.file = file;
		this.next = null;
		this.head = null;
	}

	public void setNextVertex(Vertex next, Vertex head) {
		this.setNextVertex(next);
		this.head = head;
	}

	public void setNextVertex(Vertex next) {
		if (next == null) {
			this.next = null;
			return;
		}

		if (this.next == null) {
			this.next = new HashSet<Vertex>();
			this.next.add(next);
		} else {
			this.next.add(next);
		}
	}

	public Vertex getHead() {
		return this.head;
	}

	public Set<Vertex> getNextVertex() {
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
		return _obj.method.equals(method) && _obj.file.equals(file);
	}

	public String getFile() {
		return this.file;
	}

	public String getMethod() {
		return method;
	}
}