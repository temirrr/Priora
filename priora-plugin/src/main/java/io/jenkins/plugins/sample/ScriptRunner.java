package io.jenkins.plugins.sample;

import hudson.FilePath;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ScriptRunner {
    public static final String sep = System.lineSeparator();
    public static final String scriptPrefix = "#!/bin/bash " + sep + "input=\"";
    public static final String scriptSuffix = "\"" + sep + "while read -r line" + sep + "do" 
        + sep + "mvn -Dtest=\"$line\" surefire:test" + sep + "done < \"$input\"";
    public static void writeScriptRunner(FilePath dir, String fileName) throws Throwable {
        String script = scriptPrefix + fileName + scriptSuffix;
        try {
            dir.write(script, StandardCharsets.UTF_8.name());
            dir.chmod(0777);
        } catch(Throwable e) {
            throw e;
        }
    }
}