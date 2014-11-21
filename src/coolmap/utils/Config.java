/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author sugang
 */
public class Config {

    public static final String COOLMAP_DIRECTORY = "coolmap-directory";
    public static final String PLUGIN_DIRECTORY = "plugin-directory";
    public static final String CONFIG_FILE = "config-file";
    public static final String WORKSPACE_DIRECTORY = "workspace-directory";
    public static final String VERSION = "version";
    
    
    private static JSONObject configObject = new JSONObject();
    private static HashMap<String, String> configHash = new HashMap<String, String>();
    
    private static boolean isInitialized = false;
    
    public static boolean isInitialized(){
        return isInitialized;
    }

    public static String getProperty(String key){
        return configHash.get(key);
    }
    
    public static void saveConfig() {
        try{
//            String workingDirectory;
//            workingDirectory = System.getProperty("user.dir");
//            configFile = workingDirectory + File.separator + "config.json";
            String savePath = getProperty(COOLMAP_DIRECTORY) + File.separator + "config.json";
            IOUtils.write(getJSONConfig().toString(), new FileOutputStream(new File(savePath)));
        }
        catch(Exception e){
            System.err.println("Config can not be saved");
        }
    }
    
    
    public static void initialize() {

        String rscriptPath;
        String workingDirectory;
        String pluginDirectory;
        String configFile;
        String workspaceDirectory;

        try {

            workingDirectory = System.getProperty("user.dir");

            if (workingDirectory != null) {
                if (workingDirectory.endsWith("/") || workingDirectory.endsWith("\\")) {
                    workingDirectory = workingDirectory.substring(0, workingDirectory.length() - 1); //strip last separator
                }
                configFile = workingDirectory + File.separator + "config.json";
                configHash.put(COOLMAP_DIRECTORY, workingDirectory);
            }
            else{
                throw new Exception("The working directory is null");
            }

            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            configObject = new JSONObject(sb.toString());


            //load pluginDirectory
            try {
                String isRelative = configObject.getJSONObject("plugin").optString("directory_relative", "true");
                String dir = configObject.getJSONObject("plugin").getString("directory");
                if(isRelative.equals("true")){
                    pluginDirectory = workingDirectory + File.separator + dir;
                }
                else{
                    pluginDirectory = dir;
                }
            } catch (JSONException ejson) {
                pluginDirectory = workingDirectory + File.separator + "plugin";
            }
            configHash.put(PLUGIN_DIRECTORY, pluginDirectory);
            System.out.println("Plugin folder:" + pluginDirectory);
            
            
            //load workspace directory
            try{
                workspaceDirectory = workingDirectory + File.separator + configObject.getJSONObject("workspace").getString("directory");
            }
            catch (JSONException ejson){
                workspaceDirectory = workingDirectory + File.separator + "workspace";
            }
//            System.out.println(workspaceDirectory);
            configHash.put(WORKSPACE_DIRECTORY, workspaceDirectory);
            
            configHash.put(VERSION, configObject.optString(VERSION));
            
            //if successful intialized
            isInitialized = true;

        } catch (Exception e) {
            System.err.println("Config file initialization error");
            //set some defaults
            
            
            e.printStackTrace();
        }

    }

    public static JSONObject getJSONConfig() {
        return configObject;
    }
    
    public static void printAttributes(){
//        System.out.println("Printing config attributes:");
        for(Map.Entry<String, String> entry : configHash.entrySet()){
            System.out.println( entry.getKey() + ": " + entry.getValue());
        }
//        System.out.println("");
    }
    

    public static void main(String[] args) {
        Config.initialize();
        printAttributes();
    }

}
