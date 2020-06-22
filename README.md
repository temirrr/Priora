# Priora
CI-Integrated Test Prioritization Tool


## Plugin Documentary 

### Priora: Introduction

For Maven projects this plugin can:

* Accumulate Test Information;
* Priortize Tests using NSGA-II, Greedy or Random algorithm;
* Execute those tests;

### Priora: Dependencies
* Project must be buildable with Maven;
* JUnit Plugin;

### Priora: Integrate Into Pipeline

In Descriptive Pipeline:
`
        step([$class: 'PrioraBuilder'])
`
Add above step after build and add Junit Report step then Priora will prioritize tests and print the order.

Additional parameters can be specified:

* Order will be printed in file `$WORKSPACE/PATH/testsOrder.txt`
* Data will be accumulated in = `$WORKSPACE/PATH/data.xml`
* Script used for executing tests will be in `$WORKSPACE/exectests`
* `path` : The PATH can be specified using this parameter. Default - `['priora']`
* `prioraMethod` : By Default - `'NSGA'`, can be specified to `'Greedy'` or `'Random'`
* `NUMBER_OF_BUILDS_TO_SEARCH` : Default - `1`. Number of previous builds that we want to consider, must be a positive number, if set to `1`, we consider all previous build reports
* `restart` : Default - `false`, if you want to delete all accumulated data stored in Jenkins Workspace can be set to `true`
* `mxCommitInterv` : Default - `60000L`, Maximum Commit Interval;
* `mnCommitInterv` : Default - `100L`, Minimum Commit Interval, not sharp;


@temirrr

### Priora: Notes

* If project under build contains the same directory `/priora/` change it.
* Do not change the PATH to `/target/...`, because it accumulated data can be deleted when `mvn clean` is executed.
* If `NUMBER_OF_BUILDS_TO_SEARCH` is set to number more than `1`, we do not accumulate data in `$WORKSPACE/PATH/data.xml`, but reread JUnit reports again.


## How to use priora-plugin

### Soft 
Maven v(latest), Java (8)

### Run Jenkins Locally
In the ./priora-plugin/ directory

`
$ mvn hpi:run
`

### Directories
priora-plugin:
* src - source code of plugin
* target - built files of plugin
* test-project - test project, currently just simple-maven-project. can be replaced with benchmark
    - Must be separately pulled from or cloned https://github.com/cs453-team3/test-project
* work - Data from Jenkins run

### Test Project
https://github.com/cs453-team3/test-project (Must be cloned or pulled manually)
    
### Guidelines for Build Trigger
Register at https://ngrok.com

Following steps, launch  public URL and add Webhook in Settings of test-project repository

Should work, if any problem contact @fesiib

### TODO: Integrate Benchmark

### Pipeline
This Jenkins instance is based on Pipeline located in repo cs453-team3/test-project/Jenkinsfile

