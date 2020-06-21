package io.jenkins.plugins.sample;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

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

public class PrioraBuilder extends Builder implements SimpleBuildStep {
    public static final String testReportFile = "tests_info";
    public static final String dataStoreFile = "data";
    
    private String prioraMethod = "NSGA";
    
    private int NUMBER_OF_BUILDS_TO_SEARCH = 1;

    private boolean restart = false;

    private long mxCommitInterv = 60000L;
    private long mnCommitInterv = 100L;

    @DataBoundConstructor
    public PrioraBuilder() {
        this.mxCommitInterv = 60000L;
        this.mnCommitInterv = 100L;
        this.prioraMethod = "NSGA";
        this.NUMBER_OF_BUILDS_TO_SEARCH = 1;
        this.restart = false;
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

    public boolean getRestart() {
        return this.restart;
    }
    
    public String getTestReportFile() {
        return this.testReportFile;
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
    
    public TestList getPrevTestResults(Run<?, ?> run, FilePath workspace, TaskListener listener) {
        TestList ret = new TestList();
        int included = 0;
        while(included < this.getNUMBER_OF_BUILDS_TO_SEARCH()) {
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
        return IOUtils.toString(inputStream, "UTF-8");
    }
    
    public static TestList parseAcc(Run<?, ?> run, FilePath workspace, TaskListener listener) {
        if (workspace == null) {
            return new TestList();
        }
        FilePath dir = workspace.child("priora").child(dataStoreFile + ".xml");
        TestList tests = new TestList();
        try {
            JAXBContext context = JAXBContext.newInstance(io.jenkins.plugins.sample.TestList.class);
        
            Unmarshaller unmarshaller = context.createUnmarshaller();
            tests = (TestList) unmarshaller.unmarshal(dir.read());
        } catch (JAXBException e) {
            e.printStackTrace(listener.getLogger());
        } catch (Throwable e) {
            e.printStackTrace(listener.getLogger());    
        }
        
        return tests;
    }
    
    public static void updateAcc(Run<?, ?> run, FilePath workspace, TaskListener listener, TestList tests) {
        FilePath dir = workspace.child("priora").child(dataStoreFile + ".xml");
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
            dir.write(xmlString, "UTF-8");
        } catch (JAXBException e) 
        {
            e.printStackTrace(listener.getLogger());
        } catch (Throwable e) {
            e.printStackTrace(listener.getLogger());    
        }
    }
    
    public void update(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) {
        TestList tests = new TestList();
        FilePath dir = workspace.child("priora").child(dataStoreFile + ".xml");
        if (this.getRestart()) {
            updateAcc(run, workspace, listener, tests);
            return;
        }
        try {
            if (!dir.exists() || this.getNUMBER_OF_BUILDS_TO_SEARCH() != 1) {
                tests = new TestList();
            }
            else {
                tests = parseAcc(run, workspace, listener);
            }
        }
        catch (Throwable throwable) {
            throwable.printStackTrace(listener.getLogger());
        }
        TestList prevTests = this.getPrevTestResults(run, workspace, listener);
        
        tests.sort();
        prevTests.sort();
        listener.getLogger().println(tests.size());
        listener.getLogger().println(prevTests.size());
        TestList combined = new TestList(); 
        int i = 0;
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
        listener.getLogger().println(combined.size());
        prevTests.clear();
        tests.clear();
        updateAcc(run, workspace, listener, combined);
    }
    
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        if (workspace == null) {
            throw new AbortException("no workspace");
        }
        
        listener.getLogger().printf("Prioritizing...%n");
        listener.getLogger().printf("MinCommitInterval: %d, MaxCommitInterval: %d (in milliseconds)%n", this.mnCommitInterv, this.mxCommitInterv);
        
        this.update(run, workspace, launcher, listener);

        //TODO: Integrate Algorithm
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