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
import org.codehaus.groovy.ast.stmt.*;
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
 * @author Uehara Junji(@uehaj)
 */
public class ComprehensionInfo {

    private final static PREFIX='$$'

    private final SourceUnit sourceUnit
    
    //    ClassExpression clazz; // monad class
    def clazz; // monad class
    Expression yieldValue;
    List<Operation> steps;

    static class Operation {
        Expression exp;
    }
    static class Guard extends Operation {
        String toString() {
            "    guard($exp)\n"
        }
    }
    static class CallBind extends Operation {
        VariableScope scope
        String varName
        String toString() {
            "    $varName <- $exp\n"
        }
    }
    /**
     * Reports an error back to the source unit.
     *
     * @param msg  the error message
     * @param expr the expression that caused the error message.
     */
    void addError(String msg, ASTNode expr, boolean isFatal=false) {
        def collector = sourceUnit.getErrorCollector()
        collector.addErrorAndContinue(
            new SyntaxErrorMessage(new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber(), expr.getLastLineNumber(), expr.getLastColumnNumber()), sourceUnit)
        );
    }
    /*
      input souce:
      
         List.for([a,b,c]) {
           a:(1..10); a>5
           b:(1..a)
           c:(a..a+b)
           a**2 + b**2 == c**2
         }

     parsed AST should be:

        astexpr.MethodCallExpression[
             object: astexpr.ClassExpression[type: java.util.List]
             method: ConstantExpression[for]
             arguments: astexpr.ArgumentListExpression  [ astexpr.ListExpression // [a,b,c]
                                                          [astexpr.VariableExpression[variable: a]
                                                           , astexpr.VariableExpression[variable: b]
                                                           , astexpr.VariableExpression[variable: c]
                                                           ]
                                                          , astexpr.ClosureExpression[]{
             aststmt.BlockStatement
              [
        a:     aststmt.ExpressionStatement[expression:astexpr.RangeExpression]   // (1..10)
               , aststmt.ExpressionStatement[expression:astexpr.BinaryExpression   // guard(a>5)
                                               [astexpr.VariableExpression[variable: a]
                                                 (">" at 10:15:  ">" )
                                               ConstantExpression[5]]
                                            ]
        b:     , aststmt.ExpressionStatement[expression:astexpr.RangeExpression] // (1..a)
        c:     , aststmt.ExpressionStatement[expression:astexpr.RangeExpression] // (a .. a+b)
               , aststmt.ExpressionStatement[expression:astexpr.BinaryExpression // guard(a**2 + b**2 == c**2)
                                             [astexpr.BinaryExpression // ==
                                              [astexpr.BinaryExpression // +
                                               [astexpr.VariableExpression[variable: a] // a**2
                                                ("**" at 13:4:  "**" )
                                                ConstantExpression[2]]
                                               ("+" at 13:8:  "+" )
                                               astexpr.BinaryExpression
                                               [astexpr.VariableExpression[variable: b]
                                                ("**" at 13:11:  "**" )
                                                ConstantExpression[2]]]
                                              ("==" at 13:15:  "==" )
                                              astexpr.BinaryExpression // c ** 2
                                                [astexpr.VariableExpression[variable: c]
                                                 ("**" at 13:19:  "**" )
                                                 ConstantExpression[2]]
                                              ]
                                             ]
               ]
              }
        ]
        ]

     */

    @groovy.transform.TypeChecked
    public ComprehensionInfo(MethodCallExpression call, SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit
        this.clazz = call.objectExpression // now not using
        ArgumentListExpression arguments = (ArgumentListExpression)call.arguments

        if (arguments.expressions.size() == 1) { // select { ... ; yield(value) }
            if (arguments.expressions[0] instanceof ClosureExpression) {
                BlockStatement code = (BlockStatement)arguments.expressions[0].code
                if (code.statements.size() < 2) {
                    addError("Comprehension closure body shold have more then 2 statements.", code);
                    return
                }
                ExpressionStatement lastStatement = (ExpressionStatement)code.statements[-1]
                this.yieldValue = lastStatement.expression
                this.steps = genSteps(code.statements.size() == 1 ? (List<Statement>)[] : code.statements[0..-2])
            }
            else {
                addError("The argument of comprehension should be Closure.", arguments.expressions[0]);
                return
            }
        }
        else if (arguments.expressions.size() == 2) { // select (value) { ... }
            if (arguments.expressions[1] instanceof ClosureExpression) {
                this.yieldValue = new MethodCallExpression(
                    new VariableExpression("delegate"),
                    new ConstantExpression("yield"),
                    new ArgumentListExpression(arguments.expressions[0]))
                BlockStatement code = (BlockStatement)arguments.expressions[1].code
                if (code.statements.size() < 1) {
                    addError("Comprehension closure body shold have more then 1 statement.", code);
                    return
                }
                this.steps = genSteps(code.statements)
            }
            else {
                addError("The second argument of comprehension should be Closure.", arguments.expressions[1]);
                return
            }
        }
        else {
            addError("The number of comprehension should be 1 or 2.", arguments);
        }
    }

    @groovy.transform.TypeChecked
    List<Operation> genSteps(List<Statement> statements) {
        def result = []
        statements.each { Statement statement ->
            if (!(statement instanceof ExpressionStatement)) {
                addError("Statement in comprehension should be expression statement.", statement);
                return
            }
            assert statement instanceof ExpressionStatement
            if (statement.statementLabel) {
                result += new CallBind(varName:statement.statementLabel, exp:statement.expression)
            }
            else {
                result += new Guard(exp:statement.expression)
            }
        }
        return result
    }

    public String toString() {
        "class=${clazz.type}"+
        "yieldValue=${yieldValue.expressions.collect {it.variable}}\n"
        "steps=${steps.collect { it.toString() }}\n"
    }
    /*
     AST tree to be generated:

    MethodCall - [1,2,3].$bind{...
      List [1,2,3]
      Constant - $bind String
      ArgumentList
        ClosureExpression
          Parameter - x
          BlockStatement
            ExpressionStatement: MethodCallExpression
              MethodCall - [4,5,6]
                Constant $bind:String
                ArgumentList ({ y -> ... }
                ClosureExpression
                Parameter - y
                BlockStatement (1)
                  ExpressionStatement - MethodCallExpression
                    MethodCall  - List.yield([x*y])
                      Variable - List
                      Constant - yield
                      ArgumentList - ([(x*y)])
                        List [(x*y)]
                          Binary(x*y)
                            Variable - x
                            Variable - y

                    
    */
    ASTNode genRhs(exp, parameter) {
        Parameter[] parameters = [parameter]
        ClosureExpression clos = new ClosureExpression(
            parameters,
            new BlockStatement(
                [ new ExpressionStatement(exp) ], new VariableScope()
            )
        )
        def scope = new VariableScope()
        clos.variableScope = scope
        return new ArgumentListExpression(clos)
    }

    ASTNode genLhs(exp) {
        return new MethodCallExpression(
            new VariableExpression("delegate"),
            new ConstantExpression("autoGuard"),
            new ArgumentListExpression(exp)
        );
    }

    void setReferencedLocalVariable(exp, varName) {
        exp.argumentList[0].closureExpression.blockStatement.expressionStatement.methodCall.closureexpression
    }

    ASTNode toAST() {
        def exp = yieldValue
        def n = 0
        steps.reverse().eachWithIndex { Operation step, int idx ->
            def varName = step instanceof CallBind ? step.varName : null
            Parameter parameter = new Parameter(ClassHelper.DYNAMIC_TYPE, varName ?: PREFIX+"${n++}")
            if (idx == steps.size()-1) {
                exp = new MethodCallExpression(step.exp, "bind", genRhs(exp, parameter));
            }
            else {
                exp = new MethodCallExpression(genLhs(step.exp), "bind", genRhs(exp, parameter));
            }
        }
        return exp
    }
    
}
