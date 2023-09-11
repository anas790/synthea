package org.mitre.synthea.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.mitre.synthea.engine.Generator;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
public class Controller {

  @CrossOrigin(origins = "*", allowedHeaders = "*", exposedHeaders = "Content-Disposition")
  @RequestMapping(value = "/generate", produces = "application/zip")
  public ResponseEntity<StreamingResponseBody> zipFolder(@RequestParam Map<String,String> allRequestParams) {

    String outputFolderName = "./output/";

    // clean up the output folder before running the generator
    File outputFolder = new File(outputFolderName);
    if (outputFolder.exists()) {
      File[] files = outputFolder.listFiles();
      if (files != null) {
        for (File f : files) {
          f.delete();
        }
      }
    }
    
    


    // run the generator
    System.out.println(allRequestParams);
    Generator.GeneratorOptions options = new Generator.GeneratorOptions();
    if(allRequestParams.containsKey("populationSize")){
      int populationSize=Integer.parseInt(allRequestParams.get("populationSize")); 
      options.population = populationSize; 
    }
    if(allRequestParams.containsKey("seed")){
      long seed=Long.parseLong(allRequestParams.get("seed")); 
      options.seed = seed; 
    }
    if(allRequestParams.containsKey("minAge")){
      int minAge=Integer.parseInt(allRequestParams.get("minAge")); 
      options.minAge = minAge; 
    }
    if(allRequestParams.containsKey("maxAge")){
      int maxAge=Integer.parseInt(allRequestParams.get("maxAge")); 
      options.maxAge = maxAge; 
    }
    if(allRequestParams.containsKey("gender")){
      String gender=allRequestParams.get("gender"); 
      options.gender = gender; 
    }
    Generator generator = new Generator(options);
    generator.run();

    // creating a zip of the output folder in-memory and returning it
    File f = new File(outputFolderName);
    String zipFileName = new SimpleDateFormat("yyyy_MM_dd_hhmmss'_synthetic_data.zip'").format(new Date()); 
    if (f.exists() && f.isDirectory()) {
      return ResponseEntity
        .ok()
        .header("Content-Disposition", "attachment; filename=\""+zipFileName+"\"")
        .body(out -> {
          try (ZipOutputStream zipOutputStream = new ZipOutputStream(out)) {
            File folderToZip = new File(outputFolderName);
            zipFolderRecursive(folderToZip, folderToZip.getName() + "/", zipOutputStream);
          }
        });
    }
    else{
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

  }

  public void zipFolderRecursive(File folder, String folderPathInZip, ZipOutputStream zipOutputStream)
      throws IOException {
    if (folder.isDirectory()) {
      File[] files = folder.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.isDirectory()) {
            zipFolderRecursive(file, folderPathInZip + file.getName() + "/", zipOutputStream);
          } else {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
              ZipEntry zipEntry = new ZipEntry(folderPathInZip + file.getName());
              zipOutputStream.putNextEntry(zipEntry);
              StreamUtils.copy(fileInputStream, zipOutputStream);
              zipOutputStream.closeEntry();
            }
          }
        }
      }
    }
  }
}
