/**
 * 
 */
package it.uniroma2.ing.isw2.fmancini.swanalytics.jira;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import it.uniroma2.ing.isw2.fmancini.swanalytics.JSONTools;

/**
 * Allows you to interact with the Jira resources of Apache projects
 * @author fmancini
 *
 */
public class JiraAPI {
	private static String basicUrl = "https://issues.apache.org/jira/rest/api/2/";
	private String projectName;
	
	
	public JiraAPI(String projectName) {
		this.projectName = projectName;
	}
	
	/**
	 * Search tickets for a specific issue type
	 * @param issueType
	 * @return
	 * @throws IOException
	 */
	public Map<String,Ticket> retriveTickets(IssueType issueType) throws IOException {
		Map<String,Ticket> tickets = new HashMap<>();
		
		Integer j = 0;
		Integer i = 0;
		Integer total = 1;
	      //Get JSON API for closed bugs w/ AV in the project
		do {
			//https://issues.apache.org/jira/rest/api/2/search?jql=project%20%3D%20OPENJPA%20AND%20issueType%20%3D%20Bug%20AND(%20status%20=%20closed%20OR%20status%20=%20resolved%20)AND%20resolution%20=%20fixed%20&fields=key,resolutiondate,versions,created&startAt=0&maxResults=1000
			//Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
			j = i + 1000;
	        String url = basicUrl + "search?jql=project%20%3D%20"
	               + this.projectName + "%20AND%20issueType%20" + issueType.getType() + "%20AND(%20status%20=%20closed%20OR"
	               + "%20status%20=%20resolved%20)AND%20resolution%20=%20fixed%20&fields=key,resolutiondate,versions,created&startAt="
	               + i.toString() + "&maxResults=" + j.toString();
	        JSONObject json = JSONTools.readJsonFromUrl(url);
	        JSONArray issues = json.getJSONArray("issues");
	        total = json.getInt("total");
	        for (; i < total && i < j; i++) {
	           //Iterate through each bug
	       	 
	        	JSONObject issue = issues.getJSONObject(i%1000);
	        	tickets.put(issue.get("key").toString(), new Ticket(this.projectName, issue.get("key").toString(), issueType));
	        	
	        }  
	        
		} while (i < total);
		
		return tickets;
	      
	}

}
