package com.priya.refactor.codesmelldetector.processor;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

class MethodCallVisitor extends VoidVisitorAdapter<Void> {
    private final MethodDeclaration originalMethod;
    private final MethodDeclaration commonMethod;

    public MethodCallVisitor(MethodDeclaration originalMethod, MethodDeclaration commonMethod) {
        this.originalMethod = originalMethod;
        this.commonMethod = commonMethod;
    }

    /**
     * This Method over rides the visitor method so that we can remove common code
     * and add method call to the original methods
     * @param md
     * @param arg
     */
    @Override
    public void visit(MethodDeclaration md, Void arg) {
        super.visit(md, arg);
        if (md.equals(originalMethod)) {
            BlockStmt body = md.getBody().orElseThrow(() -> new IllegalStateException("Original method body not found"));
            removeCommonCode(body);
            addMethodCallToBody(md.getBody().orElse(new BlockStmt()));
        }
    }

    /**
     * This method removes the common code from the originating methods
     * @param body
     */
    private void removeCommonCode(BlockStmt body) {
            int commonCodeSize = commonMethod.getBody().orElseThrow(() ->
                    new IllegalStateException("Common method body not found")).getStatements().size();
            for (int i = 0; i < commonCodeSize; i++) {
                body.getStatements().remove(0); // Remove statements from the beginning
            }
        }

    /**
     * This Method add the new method call to the originating methods where the common code was removed
     * @param body
     */
    private void addMethodCallToBody(BlockStmt body) {
        MethodCallExpr methodCallExpr = new MethodCallExpr(null, commonMethod.getName());

        body.addStatement(methodCallExpr);

        originalMethod.setBody(body);
    }
}