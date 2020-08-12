# ISW2 Project - Deliverable 1
[![Build Status](https://travis-ci.com/francesco1997/isw2-project-deliverable1.svg?branch=master)](https://travis-ci.com/francesco1997/isw2-project-deliverable1)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=francesco1997_isw2-project-deliverable1&metric=alert_status)](https://sonarcloud.io/dashboard?id=francesco1997_isw2-project-deliverable1)

## Project compilation
To compile the project you need to use [Apache Ant](https://ant.apache.org/)
```bash
ant build
```
## Jira ticket analysis of Apache projects
The program allows you to perform analyses on the Jira tickets of an Apache project. The results are saved in csv files in `./output/`. The analyses to be performed and the projects to be analysed must be specified in the `config.json` file.

To start the project analysis, type:
```bash
ant SWAnalytics
```
### config.json
The file is an array of jsons, each of which represents a project to analyze.
```js
[{
    "project-name": "MAHOUT", // Name of the project
    "analysis-types": ["BUGS", "TICKET","NEWFEATURES"] // Types of tickets to analyze
},
...
]
```