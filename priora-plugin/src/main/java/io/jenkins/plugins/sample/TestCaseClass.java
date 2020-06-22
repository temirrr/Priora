package io.jenkins.plugins.sample;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.tasks.junit.CaseResult;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressFBWarnings(value="EQ_COMPARETO_USE_OBJECT_EQUALS", justification="We have to compare only names")
@XmlRootElement(name = "TestCaseClass")
public class TestCaseClass implements Comparable<TestCaseClass> {
    private String caseName;
    
    private long fail;
    private long run;
    private long avgDuration;
    
    public TestCaseClass() {
        this("");
    }

    public TestCaseClass(CaseResult cr) {
        this.caseName = cr.getClassName() + "#" + cr.getName();
        this.fail = cr.getFailCount();
        this.run = cr.getFailCount() + cr.getPassCount();
        this.avgDuration = (long)(cr.getDuration() * 1000);
    }
    
    public TestCaseClass(String caseName) {
        this.caseName = caseName;
        this.fail = 0L;
        this.run = 0L;
        this.avgDuration = 0L;
    }
    
    public String getCaseName() {
        return this.caseName;
    }

    @XmlElement(name = "caseName")
    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public long getFail() {
        return this.fail;
    }

    @XmlElement(name = "fail")
    public void setFail(long fail) {
        this.fail = fail;
    }

    public long getRun() {
        return this.run;
    }

    @XmlElement(name = "run")  
    public void setRun(long run) {
        this.run = run;
    }

    public long getAvgDuration() {
        return this.avgDuration;
    }

    @XmlElement(name = "avgDuration")  
    public void setAvgDuration(long avgDuration) {
        this.avgDuration = avgDuration;
    }

    public long getRun(boolean nonzero) {
        if (nonzero && this.run == 0L) {
            return 1L;
        }
        return this.run;
    }
    
    public long getPass() {
        return this.run - this.fail;
    }
        
    public TestCaseClass merge(TestCaseClass that) throws Exception{
        if (!this.getCaseName().equals(that.getCaseName())) {
            throw new Exception("These tests are not equal");
        }
        this.avgDuration = this.avgDuration * this.run + that.getAvgDuration() * that.getRun();
        this.run += that.getRun();
        this.fail += that.getFail();
        this.avgDuration /= this.getRun(true);
        return this;
    }

    @SuppressFBWarnings(value="HE_EQUALS_USE_HASHCODE", justification="If their caseNames are the same then they have the same hashCode too")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestCaseClass) {
            TestCaseClass test = (TestCaseClass)obj;
            if (this.compareTo(test) == 0)
                return true;
            return false;
        }
        return false;
    }
    
    @Override
    public int compareTo(TestCaseClass that) {
        String f = this.getCaseName();
        String s = that.getCaseName();
        for (int i = 0; i < Math.min(f.length(), s.length()); i++) {
            if (f.charAt(i) < s.charAt(i))
                return -1;
            if (f.charAt(i) > s.charAt(i))
                return 1;
        }
        if (f.length() < s.length())
            return -1;
        if (f.length() > s.length())
            return 1;
        return 0;
    }

    @Override
    public String toString() {
        return this.getCaseName() + " " + Long.toString(this.getFail()) + " " + Long.toString(this.getAvgDuration()) + " " + Long.toString(this.getRun());
    }
    
    public String getSourceFileName(String extension) {
        return this.caseName.replace('.', '/') + extension;
    }
}