package io.jenkins.plugins.sample;

import hudson.FilePath;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ScriptRunner {
    private static String sep = System.lineSeparator();
    public static void writeScriptRunner(FilePath dir, String fileName, String analysisName) throws Throwable {
        String scriptPath = System.getProperty("user.dir") + "/src/main/java/io/jenkins/plugins/sample/exectests";
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(scriptPath), StandardCharsets.UTF_8);
            BufferedReader fr = new BufferedReader(isr);
            StringBuilder script = new StringBuilder();
            String line = fr.readLine();
            
            script.append(line + sep + sep);
            script.append("input=" + fileName + sep);
            script.append("output=" + analysisName + sep);
            while ((line = fr.readLine()) != null) {
                script.append(line + sep);
            }
            fr.close();
            isr.close();
            dir.write(script.toString(), StandardCharsets.UTF_8.name());
            dir.chmod(0777);
        } catch(Throwable e) {
            throw e;
        }
    }
}