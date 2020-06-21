package io.jenkins.plugins.sample;

import java.util.List;
import java.io.EOFException;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

import io.jenkins.plugins.sample.TestCaseClass;


@XmlRootElement(name = "TestList")
public class TestList {
    private List<TestCaseClass> list;

    public TestList() {
        this.list = new ArrayList<>();
    }

    public TestList(List<TestCaseClass> list) {
        this.list = list;
    }

    public List<TestCaseClass> getList() {
        return this.list;
    }
    
    @XmlElement(name = "TestCaseClass")
    public void setList(List<TestCaseClass> list) {
        this.list.clear();
        this.list = list;
    }

    public void add(TestCaseClass test) {
        this.list.add(test);
    }

    public void add_NO_DUPLICATE(TestCaseClass test) throws Exception {
        if (this.list.contains(test)) {
            int i = this.list.indexOf(test);
            try {
                this.list.set(i, this.list.get(i).merge(test));
            } catch (Throwable e) {
                this.list.add(test);
                throw new Exception("Error in add_NO_DUPLICATE");
            }
        }
        else {
            this.list.add(test);
        }
    }

    public TestCaseClass get(int index) {
        return this.list.get(index);
    }

    public void set(int index, TestCaseClass test) {
        this.list.set(index, test);
    }

    public void sort() {
        this.list.sort(null);
    }

    public int size() {
        return this.list.size();
    }

    public void clear() {
        this.list.clear();
    }
}