package edu.cuny.hunter.github.core.analysis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

@SuppressWarnings("restriction")
public class Test {

    public static void main(String[] args) throws IOException, GitAPIException {
        
        Repository repo = new FileRepository("C:\\Users\\tangy\\eclipse-workspace\\Java-8-Stream-Refactoring\\.git");
        
        Git git = new Git(repo);
        
        List<String> logMessages = new ArrayList<String>();
        
        Iterable<RevCommit> log = git.log().call();
        RevCommit previousCommit = null;
        
        for (RevCommit commit : log) {
        	
            if (previousCommit != null) {
                AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser( previousCommit, git );
                AbstractTreeIterator newTreeIterator = getCanonicalTreeParser( commit, git );
                OutputStream outputStream = new ByteArrayOutputStream();
                try( DiffFormatter formatter = new DiffFormatter( outputStream ) ) {
                  formatter.setRepository( git.getRepository() );
                  formatter.format( oldTreeIterator, newTreeIterator );
                }
                String diff = outputStream.toString();
                System.out.println(diff);
            }
            
            System.out.println("LogCommit: " + commit);
            String logMessage = commit.getFullMessage();
            System.out.println("LogMessage: " + logMessage);
            logMessages.add(logMessage.trim());
            previousCommit = commit;
        }
        
        git.close();
    }
    
    private static AbstractTreeIterator getCanonicalTreeParser( ObjectId commitId , Git git) throws IOException {
        try( RevWalk walk = new RevWalk( git.getRepository() ) ) {
          RevCommit commit = walk.parseCommit( commitId );
          ObjectId treeId = commit.getTree().getId();
          try( ObjectReader reader = git.getRepository().newObjectReader() ) {
            return new CanonicalTreeParser( null, reader, treeId );
          }
        }
    }

}
