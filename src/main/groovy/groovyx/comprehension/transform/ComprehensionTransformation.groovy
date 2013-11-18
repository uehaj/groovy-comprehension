/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.comprehension.transform;

import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.ArrayList;
import java.util.List;

/**
 * Transformation to capture ASTBuilder from code statements.
 * <p>
 * The AstBuilder "from code" approach is used with a single Closure
 * parameter. This transformation converts the ClosureExpression back
 * into source code and rewrites the AST so that the "from string"
 * builder is invoked on the source. In order for this to work, the
 * closure source must be given a goto label. It is the "from string"
 * approach's responsibility to remove the BlockStatement created
 * by the label.
 *
 * @author Uehara Junji(@uehaj)
 */

@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class ComprehensionTransformation implements ASTTransformation {

    private static final String TRIGGER_KEYWORD_PACKAGE = 'groovyx.comprehension.keyword.';
    private static final String TRIGGER_KEYWORD_CLASS = 'select';
    private static final String TRIGGER_KEYWORD_FQCN = TRIGGER_KEYWORD_PACKAGE+TRIGGER_KEYWORD_CLASS;

    @groovy.transform.TypeChecked
    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        // todo : are there other import types that can be specified?
        AstBuilderInvocationTrap transformer = new AstBuilderInvocationTrap(
                sourceUnit.getAST().getImports(),
                sourceUnit.getAST().getStarImports(),
                sourceUnit.getSource(),
                sourceUnit
        );
        if (nodes != null) {
            for (ASTNode it : nodes) {
                if (!(it instanceof AnnotationNode) && !(it instanceof ClassNode)) {
                    it.visit(transformer);
                }
            }
        }
        if (sourceUnit.getAST() != null) {
            sourceUnit.getAST().visit(transformer);
            if (sourceUnit.getAST().getStatementBlock() != null) {
                sourceUnit.getAST().getStatementBlock().visit(transformer);
            }
            if (sourceUnit.getAST().getClasses() != null) {
                for (ClassNode classNode : sourceUnit.getAST().getClasses()) {
                    if (classNode.getMethods() != null) {
                        for (MethodNode node : classNode.getMethods()) {
                            if (node != null && node.getCode() != null) {
                                node.getCode().visit(transformer);
                            }
                        }
                    }

                    try {
                        if (classNode.getDeclaredConstructors() != null) {
                            for (MethodNode node : classNode.getDeclaredConstructors()) {
                                if (node != null && node.getCode() != null) {
                                    node.getCode().visit(transformer);
                                }
                            }
                        }
                    } catch (MissingPropertyException ignored) {
                        // todo: inner class nodes don't have a constructors field available
                    }

                    // all properties are also always fields
                    if (classNode.getFields() != null) {
                        for (FieldNode node : classNode.getFields()) {
                            if (node.getInitialValueExpression() != null) {
                                node.getInitialValueExpression().visit(transformer);
                            }
                        }
                    }

                    try {
                        if (classNode.getObjectInitializerStatements() != null) {
                            for (Statement node : classNode.getObjectInitializerStatements()) {
                                if (node != null) {
                                    node.visit(transformer);
                                }
                            }
                        }
                    } catch (MissingPropertyException ignored) {
                        // todo: inner class nodes don't have a objectInitializers field available
                    }

                    // todo: is there anything to do with the module ???
                }
            }
            if (sourceUnit.getAST().getMethods() != null) {
                for (MethodNode node : sourceUnit.getAST().getMethods()) {
                    if (node != null) {
                        if (node.getParameters() != null) {
                            for (Parameter parameter : node.getParameters()) {
                                if (parameter != null && parameter.getInitialExpression() != null) {
                                    parameter.getInitialExpression().visit(transformer);
                                }
                            }
                        }
                        if (node.getCode() != null) {
                            node.getCode().visit(transformer);
                        }
                    }
                }
            }
        }
    }

    /**
     * This class traps invocations of AstBuilder.build(CompilePhase, boolean, Closure) and converts
     * the contents of the closure into expressions by reading the source of the Closure and sending
     * that as a String to AstBuilder.build(String, CompilePhase, boolean) at runtime.
     */
    @groovy.transform.TypeChecked
    private static class AstBuilderInvocationTrap extends CodeVisitorSupport {

        private final List<String> factoryTargets = new ArrayList<String>();
        private final ReaderSource source;
        private final SourceUnit sourceUnit;

        /**
         * Creates the trap and captures all the ways in which a class may be referenced via imports.
         *
         * @param imports        all the imports from the source
         * @param importPackages all the imported packages from the source
         * @param source         the reader source that contains source for the SourceUnit
         * @param sourceUnit     the source unit being compiled. Used for error messages.
         */
        @groovy.transform.TypeChecked
        AstBuilderInvocationTrap(List<ImportNode> imports, List<ImportNode> importPackages, ReaderSource source, SourceUnit sourceUnit) {
            if (source == null) throw new IllegalArgumentException("Null: source");
            if (sourceUnit == null) throw new IllegalArgumentException("Null: sourceUnit");
            this.source = source;
            this.sourceUnit = sourceUnit;

            // factory type may be references as fully qualified, an import, or an alias
            factoryTargets.add(TRIGGER_KEYWORD_FQCN);//default package

            if (imports != null) {
                for (ImportNode importStatement : imports) {
                    if (TRIGGER_KEYWORD_FQCN.equals(importStatement.getType().getName())) {
                        factoryTargets.add(importStatement.getAlias());
                    }
                }
            }

            if (importPackages != null) {
                for (ImportNode importPackage : importPackages) {
                    if (TRIGGER_KEYWORD_PACKAGE.equals(importPackage.getPackageName())) {
                        factoryTargets.add(TRIGGER_KEYWORD_CLASS);
                        break;
                    }
                }
            }
        }

        /**
         * Reports an error back to the source unit.
         *
         * @param msg  the error message
         * @param expr the expression that caused the error message.
         */
        @groovy.transform.TypeChecked
        void addError(String msg, ASTNode expr) {
            sourceUnit.getErrorCollector().addErrorAndContinue(
                    new SyntaxErrorMessage(new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber(), expr.getLastLineNumber(), expr.getLastColumnNumber()), sourceUnit)
            );
        }


        /**
         * Attempts to find AstBuilder 'from code' invocations. When found, converts them into calls
         * to the 'from string' approach.
         *
         * @param call the method call expression that may or may not be an AstBuilder 'from code' invocation.
         */
        @groovy.transform.TypeChecked
        public void visitMethodCallExpression(MethodCallExpression call) {
            // List.for([a,b,c]) { a:(1..10); b:(1..a); c:(a..a+b); a**2 + b**2 == c**2); }
            if (isComprehension(call)) {
//              println "INFO="+call.hashCode()+"call.method"+call.method
                ComprehensionInfo info = new ComprehensionInfo(call, sourceUnit);
                if (info.yieldValue == null || info.steps == null){
                    return;
                }
                ASTNode ast = info.toAST()
                assert ast instanceof MethodCallExpression
                MethodCallExpression newCall = (MethodCallExpression)ast
                call.method = newCall.method
                call.objectExpression = newCall.objectExpression
                call.arguments = newCall.arguments
            } else {
                // continue normal tree walking
                call.getObjectExpression().visit(this);
                call.getMethod().visit(this);
                call.getArguments().visit(this);
            }
        }

        @groovy.transform.TypeChecked
        String propertyToPackage(PropertyExpression pe1) {
            if (pe1.getObjectExpression() instanceof PropertyExpression) {
                PropertyExpression pe2 = (PropertyExpression)pe1.getObjectExpression()
                if (pe2.getObjectExpression() instanceof VariableExpression) {
                    VariableExpression ve = (VariableExpression)pe2.getObjectExpression()
                    if (pe2.getProperty() instanceof ConstantExpression) {
                        ConstantExpression ce1 = (ConstantExpression)pe2.getProperty()
                        if (pe1.getProperty() instanceof ConstantExpression) {
                            ConstantExpression ce2 = (ConstantExpression)pe1.getProperty();
//                                    println "${ve.variable} + ${ce1.value} + ${ce2.value}"
                            return ve.variable+'.'+ce1.value+'.'+ce2.value 
                        }
                    }
                }
            }
            return null
        }

        /**
         * Looks for method calls on the AstBuilder class called build that take
         * a Closure as parameter. This is all needed b/c build is overloaded.
         *
         * @param call the method call expression, may not be null
         */
        @groovy.transform.TypeChecked
        private boolean isComprehension(MethodCallExpression call) {
            if (call == null) throw new IllegalArgumentException("Null: call");
            String methodName = ((ConstantExpression) call.getMethod()).getValue();
            
            if (call.getMethod() instanceof ConstantExpression) {
                if (factoryTargets.contains(methodName)) {
                    if (call.getObjectExpression() instanceof VariableExpression) {
                        VariableExpression ve = (VariableExpression)call.getObjectExpression();
                        if (ve.variable == 'this') {
                            return true;
                        }
                    }
                }
                if (call.getObjectExpression() instanceof PropertyExpression) {
                    String packageName = propertyToPackage((PropertyExpression)call.getObjectExpression());
                    if (packageName != null && packageName+'.'+methodName == TRIGGER_KEYWORD_FQCN) {
                        return true
                    }
                }
            }
            return false;
        }
    }
}
