package it.uniroma2.ing.isw2.fmancini.swanalytics;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.errors.GitAPIException;


import it.uniroma2.ing.isw2.fmancini.swanalytics.git.CommitInfo;
import it.uniroma2.ing.isw2.fmancini.swanalytics.git.GitAPI;
import it.uniroma2.ing.isw2.fmancini.swanalytics.jira.IssueType;
import it.uniroma2.ing.isw2.fmancini.swanalytics.jira.JiraAPI;
import it.uniroma2.ing.isw2.fmancini.swanalytics.jira.Ticket;

public class ProjectAnalyzer {
	private GitAPI git;
	private JiraAPI jira;
	private String projectName;
	private String baseDir;
	
	
	public ProjectAnalyzer(String projectName, String baseDir) {
		this.projectName = projectName;
		if (!baseDir.substring(baseDir.length() - 1).contains("/")) {
			this.baseDir = baseDir + "/";
		} else {
			this.baseDir = baseDir;
		}
	}
	
	public void init() throws IOException, GitAPIException {
		// Check if the directory output exists
		File outputDirectory = new File(this.baseDir);
	    if (!outputDirectory.exists()){
	        outputDirectory.mkdir();
	    }
	    
	    // Check if the project directory exist
	    File projectDirectory = new File(this.baseDir + this.projectName.toLowerCase());
	    if (!projectDirectory.exists()) {
	    	projectDirectory.mkdir();
	    }

		this.git = new GitAPI(this.projectName, this.baseDir);
		this.git.init();
		this.jira = new JiraAPI(this.projectName);
	}
	
	public Map<String,Ticket> analyzeTickets(IssueType issueType) throws IOException, GitAPIException {
		Map<String,Ticket> tickets = null;
		tickets = this.jira.retriveTickets(issueType);
		List<CommitInfo> commits = this.git.getCommits();

		this.findFixedDate(tickets, commits);		
		return tickets;
	}
	
	private void findFixedDate(Map<String,Ticket> tickets, List<CommitInfo> commits) {
		for (CommitInfo commit : commits) {
			List<String> ticketIds = commit.findTicketIds(this.projectName.toUpperCase() + "-");
			
			for (String ticketId: ticketIds) { 
				Ticket ticket = tickets.get(ticketId);
				if (ticket != null && (ticket.getResolvedDate() == null || ticket.getResolvedDate().compareTo(commit.getDate()) < 0)) {
					ticket.setResolvedDate(commit.getDate());
				}
			}
		}
	}

}
