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

   public static List<String> readStream(InputStream out) throws IOException {
      BufferedReader reader = new BufferedReader(new InputStreamReader(out, StandardCharsets.UTF_8));
      String line = null;
      List<String> ret = new ArrayList<>();
      try{
         while ((line = reader.readLine()) != null) {
            ret.add(line);
         }
         reader.close();
         out.close();
      } catch(IOException e){
         throw e;
      }
      return ret;
   }

   public List<String> runScript(List<String> tests, String method, Double[] weights, String scriptFile, TaskListener listener) throws InterruptedException, IOException {
      String path = System.getProperty("user.dir") + "/src/main/java/io/jenkins/plugins/sample/algorithm/";
      try {
         FileOutputStream fis = new FileOutputStream(path + "tests_info.txt", false);
         OutputStreamWriter writer = new OutputStreamWriter(fis, StandardCharsets.UTF_8);   
         for (int i = 0; i < tests.size(); i++) {
            writer.write(tests.get(i) + System.lineSeparator());
         }
         writer.flush();
         writer.close();
         fis.close();
      } catch(IOException e){
         throw e;
      } catch(Throwable e) {
         e.printStackTrace(listener.getLogger());
      }

      List<String> args = new ArrayList<>();
      for (int i = 0; i < weights.length; i++) {
         args.add(0, weights[i].toString());
      }
      args.add(0, method);
      args.add(0, path);
      args.add(0, path + scriptFile + ".py");
      args.add(0, "python3");

      listener.getLogger().println(System.getProperty("user.dir"));

      Process process = null;
      try{
         pb = new ProcessBuilder(args);
         //listener.getLogger().println("command: " + pb.command());
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

      List<String> ret = null;
      try {
         int ext = process.waitFor();
         listener.getLogger().println("Algorithm ended with exit code:" + ext);
         if (ext != 0) {
            try {
               ret = readStream(process.getErrorStream());
               for (int i = 0; i < ret.size(); i++) {
                  listener.getLogger().println(ret.get(i));
               }
            } catch(IOException e) {
               throw e;
            }
         }
         else {
            try {
               ret = readStream(new FileInputStream(path + "tests_ordering.txt"));
            } catch(IOException e) {
               throw e;
            }
         }
      } catch(InterruptedException e) {
         throw e;
      } 
      return ret;
   }
}