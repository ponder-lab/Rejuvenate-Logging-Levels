package edu.cuny.hunter.mylyngit.core.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Graph {
	private Set<Vertex> vertices;
	private Set<Edge> edges;
	private Set<Vertex> entryVertices;
	private Set<Vertex> exitVertices;

	public Graph() {
		vertices = new HashSet<>();
		edges = new HashSet<>();
		entryVertices = new HashSet<>();
		exitVertices = new HashSet<>();
	}

	public boolean addVertex(String method, String file) {
		Vertex vertex = new Vertex(method, file);
		return vertices.add(vertex);
	}

	public boolean addVertex(Vertex v) {
		return vertices.add(v);
	}

	public boolean removeVertex(String method, String file) {
		Vertex vertex = new Vertex(method, file);
		return vertices.remove(vertex);
	}

	public boolean removeVertex(Vertex v) {
		return vertices.remove(v);
	}

	public boolean addEdge(Edge e) {
		if (!edges.add(e))
			return false;

		e.v1.setNextVertex(e.v2, e.v1.getHead());
		return true;
	}

	public boolean addEdge(String method1, String file1, String method2, String file2) {
		return addEdge(new Edge(new Vertex(method1, file1), new Vertex(method2, file2)));
	}

	public boolean removeEdge(Edge e) {
		if (!edges.remove(e))
			return false;

		e.v1.setNextVertex(null);

		return true;
	}

	public boolean removeEdge(String method1, String file1, String method2, String file2) {
		return this.removeEdge(new Edge(new Vertex(method1, file1), new Vertex(method2, file2)));
	}

	public Set<Vertex> getVertices() {
		return Collections.unmodifiableSet(vertices);
	}

	public Set<Edge> getEdges() {
		return Collections.unmodifiableSet(edges);
	}

	public Set<Vertex> getExitVertices() {
		this.exitVertices.clear();
		vertices.forEach(v -> {
			if (v.getNextVertex() == null)
				this.exitVertices.add(v);
		});
		return this.exitVertices;
	}

	public Set<Vertex> getEntryVertices() {
		this.entryVertices.clear();
		this.entryVertices.addAll(vertices);
		HashSet<Vertex> nonEntryVertices = new HashSet<>();
		for (Vertex v : this.vertices) {
			nonEntryVertices.add(v.getNextVertex());
		}
		this.entryVertices.removeAll(nonEntryVertices);
		return this.entryVertices;
	}

	public void printGraph() {
		this.getEntryVertices();
		this.entryVertices.forEach(v -> {
			System.out.print("v (m: " + v.getMethod() + ")");
			while (v.getNextVertex() != null) {
				v = v.getNextVertex();
				System.out.print("-> v (m: " + v.getMethod() + ")");
			}
			System.out.println();
		});
	}
}