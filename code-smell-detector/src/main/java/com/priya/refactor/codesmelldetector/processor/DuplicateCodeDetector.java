package com.priya.refactor.codesmelldetector.processor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DuplicateCodeDetector {


    /**
     * this method checks for Jaccard similarity in the parsed java file and if its greater than 0.75 performs refactoring
     * and returns the refactored code
     *
     * @param compilationUnit
     * @return refactored code
     */
    public String refactorDuplicateCode(CompilationUnit compilationUnit) {
        List<MethodDeclaration> methods = compilationUnit.findAll(MethodDeclaration.class);
        boolean isduplicate = false;
        for (int i = 0; i < methods.size(); i++) {
            for (int j = i + 1; j < methods.size(); j++) {
                // identify the similarity using Jaccard  principle
                double jaccardValue = calculateMethodSimilarity(methods.get(i), methods.get(j));
                System.out.println("methods 1. = " + methods.get(i).getNameAsString() +
                        "  2. = " + methods.get(j).getNameAsString() + "  jaccard value = "+ jaccardValue);
                if (jaccardValue >= 0.75) {
                    isduplicate = true;
                    MethodDeclaration commonMethod = extractCommonCode(methods.get(i), methods.get(j));
                    if (commonMethod != null) {
                        MethodDeclaration parameterizedMethod = addMethodToCompilationUnit(compilationUnit, commonMethod);
                        addMethodCall(compilationUnit,methods.get(i),parameterizedMethod);
                        addMethodCall(compilationUnit,methods.get(j),parameterizedMethod);
                    }
                }
            }
        }
        System.out.println("final output is : " + compilationUnit);
        if(isduplicate){
            return compilationUnit.toString();
        }
        return null;
    }

    public double calculateMethodSimilarity(MethodDeclaration method1, MethodDeclaration method2) {

        Set<String> tokens1 = extractMethodTokens(method1);
        Set<String> tokens2 = extractMethodTokens(method2);

        return calculateJaccardSimilarity(tokens1, tokens2);
    }

    /**
     * this method extracts method content as tokens so that they can be used to identify Jaccard similarity
     * @param method
     * @return Set<String>
     */
    private Set<String> extractMethodTokens(MethodDeclaration method) {
        Set<String> tokens = new HashSet<>();

        // Tokenize method contents
        String methodContent = method.getBody().map(body -> body.toString()).orElse("");
        String[] methodTokens = methodContent.split("\\W+"); // Split by non-word characters
        for (String token : methodTokens) {
            tokens.add(token);
        }
        return tokens;
    }

    /**
     * calculation of Jaccard similarity
     * @param set1
     * @param set2
     * @return
     */
    private double calculateJaccardSimilarity(Set<String> set1, Set<String> set2) {
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }

    /**
     * utility method to extract common code from the two methods where the similarity was found to be >= 0.75
     * @param method1
     * @param method2
     * @return
     */
    private MethodDeclaration extractCommonCode(MethodDeclaration method1, MethodDeclaration method2) {
        // Get the body of each method
        BlockStmt body1 = method1.getBody().orElse(null);
        BlockStmt body2 = method2.getBody().orElse(null);
        MethodDeclaration commonMethod = new MethodDeclaration();
        if (body1 != null && body2 != null) {
            // Find common statements
            NodeList<Statement> commonStatements = new NodeList<>();
            for (Statement stmt1 : body1.getStatements()) {
                for (Statement stmt2 : body2.getStatements()) {
                    if (stmt1.toString().equals(stmt2.toString())) {
                        commonStatements.add(stmt1.clone());
                    }
                }
            }

            // Create a new method with common statements
            commonMethod.setName("refactoredMethod"); // Set a name for the common method
            commonMethod.setType(method1.getType()); // Set the return type (assuming methods have the same return type)
            commonMethod.setModifiers(method1.getModifiers()); // Set method modifiers
            commonMethod.setParameters(method1.getParameters()); // Set method parameters (assuming methods have the same parameters)
            commonMethod.setBody(new BlockStmt(commonStatements));

            return commonMethod;
        }

        return null;
    }

    /**
     * utility method to add common method to the class body
     * @param compilationUnit
     * @param commonMethod
     * @return
     */
    private MethodDeclaration addMethodToCompilationUnit(CompilationUnit compilationUnit, MethodDeclaration commonMethod) {
        BlockStmt body = commonMethod.getBody().orElse(null);
        if (body != null) {

            compilationUnit.getTypes().forEach(type -> {
                // Check if the type is a ClassOrInterfaceDeclaration
                if (type instanceof ClassOrInterfaceDeclaration) {
                    ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) type;
                    classOrInterfaceDeclaration.addMember(commonMethod);
                }
            });

        }
        return commonMethod;
    }

    /**
     * calls the over ridden Method visitor to add method call
     * @param cu
     * @param originalMethod
     * @param commonMethod
     */
    public void addMethodCall(CompilationUnit cu, MethodDeclaration originalMethod, MethodDeclaration commonMethod) {
        cu.accept(new MethodCallVisitor(originalMethod, commonMethod), null);
    }

}
