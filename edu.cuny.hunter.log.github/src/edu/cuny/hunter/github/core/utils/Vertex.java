package edu.cuny.hunter.github.core.utils;

import org.eclipse.jgit.revwalk.RevCommit;

class Vertex {
	private RevCommit commit;
	private String method;

	public Vertex(RevCommit commit, String method) {
		this.commit = commit;
		this.method = method;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Vertex))
			return false;

		Vertex _obj = (Vertex) obj;
		return _obj.commit == commit && _obj.method == method;
	}
}