# Priora
CI-Integrated Test Prioritization Tool


## How to use priora-plugin

### Soft 
Maven v(latest), Java (8)

### Run Jenkins Locally
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

