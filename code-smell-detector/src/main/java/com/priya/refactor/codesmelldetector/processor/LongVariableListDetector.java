package com.priya.refactor.codesmelldetector.processor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.List;

public class LongVariableListDetector {


    public Boolean detectLongVariableList(CompilationUnit compilationUnit) {


        if (compilationUnit != null) {
            // Find all variable in the program
            List<MethodDeclaration> methods = compilationUnit.findAll(MethodDeclaration.class);
            for (MethodDeclaration method : methods) {

                System.out.println("parameters :  " + method.getParameters());
                // 4 is the threshold for long parameters as per the project requirements.
                if(method.getParameters().size() > 3)
                    return true;
            }
        }
        return false;
    }
}
