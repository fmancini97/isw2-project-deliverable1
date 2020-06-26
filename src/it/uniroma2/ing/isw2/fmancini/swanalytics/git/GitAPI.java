/**
 * 
 */
package it.uniroma2.ing.isw2.fmancini.swanalytics.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author fmancini
 *
 */
public class GitAPI {

	private static String gitUrl = "https://github.com/apache/";
	
	private String projectName;
	
	private Git git;
	
	private String repoDir;
	
	private String baseDir;
	
	private List<CommitInfo> commits;
	
	
	public GitAPI(String projectName, String baseDir) {
		this.projectName = projectName.toLowerCase();
		if (!baseDir.substring(baseDir.length() - 1).contains("/")) {
			this.baseDir = baseDir + "/";
		} else {
			this.baseDir = baseDir;
		}
	}
	
	public void init() throws IOException, GitAPIException {
		this.repoDir = this.baseDir + projectName.toLowerCase() + "/" + projectName + "_repo";
		
		if (!Files.exists(Paths.get(repoDir))) {
			this.git = Git.cloneRepository()
					.setURI(gitUrl + projectName + ".git")
					.setDirectory(new File(repoDir))
					.call();
		} else {
			try (Git gitRepo = Git.open(new File( repoDir + "/.git"))){
				this.git = gitRepo;
				gitRepo.checkout().setName(this.getDefaultBranch()).call();
				gitRepo.pull().call();
				
			}
		}
	}
	
	private String getDefaultBranch() throws GitAPIException {
		List<Ref> branches = this.git.branchList().setListMode(ListMode.ALL).call();
		for (Ref branch: branches) {
			String branchName = branch.getName();
			if (branchName.startsWith("refs/heads/")) {
				return branchName.substring("refs/heads/".length());
			}
		}
		return "";
		
	}
	
	public String getRepoDir() {
		return repoDir;
	}

	public List<CommitInfo> getCommits() throws GitAPIException {
		if (this.commits != null) {
			return this.commits;
		}
		
		this.commits = new ArrayList<>();  
        Iterable<RevCommit> commitsLog = null;
        
		this.git.checkout().setName(this.getDefaultBranch()).call();
		commitsLog = git.log().call();
       
        for (RevCommit commit : commitsLog) {
        	ObjectId parentId = (commit.getParentCount() != 0) ? commit.getParent(0).getId() : null;
        	this.commits.add(new CommitInfo(commit.getId(), new Date(commit.getCommitTime() * 1000L), commit.getFullMessage(), parentId));
        }
                    
        return this.commits;
	}
	
	public List<RevCommit> listCommits(ObjectId startRelease, ObjectId endRelease) throws MissingObjectException, IncorrectObjectTypeException, GitAPIException {
		LogCommand logCommand = this.git.log();
		logCommand = (startRelease != null) ? logCommand.addRange(startRelease, endRelease) : logCommand.add(endRelease);
		
		Iterable<RevCommit> commitPath = logCommand.call();
		List<RevCommit> releaseCommits = new ArrayList<>();
		for (RevCommit commit : commitPath) {
			releaseCommits.add(0, commit);
		}
		return releaseCommits;
	}
	
	public List<RevCommit> listCommits(ObjectId endRelease) throws MissingObjectException, IncorrectObjectTypeException, GitAPIException {
		return this.listCommits(null, endRelease);
	}
	
}
