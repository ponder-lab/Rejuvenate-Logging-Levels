package edu.cuny.hunter.mylyngit.core.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Graph {
	private Set<Vertex> vertices;
	private Set<Vertex> headVertices;
	private Set<Vertex> tailVertices;

	private HashMap<Vertex, Vertex> historicalMethodToCurrentMethods = new HashMap<>();

	public Graph() {
		vertices = new HashSet<>();
		headVertices = new HashSet<>();
		tailVertices = new HashSet<>();
	}

	public boolean addVertex(String method, String file, int commit) {
		Vertex vertex = new Vertex(method, file, commit);
		return vertices.add(vertex);
	}

	public boolean addVertex(Vertex v) {
		return vertices.add(v);
	}

	public boolean removeVertex(String method, String file, int commit) {
		Vertex vertex = new Vertex(method, file, commit);
		return vertices.remove(vertex);
	}

	public boolean removeVertex(Vertex v) {

		return vertices.remove(v);
	}

	public boolean addEdge(Vertex v1, Vertex v2) {
		if (!this.vertices.contains(v1) && !this.vertices.contains(v2))
			return false;
		v1.setNextVertex(v2);
		v2.setPriorVertex(v1);
		return true;
	}

	public boolean addEdge(String method1, String file1, int commit1, String method2, String file2, int commit2) {

		Vertex v1 = new Vertex(method1, file1, commit1);
		Vertex v2 = new Vertex(method2, file2, commit2);
		return this.addEdge(v1, v2);
	}

	public boolean removeEdge(Vertex v1, Vertex v2) {

		if (!this.vertices.contains(v1) && !this.vertices.contains(v2))
			return false;

		v1.setNextVertex(null);
		v2.setPriorVertex(null);
		return true;
	}

	public boolean removeEdge(String method1, String file1, int commit1, String method2, String file2, int commit2) {

		Vertex v1 = new Vertex(method1, file1, commit1);
		Vertex v2 = new Vertex(method2, file2, commit2);
		this.removeEdge(v1, v2);
		return true;
	}

	public Set<Vertex> getVertices() {
		return Collections.unmodifiableSet(vertices);
	}

	public Set<Vertex> getExitVertices() {
		this.tailVertices.clear();
		vertices.forEach(v -> {
			if (v.getNextVertex() == null)
				this.tailVertices.add(v);
		});
		return this.tailVertices;
	}

	private Set<Vertex> updateEntryVertices() {
		this.headVertices.clear();
		this.headVertices.addAll(this.vertices);
		HashSet<Vertex> nonEntryVertices = new HashSet<>();
		for (Vertex v : this.vertices) {
			nonEntryVertices.add(v.getNextVertex());
		}
		this.headVertices.removeAll(nonEntryVertices);
		return this.headVertices;
	}

	private Set<Vertex> getEntryVertices() {
		return this.headVertices;
	}

	public HashMap<Vertex, Vertex> computeHistoricalMethodToCurrentMethods() {
		this.updateEntryVertices();
		for (Vertex entry : this.headVertices) {
			LinkedList<Vertex> visitedVertices = new LinkedList();

			Vertex tmp = entry;
			// traverse all vertices
			while (tmp != null) {
				visitedVertices.add(tmp);
				tmp = tmp.getNextVertex();
			}

			Vertex exist = visitedVertices.getLast();
			visitedVertices.remove(exist);
			for (Vertex visitedVertex : visitedVertices) {
				this.historicalMethodToCurrentMethods.put(visitedVertex, exist);
			}
		}
		return this.historicalMethodToCurrentMethods;
	}

	/**
	 * Given a vertex, get its head in the linked list.
	 */
	private Vertex getHead(Vertex vertex) {
		while (vertex.getPriorVertex() != null)
			vertex = vertex.getPriorVertex();
		return vertex;
	}

	/**
	 * Given an exist node, remove the connected component in which it is.
	 */
	public void pruneGraphByExist(Vertex exist) {
		Vertex entry = this.getHead(exist);
		// traverse all vertices
		while (entry != null) {
			this.vertices.remove(entry);
			entry = entry.getNextVertex();
		}
		// update heads and tails
		this.updateEntryVertices();
		this.getExitVertices();
	}

	public HashMap<Vertex, Vertex> getHistoricalMethodToCurrentMethods() {
		return this.historicalMethodToCurrentMethods;
	}

}