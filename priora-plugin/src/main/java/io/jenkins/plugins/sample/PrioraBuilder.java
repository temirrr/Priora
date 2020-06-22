package io.jenkins.plugins.sample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.EnvVars;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import jenkins.tasks.SimpleBuildStep;

import io.jenkins.plugins.sample.TestList;
import io.jenkins.plugins.sample.TestCaseClass;
import io.jenkins.plugins.sample.ScriptRunner;
import io.jenkins.plugins.sample.ScriptPython;

public class PrioraBuilder extends Builder implements SimpleBuildStep {
    public static final String testReportFile = "testsOrder";
    public static final String dataStoreFile = "data";
    public static final String scriptFile = "evaluation";
    public static final String execTests = "exectests";
    public static final String analysisFile = "analysis";

    private String[] path = new String[]{"priora"};

    private String prioraMethod = "NSGA"; // "NSGA", "Greedy"
    
    private int NUMBER_OF_BUILDS_TO_SEARCH = 1;

    private boolean restart = false;

    private long mxCommitInterv = 60000L;
    private long mnCommitInterv = 0L;

    private Double[] weights = new Double[]{0.33, 0.33, 0.33};

    @DataBoundConstructor
    public PrioraBuilder() {
        this.mxCommitInterv = 60000L;
        this.mnCommitInterv = 0L;
        this.prioraMethod = "NSGA";
        this.NUMBER_OF_BUILDS_TO_SEARCH = 1;
        this.restart = false;
        this.path = new String[]{"priora"};
        this.weights = new Double[]{0.33, 0.33, 0.33};
    }
    
    @DataBoundSetter
    public void setWeights(Double... weights) {
        this.weights = weights;
    } 
    
    @DataBoundSetter
    public void setPath(String... path) {
        this.path = path;
    } 

    @DataBoundSetter
    public void setRestart(boolean restart) {
        this.restart = restart;
    }
    
    @DataBoundSetter
    public void setPrioraMethod(String prioraMethod) {
        this.prioraMethod = prioraMethod;
    }
    
    @DataBoundSetter
    public void setNUMBER_OF_BUILDS_TO_SEARCH(int NUMBER_OF_BUILDS_TO_SEARCH) {
        this.NUMBER_OF_BUILDS_TO_SEARCH = NUMBER_OF_BUILDS_TO_SEARCH;
    }
    
    @DataBoundSetter
    public void setMxCommitInterv(long mxCommitInterv) {
        this.mxCommitInterv = mxCommitInterv;
    }
    
    @DataBoundSetter
    public void setMnCommitInterv(long mnCommitInterv) {
        this.mnCommitInterv = mnCommitInterv;
    }

    public Double[] getWeights() {
        return this.weights.clone();
    }

    public String[] getPath() {
        return this.path.clone();
    } 

    public boolean getRestart() {
        return this.restart;
    }
    
    public String getPrioraMethod() {
        return this.prioraMethod;
    }
    
    public int getNUMBER_OF_BUILDS_TO_SEARCH() {
        return this.NUMBER_OF_BUILDS_TO_SEARCH;
    }
    
    public long getMxCommitInterv() {
        return this.mxCommitInterv;
    }
    
    public long getMnCommitInterv() {
        return this.mnCommitInterv;
    }

    private FilePath getFilePath(FilePath workspace) {
        FilePath ret = workspace;
        String[] path = this.getPath();
        for (int i = 0; i < path.length; i++) {
            ret = ret.child(path[i]);
        }
        return ret;
    }

    private String getRelativeFilePath() {
        StringBuilder ret = new StringBuilder();
        ret.append("./");
        String[] path = this.getPath();
        for (int i = 0; i < path.length; i++) {
            ret.append(path[i]);
            ret.append("/");
        }
        return ret.toString();
    }
    
    public TestList getPrevTestResults(Run<?, ?> run, FilePath workspace, TaskListener listener) {
        TestList ret = new TestList();
        int included = 0;
        while (included < this.getNUMBER_OF_BUILDS_TO_SEARCH()) {
            run = (Run<?, ?>)run.getPreviousBuild();
            if (run == null) {
                break;
            }
            if (!run.isBuilding()) {
                AbstractTestResultAction tra = (AbstractTestResultAction)run.getAction((Class)AbstractTestResultAction.class);
                if (tra != null) {
                    Object o = tra.getResult();
                    if (o instanceof TestResult) {
                        listener.getLogger().printf("Using build #%d as reference%n", run.getNumber());
                        TestResult testRes = (TestResult)o;
                        List<CaseResult> cur = (List<CaseResult>)testRes.getFailedTests();
                        cur.addAll((List<CaseResult>)testRes.getPassedTests());
                        for (int j = 0; j < cur.size(); ++j) {
                            TestCaseClass now = new TestCaseClass((CaseResult)cur.get(j));
                            try {
                                ret.add_NO_DUPLICATE(now);
                            } catch(Throwable e) {
                                e.printStackTrace(listener.getLogger());
                            }
                        }
                        included++;
                    }
                }
            }
            else if (this.getNUMBER_OF_BUILDS_TO_SEARCH() == 1) 
                break;
        }
        return ret;
    }
    
    public static String inputStreamToString(InputStream inputStream) throws IOException {
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
    }
    
    public static TestList parseAcc(Run<?, ?> run, FilePath workspace, TaskListener listener, FilePath dir) throws InterruptedException, IOException {
        TestList tests = new TestList();
        try {
            JAXBContext context = JAXBContext.newInstance(io.jenkins.plugins.sample.TestList.class);
        
            Unmarshaller unmarshaller = context.createUnmarshaller();
            tests = (TestList) unmarshaller.unmarshal(dir.read());
        } catch (JAXBException e) {
            e.printStackTrace(listener.getLogger());
        } catch(InterruptedException e) {
            throw e;
        } catch(IOException e) {
            throw e;
        } catch(Throwable e) {
            e.printStackTrace(listener.getLogger());
        }
        
        return tests;
    }
    
    public static void updateAcc(Run<?, ?> run, FilePath workspace, TaskListener listener, TestList tests, FilePath dir) throws InterruptedException, IOException{
        try {
            dir.delete();
        }
        catch (Throwable throwable) {
            throwable.printStackTrace(listener.getLogger());
        }
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(io.jenkins.plugins.sample.TestList.class);
             
            //Create Marshaller
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
 
            //Required formatting??
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            
            StringWriter sw = new StringWriter();

            jaxbMarshaller.marshal(tests, sw);
            String xmlString = sw.toString();
            dir.write(xmlString, StandardCharsets.UTF_8.name());
        } catch (JAXBException e) 
        {
            e.printStackTrace(listener.getLogger());
        } catch(InterruptedException e) {
            throw e;
        } catch(IOException e) {
            throw e;
        } catch(Throwable e) {
            e.printStackTrace(listener.getLogger());
        }
    }
    
    public TestList update(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        TestList tests = new TestList();
        FilePath dir = this.getFilePath(workspace).child(dataStoreFile + ".xml");
        if (this.getRestart()) {
            updateAcc(run, workspace, listener, tests, dir);
            return tests;
        }
        try {
            if (!dir.exists() || this.getNUMBER_OF_BUILDS_TO_SEARCH() != 1) {
                tests = new TestList();
            }
            else {
                tests = parseAcc(run, workspace, listener, dir);
            }
        } catch(InterruptedException e) {
            throw e;
        } catch(IOException e) {
            throw e;
        } catch(Throwable e) {
            e.printStackTrace(listener.getLogger());
        }TestList prevTests = this.getPrevTestResults(run, workspace, listener);
        
        tests.sort();
        prevTests.sort();
        TestList combined = new TestList(); 
        try {
            for (int i = 0; i < tests.size(); i++)
                combined.add_NO_DUPLICATE(tests.get(i));
            for (int i = 0; i < prevTests.size(); i++)
                combined.add_NO_DUPLICATE(prevTests.get(i));
        } catch (Throwable e) {
            e.printStackTrace(listener.getLogger());
        }
                /*int i = 0;
        for (int j = 0; j < prevTests.size(); ++j) {
            if (i >= tests.size()) {
                combined.add(prevTests.get(j));
            }
            else {
                while (i < tests.size() && tests.get(i).compareTo(prevTests.get(j)) < 0) {
                    combined.add(tests.get(i));
                    ++i;
                }
                if (i < tests.size() && tests.get(i).compareTo(prevTests.get(j)) == 0) {
                    try {
                        tests.set(i, tests.get(i).merge(prevTests.get(j)));
                    } catch(Throwable e) {
                        e.printStackTrace(listener.getLogger());
                    }
                }
                else {
                    combined.add(prevTests.get(j));
                }
            }
        }
        while (i < tests.size()) {
            combined.add(tests.get(i));
            i++;
        }
        */
        //listener.getLogger().println(combined.size());
        prevTests.clear();
        tests.clear();
        updateAcc(run, workspace, listener, combined, dir);
        return combined;
    }

    public String truncate(TestList tests, List<String> order) throws InterruptedException{
        Map <String, Long> execTime = new TreeMap<>();
        
        for (int i = 0; i < tests.size(); i++) {
            execTime.put(tests.get(i).getCaseName(), tests.get(i).getAvgDuration());
        }
        
        long limit = (long)(Math.random() * (this.getMxCommitInterv() - this.getMnCommitInterv())) + this.getMnCommitInterv();
        limit = Math.min(limit, this.getMxCommitInterv()); 

        long sum = 0;
        StringBuffer content = new StringBuffer();
        for (String line : order) {
            long cur = execTime.getOrDefault(line, -1L);
            if (cur == -1L) {
                throw new InterruptedException("Something went wrong");
            }
            sum += cur;
            if (sum > limit) break;
            content.append(line + System.lineSeparator());
        }
        return content.toString();
    }
    
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        if (workspace == null) {
            throw new AbortException("no workspace");
        }
        
        this.analyzePrevBuild(run, workspace, launcher, listener);

        listener.getLogger().printf("Prioritizing...%n");
        listener.getLogger().printf("MinCommitInterval: %d, MaxCommitInterval: %d (in milliseconds)%n", this.mnCommitInterv, this.mxCommitInterv);
        listener.getLogger().println(this.getRelativeFilePath());
        
        TestList tests = this.update(run, workspace, launcher, listener);

        ScriptPython script = new ScriptPython();
        List<String> order = new ArrayList<>();
        try {
            order = script.runScript(tests.toListString(), this.getPrioraMethod(), this.getWeights(), scriptFile, listener);
        } catch(InterruptedException e) {
            throw e;
        } catch(IOException e) {
            throw e;
        } catch(Throwable e) {
            e.printStackTrace(listener.getLogger());
        }
        String content = "";
        FilePath dir = this.getFilePath(workspace).child(testReportFile + ".txt");

        try {
            if (order.isEmpty()) {
                dir.delete();
                return;
            }    
            content = this.truncate(tests, order);
            dir.write(content, StandardCharsets.UTF_8.name());
            //listener.getLogger().println(content);
            dir.chmod(0666);
        } catch(InterruptedException e) {
            throw e;
        } catch(IOException e) {
            throw e;
        } catch(Throwable e) {
            e.printStackTrace(listener.getLogger());
        }
        
        try {
            ScriptRunner.writeScriptRunner(this.getFilePath(workspace).child(execTests), this.getRelativeFilePath() + testReportFile + ".txt");    
        } catch(InterruptedException e) {
            throw e;
        } catch(IOException e) {
            throw e;
        } catch(Throwable e) {
            e.printStackTrace(listener.getLogger());
        }
    }

    public void analyzePrevBuild(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        FilePath dir = getFilePath(workspace).child(analysisFile + ".xml");
        while (true) {
            run = (Run<?, ?>)run.getPreviousBuild();
            if (run == null) {
                break;
            }
            if (!run.isBuilding()) {
                AbstractTestResultAction tra = (AbstractTestResultAction)run.getAction((Class)AbstractTestResultAction.class);
                if (tra != null) {
                    Object o = tra.getResult();
                    if (o instanceof TestResult) {
                        listener.getLogger().printf("Analyzing build #%d%n", run.getNumber());
                        TestResult testRes = (TestResult)o;
                        int buildNumber = run.getNumber();
                        long tests = testRes.getFailCount() + testRes.getPassCount();
                        long fails = testRes.getFailCount();
                        long duration = (long)(testRes.getDuration() * 1000L); //(in milliseconds)
                        
                        listener.getLogger().printf("Analysis %d %d %d %d%n", buildNumber, tests, fails, duration);
                        
                        Analysis info = new Analysis(buildNumber, tests, fails, duration);
                        try
                        {
                            JAXBContext jaxbContext = JAXBContext.newInstance(io.jenkins.plugins.sample.Analysis.class);
                            
                            //Create Marshaller
                            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                
                            //Required formatting??
                            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                            
                            StringWriter sw = new StringWriter();

                            jaxbMarshaller.marshal(info, sw);
                            String xmlString = sw.toString() + System.lineSeparator();
                           
                            File f = new File(dir.toURI());
                            FileOutputStream fos = new FileOutputStream(f, true);
                            OutputStreamWriter wrt = new OutputStreamWriter(fos, StandardCharsets.UTF_8.name());
                            wrt.append(xmlString);
                            wrt.flush();
                            wrt.close();
                            fos.close();
                        } catch (JAXBException e) {
                            e.printStackTrace(listener.getLogger());
                        } catch(InterruptedException e) {
                            throw e;
                        } catch(IOException e) {
                            throw e;
                        } catch(Throwable e) {
                            e.printStackTrace(listener.getLogger());
                        }
                        break;
                    }
                }
            }
        }
    }

    @Symbol("priora")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }
        
        public String getDisplayName() {
            return Messages.PrioraBuilder_DescriptorImpl_DisplayName();
        }
    }
}