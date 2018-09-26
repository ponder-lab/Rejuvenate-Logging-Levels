package edu.cuny.hunter.github.core.analysis;

import java.io.IOException;
import java.util.Collection;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class Test {

    public static void main(String[] args) throws IOException, GitAPIException {
        
        Repository repo = new FileRepository("C:\\Users\\tangy\\eclipse-workspace\\Java-8-Stream-Refactoring.git");
        
        // get a list of all known heads, tags, remotes, ...
        Collection<Ref> allRefs = repo.getAllRefs().values();

        // a RevWalk allows to walk over commits based on some filtering that is defined
        try (RevWalk revWalk = new RevWalk(repo)) {
            for (Ref ref : allRefs) {
                revWalk.markStart(revWalk.parseCommit(ref.getObjectId()));
            }
            System.out.println("Walking all commits starting with " + allRefs.size() + " refs: " + allRefs);
            int count = 0;
            for (RevCommit commit : revWalk) {
                System.out.println("Commit: " + commit);
                count++;
            }
            System.out.println("Had " + count + " commits");
        }
    }

}
