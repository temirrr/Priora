package io.jenkins.plugins.sample;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Analysis")
public class Analysis {
    private long tests;
    private long fails;

    private int buildNumber;

    private long duration;

    public Analysis() {
        this(0, -1L, -1L, 0L);
    }

    public Analysis(int buildNumber, long tests, long fails, long duration) {
        this.buildNumber = buildNumber;
        this.tests = tests;
        this.fails = fails;
        this.duration = duration;
    }


    @XmlElement(name = "duration")
    public void setDuration(long duration) {
        this.duration = duration;
    }   

    public long getDuration() {
        return this.duration;
    }

    @XmlElement(name = "buildNumber")
    public void setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
    }

    public int getBuildNumber() {
        return this.buildNumber;
    }

    @XmlElement(name = "tests")
    public void setTests(long tests) {
        this.tests = tests;
    }

    @XmlElement(name = "fails")
    public void setFails(long fails) {
        this.fails = fails;
    }

    public long getFails() {
        return this.fails;
    }
    
    public long getTests() {
        return this.tests;
    }
}