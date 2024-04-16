package com.priya.refactor.codesmelldetector.processor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.List;

public class LongMethodDetector {

    /**
     * This method used the file content and the Java Parser's compilation unit to detect the number of lines in a function
     * @param compilationUnit
     * @param fileContent
     * @return
     */
   public Boolean detectLongMethod(CompilationUnit compilationUnit, String[] fileContent) {
        if (compilationUnit != null) {

            List<MethodDeclaration> methods = compilationUnit.findAll(MethodDeclaration.class);
            if(methods == null){
                return false;
            }
            for (MethodDeclaration method : methods) {

                int methodStartLineNum = method.getBody().get().getRange().get().begin.line -1;
                int methodEndLineNum = method.getBody().get().getRange().get().end.line -1;
                int emptyLineCount = countEmptyLines(fileContent,methodStartLineNum,methodEndLineNum);
                int statementCount =  methodEndLineNum - methodStartLineNum - emptyLineCount + 1;
                // 16 lines is the threshold for long method as per the project requirements.
                if(statementCount > 15)
                    return true;
            }
        }
        return false;
    }

    /**
     * This Method is used to get empty lines from the file content
     * @param fileContent
     * @param methodStartLineNum
     * @param methodEndLineNum
     * @return
     */
    private int countEmptyLines(String[] fileContent, int methodStartLineNum, int methodEndLineNum) {
        int emptyLineCount = 0;
        for(int i = methodStartLineNum; i <= methodEndLineNum; i++){
            String text = fileContent[i].trim();
            if (text.isEmpty()) {
                emptyLineCount++;
            }
        }
        return emptyLineCount;
    }

}
