package ca.sqrlab.arc.experiments;

import java.util.List;

public class ReportBuilder {
	
	public static final String COLOUR_MAIN = "#003366";
	
	public static final String URL = "https://github.com/sqrlab/ARC2";

	private String head;
	
	private String body;
	
	public ReportBuilder() {
		clear();
	}
	
	public ReportBuilder clear() {
		this.head = "";
		this.body = "";
		
		return this;
	}
	
	public ReportBuilder addPageTitle(String title) {
		if (title == null) {
			title = "";
		}
		this.head += "<title>" + title + "</title>";
		return this;
	}
	
	public ReportBuilder addHeaderElement(String html) {
		if (html == null) {
			return this;
		}
		this.head += html;
		return this;
	}
	
	public ReportBuilder addExperimentGroup(ExperimentGroup group,
			ExperimentResult[][] results) {
		
		// Nothing to do
		if (group == null || results == null) {
			return this;
		}
		
		Experiment[] experiments = group.getExperiments();
		int n = experiments.length, runs = group.getRunsPerExperiment();
		
		// Add the basic HTML
		this.body += "<div class=\"content\"><h2 class=\"c-main\">Experiment "
				+ "Group: " + group.getName() + "</h2><p>ARC Path: " + group.getArcPath()
				+ "</p><p>Project Path: " + group.getProjectPath() + "</p><p>"
				+ "Runs per Experiment: " + runs + "</p>";
		
		String tr = "<tr><td>%s</td><td>%s</td>"
				+ "<td>%d</td><td>%dms</td><td>%dms</td><td>%d</td>"
				+ "<td>%d</td><td>%d</td></tr>";
		
		// Add all the experiment HTML
		for (int i = 0; i < n; i ++) {
			Experiment e = experiments[i];
			String err = "";
			this.body += "<h3 class=\"c-main\">Experiment " + (i + 1) + " of "
					+ n + ": " + e.getName() + "</h3><table style=\"min-width: 100%;"
					+ "overflow: auto;\"><tr><th>Run</th><th>Fixed?</th><th>"
					+ "Generations</th><th>ARC Time</th><th>GA Time</th><th>"
					+ "Test-Suite Runs</th><th>Created</th><th>Evaluated</th></tr>";
			
			// Add each run's HTML
			double count = 0, tInEval = 0, tInCreated = 0, tGens = 0, tFixed = 0;
			double tTSE = 0, tTime = 0, tGATime = 0;
			for (int j = 0; j < runs; j ++) {
				
				ExperimentResult r = results[i][j];
				if (r == null) {
					continue;
				}
				count ++;
				
				// Run | Fixed | Generations | ARC Time | GA Time | Test-Suite
				// ... Runs | Created | Evaluated
				
				// Add to the totals
				tInEval += r.getIndividualsEvaluatedCount();
				tInCreated += r.getIndividualsGeneratedCount();
				tGens += r.getGenerationCount();
				boolean fixed = r.foundFix();
				if (fixed) {
					tFixed ++;
				}
				tTime += r.getARCExecutionTime();
				tGATime += r.getGAExecutionTime();
				tTSE += r.getTestSuiteExecutions();
				
				// Add the row
				this.body += String.format(tr, "" + (j + 1), fixed? "Yes" : "No",
								r.getGenerationCount(), r.getARCExecutionTime(),
								r.getGAExecutionTime(), r.getTestSuiteExecutions(),
								r.getIndividualsGeneratedCount(),
								r.getIndividualsEvaluatedCount());
				
				// Add any errors
				List<String> errors = r.getErrors();
				if (errors == null || errors.isEmpty()) {
					continue;
				}
				err += "<div><h5 class=\"c-main\">Run " + (j + 1) + "</h5>";
				for (String error : errors) {
					err += "<div class=\"content\">" + error + "</div>";
				}
				err += "</div>";
			}
			
			// We didn't have any run info
			if (count == 0) {
				this.body += "</table>";
				continue;
			}
			
			// Add the total column
			this.body += String.format(tr, "<b>Total:</b>", "" + tFixed,
					(int) tGens, (int) tTime, (int) tGATime, (int) tTSE,
					(int) tInCreated, (int) tInEval);
			
			// Add the average column
			tFixed /= count;
			tGens /= count;
			tTime /= count;
			tGATime /= count;
			tTSE /= count;
			tInCreated /= count;
			tInEval /= count;
			this.body += String.format(tr, "<b>Average:</b>", (tFixed * 100) + "%",
					(int) tGens, (int) tTime, (int) tGATime, (int) tTSE,
					(int) tInCreated, (int) tInEval) + "</table>";
			
			// Add the HTML after the table
			if (!err.isEmpty()) {
				this.body += "<h4 class=\"c-main\">Errors</h4><div>" + err + "</div>";
			}
			this.body += "<hr />";
		}
		
		this.body += "</div>";
		
		return this;
	}
	
	public String getHTML() {
		
		String html = "<!DOCTYPE html><html lang=\"en-ca\"><head>"
				+ "<meta name=\"viewport\", content=\"width=device-width, "
				+ "initial-scale=1.0\" /><style>* {box-sizing: border-box;} "
				+ "body {font-family: Arial, sans-serif;padding: 0;margin: 0;}"
				+ " .c-main {color: " + COLOUR_MAIN + ";} .content {padding: 8px;}"
				+ " table, tr, th, td {border-collapse: collapse;border: 1px solid "
				+ COLOUR_MAIN + ";} td, th {padding: 8px;} th {color: white;background: "
				+ COLOUR_MAIN + ";}</style>"
				+ head + "</head><body><div class=\"content c-main\"><h1><a "
				+ "target=\"_blank\" href=\"" + URL + "\">ARC2</a>: "
				+ "Experiment Report</h1></div><div>" + body
				+ "</div></body></html>";
		
		return html;
	}
}
