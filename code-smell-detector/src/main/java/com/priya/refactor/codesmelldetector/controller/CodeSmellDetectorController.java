package com.priya.refactor.codesmelldetector.controller;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.priya.refactor.codesmelldetector.processor.DuplicateCodeDetector;
import com.priya.refactor.codesmelldetector.processor.LongMethodDetector;
import com.priya.refactor.codesmelldetector.processor.LongVariableListDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;



@Controller
public class CodeSmellDetectorController {

    public LongMethodDetector longMethodDetector;
    public LongVariableListDetector longVariableListDetector;
    public DuplicateCodeDetector duplicateCodeDetector;


    @Autowired
    public CodeSmellDetectorController(LongMethodDetector longMethodDetector,
                                       DuplicateCodeDetector duplicateCodeDetector,
                                       LongVariableListDetector longVariableListDetector) {
        this.longMethodDetector = longMethodDetector;
        this.duplicateCodeDetector = duplicateCodeDetector;
        this.longVariableListDetector = longVariableListDetector;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }
    @PostMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam("isRefactoredCode") String data, Model model) throws IOException {

        // Generate file content
        InputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

        // Set response headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "file.java");

        // Return response entity with file content
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(inputStream));
    }


    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        // check if file is empty
        if (file.isEmpty()) {
            model.addAttribute("errorMessage", "Please select a file to upload");
            return "index";
        }
        if(file.getOriginalFilename().contains(".java")){
            System.out.println("This is a java file " +file.getOriginalFilename());
        }else{
            model.addAttribute("errorMessage", "Please upload a .java file");
            return "index";
        }

        // reading the file contents
        InputStream inputStream;
        String[] fileContent;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            inputStream =  file.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to read the uploaded file");
            return "index";
        }
        fileContent = stringBuilder.toString().split("\n");

        // setting parser configurations
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setLexicalPreservationEnabled(true);
        JavaParser jParser = new JavaParser(parserConfiguration);

        // Parsing the file using Java Parser
        ParseResult<CompilationUnit> parseResult = jParser.parse(inputStream);
        CompilationUnit compilationUnit = parseResult.getResult().get();

        // check for long method in the parsed java file
        Boolean isLongMethod = longMethodDetector.detectLongMethod(compilationUnit,fileContent);
        StringBuilder longMethodResult = new StringBuilder();
        if(isLongMethod) {
            longMethodResult.append("Long method detected").append("\n");
            model.addAttribute("isLongMethod", longMethodResult.toString());
        }

        // check for long variable list in the parsed java file
        Boolean isLongVariable = longVariableListDetector.detectLongVariableList(compilationUnit);
        StringBuilder longVariableResult = new StringBuilder();
        if(isLongVariable) {
            longVariableResult.append("Long Variable Detected").append("\n");
            model.addAttribute("isLongVariable", longVariableResult.toString());
        }

        // check for duplicated code in the parsed java file and refactor the contents
        String refractoredCode = duplicateCodeDetector.refactorDuplicateCode(compilationUnit);
        StringBuilder duplicateCodeResult = new StringBuilder();
        if(refractoredCode != null) {
            duplicateCodeResult.append("Duplicate Code Detected").append("\n");
            model.addAttribute("isDuplicateCode", duplicateCodeResult.toString());
            model.addAttribute("isRefactoredCode",refractoredCode);

        }
        StringBuilder defaultMessage = new StringBuilder();
        if(!isLongMethod && !isLongVariable && refractoredCode == null){
            defaultMessage.append("No Code Smells Detected").append("\n");
            model.addAttribute("defaultMessage", defaultMessage.toString());
        }

        model.addAttribute("fileName", file.getOriginalFilename());
        return "index";
    }
}
