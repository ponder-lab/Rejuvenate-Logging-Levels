package edu.cuny.hunter.github.core.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Graph {
	private Set<Vertex> vertices;
	private Set<Edge> edges;
	private Map<Vertex, Set<Edge>> adjList;
	private Set<Vertex> entryVertices;

	public Graph() {
		vertices = new HashSet<>();
		edges = new HashSet<>();
		adjList = new HashMap<>();
		entryVertices = new HashSet<>();
	}

	public boolean addVertex(int commitIndex, String method, String file) {
		Vertex vertex = new Vertex(commitIndex, method, file);
		entryVertices.add(vertex);
		return vertices.add(vertex);
	}

	public boolean addVertex(Vertex v) {
		entryVertices.add(v);
		return vertices.add(v);
	}

	public boolean removeVertex(int commitIndex, String method, String file) {
		Vertex vertex = new Vertex(commitIndex, method, file);
		entryVertices.remove(vertex);
		return vertices.remove(vertex);
	}

	public boolean removeVertex(Vertex v) {
		entryVertices.remove(v);
		return vertices.remove(v);
	}

	public boolean addEdge(Edge e) {
		if (!edges.add(e))
			return false;

		adjList.putIfAbsent(e.v1, new HashSet<>());
		adjList.putIfAbsent(e.v2, new HashSet<>());

		adjList.get(e.v1).add(e);
		adjList.get(e.v2).add(e);

		entryVertices.remove(e.v2);

		return true;
	}

	public boolean addEdge(int commitIndex1, String method1, String file1, int commitIndex2, String method2,
			String file2) {
		return addEdge(new Edge(new Vertex(commitIndex1, method1, file1), new Vertex(commitIndex2, method2, file2)));
	}

	public boolean removeEdge(Edge e) {
		if (!edges.remove(e))
			return false;
		Set<Edge> edgesOfV1 = adjList.get(e.v1);
		Set<Edge> edgesOfV2 = adjList.get(e.v2);

		if (edgesOfV1 != null)
			edgesOfV1.remove(e);
		if (edgesOfV2 != null)
			edgesOfV2.remove(e);

		entryVertices.add(e.v1);

		return true;
	}

	public boolean removeEdge(int commitIndex1, String method1, String file1, int commitIndex2, String method2,
			String file2) {
		return removeEdge(new Edge(new Vertex(commitIndex1, method1, file1), new Vertex(commitIndex2, method2, file2)));
	}

	public Set<Vertex> getAdjVertices(Vertex v) {
		if (!adjList.containsKey(v)) return new HashSet<>();
		return adjList.get(v)
				.stream()
				.map(e -> e.v1.equals(v) ? e.v2 : e.v1)
				.collect(Collectors.toSet());
	}

	public Set<Vertex> getVertices() {
		return Collections.unmodifiableSet(vertices);
	}

	public Set<Edge> getEdges() {
		return Collections.unmodifiableSet(edges);
	}

	public Map<Vertex, Set<Edge>> getAdjList() {
		return Collections.unmodifiableMap(adjList);
	}
	
	public Set<Vertex> getEntryVertices() {
		return this.entryVertices;
	}
	
	public void printGraph() {
		entryVertices.forEach(v -> {
			System.out.print("v (m: " + v.getMethod() +")");
			Set<Vertex> adjVertexs = this.getAdjVertices(v);
			while(!adjVertexs.isEmpty()) {
				Vertex adjVertex = adjVertexs.iterator().next();
				System.out.print("-> v (m: " + adjVertex.getMethod() +")");
				adjVertexs = this.getAdjVertices(adjVertex);
			}
			System.out.println();
		});
	}
}