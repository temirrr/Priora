package io.jenkins.plugins.sample;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import hudson.model.TaskListener;

public class ScriptPython {
   
   ProcessBuilder pb;
   
   public ScriptPython() {
      this.pb = null;
   }

   public List<String> runScript(List<String> args, String method, String scriptFile, TaskListener listener) throws InterruptedException, IOException {
      
      args.add(0, method);
      String path = System.getProperty("user.dir") + "/src/main/java/io/jenkins/plugins/sample/";
      args.add(0, path + scriptFile + ".py");
      args.add(0, "python3");

      listener.getLogger().println(System.getProperty("user.dir"));

      Process process = null;
      try{
         pb = new ProcessBuilder(args);
         listener.getLogger().println("command: " + pb.command());
         process = pb.start();
      } catch(IOException e) {
         listener.getLogger().println("Exception IO in pb.start");
         throw e;
      } catch(Throwable e) {
         e.printStackTrace(listener.getLogger());
      }
      if (process == null) {
         listener.getLogger().println("Could not execute the Evaluation");
         return new ArrayList<>();
      }
      InputStream stdout = process.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8));
      String line;
      List<String> ret = new ArrayList<>();
      try{
         while ((line = reader.readLine()) != null) {
            ret.add(line);
         }
         reader.close();
         stdout.close();
      } catch(IOException e){
         listener.getLogger().println("Exception in reading output"+ e.toString());
         throw e;
      }
      return ret;
   }
}