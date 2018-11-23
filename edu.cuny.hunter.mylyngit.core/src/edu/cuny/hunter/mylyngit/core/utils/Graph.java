package edu.cuny.hunter.mylyngit.core.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Graph {
	private Set<Vertex> vertices;
	private Set<Vertex> entryVertices;
	private Set<Vertex> exitVertices;
	
	private HashMap<Vertex, Vertex> historicalMethodToCurrentMethods = new HashMap<>();

	public Graph() {
		vertices = new HashSet<>();
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
		e.v1.setNextVertex(e.v2, e.v1.getHead());
		return true;
	}
	
	public void deleteOneConnectedComponent(Vertex entry) {
		if (!entry.getNextVertex().isEmpty()) {
			for (Vertex vertex : entry.getNextVertex()) {
				deleteOneConnectedComponent(vertex);
			}
			this.removeVertex(entry);
		}
	}

	public boolean addEdge(String method1, String file1, String method2, String file2) {
		return addEdge(new Edge(new Vertex(method1, file1), new Vertex(method2, file2)));
	}

	public boolean removeEdge(Edge e) {
		e.v1.setNextVertex(null);
		return true;
	}

	public boolean removeEdge(String method1, String file1, String method2, String file2) {
		return this.removeEdge(new Edge(new Vertex(method1, file1), new Vertex(method2, file2)));
	}

	public Set<Vertex> getVertices() {
		return Collections.unmodifiableSet(vertices);
	}

	public Set<Vertex> getExitVertices() {
		this.exitVertices.clear();
		vertices.forEach(v -> {
			if (v.getNextVertex() == null)
				this.exitVertices.add(v);
		});
		return this.exitVertices;
	}

	private Set<Vertex> updateEntryVertices() {
		this.entryVertices.clear();
		this.entryVertices.addAll(this.vertices);
		HashSet<Vertex> nonEntryVertices = new HashSet<>();
		for (Vertex v : this.vertices) {
			nonEntryVertices.addAll(v.getNextVertex());
		}
		this.entryVertices.removeAll(nonEntryVertices);
		return this.entryVertices;
	}
	
	private Set<Vertex> getEntryVertices(){
		return this.entryVertices;
	}

	public HashMap<Vertex, Vertex> computeHistoricalMethodToCurrentMethod() {
		for (Vertex v: this.entryVertices) {
			Set<Vertex> oneConntectedComponent = new HashSet<>();
			traverseComponent(oneConntectedComponent, v.getNextVertex());
			
		}
		return this.historicalMethodToCurrentMethods;
	}
	
	private void traverseComponent(Set<Vertex> oneConntectedComponent, Set<Vertex> vertices) {
		if (vertices == null || vertices.isEmpty()) return;
		oneConntectedComponent.addAll(vertices);
		for (Vertex v: vertices) {
			traverseComponent(oneConntectedComponent, v.getNextVertex());
		}
	}

}