package it.uniroma2.ing.isw2.fmancini.swanalytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;

import it.uniroma2.ing.isw2.fmancini.csv.CSVDAO;
import it.uniroma2.ing.isw2.fmancini.csv.CSVIncorrectNumValues;
import it.uniroma2.ing.isw2.fmancini.csv.CSVable;
import it.uniroma2.ing.isw2.fmancini.swanalytics.jira.IssueType;
import it.uniroma2.ing.isw2.fmancini.swanalytics.jira.Ticket;

/**
 * Performs the analysis of various types of Jira ticket of an Apache project
 * @author fmancini
 *
 */
public class ProjectWorker extends Thread {
	
	private static String baseDir = "output/";
	private static String savingCSV = "Generating CSV file";
	private static String csvSaved = "CSV file generated successfully";
	private static String csvLogTemplate = "[{0}] {1}";

	
	private String projectName;
	private ProjectAnalyzer projectAnalyzer;
	private Logger logger;
	private List<IssueType> issueTypes;
	
	
	
	public ProjectWorker(String projectName, List<IssueType> issueTypes) {
		this.projectName = projectName;
		this.projectAnalyzer = new ProjectAnalyzer(projectName, baseDir);
		this.logger = Logger.getLogger(ProjectWorker.class.getSimpleName() + "." + projectName);
		this.issueTypes = issueTypes;
	}
	
	/**
	 * Analyze Jira tickets of one or more types.
	 * The results are saved in csv files
	 */
	protected void analyzeTickets() {
		for (IssueType issueType : this.issueTypes) {
			logger.log(Level.INFO, "[{1}] Looking for issues of type {0} for the project {1}", new Object[] {issueType, projectName});
			Map<String, Ticket> tickets;
			
			try {
				tickets = projectAnalyzer.analyzeTickets(issueType);
			} catch (IOException | GitAPIException | JSONException e) {
				this.logger.log(Level.WARNING, "[{0}] Error while looking for issues of type {1}: {2}", new Object[] {this.projectName, issueType, e.getMessage()});
				return;
			}
				
			if (tickets == null) {
				logger.log(Level.SEVERE, "[{0}] Error while looking for {0} releases: ProjectAnalyzer.analyzeTickets() returned a null value", this.projectName);
				return;
			}
				
			logger.log(Level.INFO, csvLogTemplate, new Object[] {this.projectName, savingCSV});
				
			try {
				CSVDAO releasesCSV = new CSVDAO(baseDir + projectName.toLowerCase() + "/" + projectName.toLowerCase() + "_" + issueType.toString().toLowerCase());

				releasesCSV.open();
				this.saveToCSV(releasesCSV, new ArrayList<>(tickets.values()));
				releasesCSV.close();
					
			} catch (IOException | CSVIncorrectNumValues e) {
				this.logger.log(Level.WARNING, "[{0}] Error while saving tickets: {1}", new Object[] {this.projectName, e.getMessage()});
				return;

			}
			logger.log(Level.INFO, csvLogTemplate, new Object[] {this.projectName, csvSaved});
		}
	}
	
	private void saveToCSV(CSVDAO csvDAO, List<? extends CSVable> data) throws IOException, CSVIncorrectNumValues {
		List<List<? extends CSVable>> list = new ArrayList<>();
		list.add(data);
		csvDAO.saveToCSV(list);
	}
	

	@Override
	public void run() {
		try {
			projectAnalyzer.init();
		} catch (IOException | GitAPIException e) {
			this.logger.log(Level.WARNING, "[{0}] Error while initializing analysis: {1}", new Object[] {this.projectName, e.getMessage()});

		}
		
		try {
			this.analyzeTickets();
				
		} catch (SecurityException | IllegalArgumentException e) {
			logger.log(Level.SEVERE, "[{0}] Error while executing analyzeTickets task: {1}", new Object[] {this.projectName, e.getMessage()});

		}
		logger.log(Level.INFO, "[{0}] Analysis completed!", this.projectName);		
	}

}
