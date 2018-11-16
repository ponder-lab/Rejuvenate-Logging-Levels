package edu.cuny.hunter.mylyngit.core.utils;

public class Edge {

	Vertex v1, v2;

	public Edge(Vertex v1, Vertex v2) {
		this.v1 = v1;
		this.v2 = v2;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Edge))
			return false;

		Edge _obj = (Edge) obj;
		return _obj.v1.equals(v1) && _obj.v2.equals(v2);
	}

}
