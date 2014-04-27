package groovyx.comprehension
import groovy.transform.TypeChecked

import groovyx.comprehension.keyword.select;

class STCTest extends GroovyTestCase {

//    @TypeChecked
    void testWith2Parameters() {
        assert select("(a=$a,b=$b,c=$c)") {
            a: [1,2,3,4,5,6,7,8,9,10]
            b: (1..a).collect()
            c: (a..a+b).collect()
            guard(a**2 + b**2 == c**2)
        } == ["(a=4,b=3,c=5)", "(a=8,b=6,c=10)"]
    }

    void testWith1Parameter() {
        assert select {
            a: 1..10
            b: 1..a
            c: a..a+b
            guard(a**2 + b**2 == c**2)
            yield("(a=$a,b=$b,c=$c)")
        } == ["(a=4,b=3,c=5)", "(a=8,b=6,c=10)"]
    }

    void testWith1ParameterAutoYield() {
        assert select {
            a: 1..10
            b: 1..a
            c: a..a+b
            guard(a**2 + b**2 == c**2)
            "(a=$a,b=$b,c=$c)"
        } == ["(a=4,b=3,c=5)", "(a=8,b=6,c=10)"]
    }

    void testWith2ParametersAutoGuard() {
        assert select("(a=$a,b=$b,c=$c)") {
            a: 1..10
            b: 1..a
            c: a..a+b
            a**2 + b**2 == c**2 // auto guard
        } == ["(a=4,b=3,c=5)", "(a=8,b=6,c=10)"]
    }

    void testWith1ParameterAutoGuard() {
        assert select {
            a: 1..10
            b: 1..a
            c: a..a+b
            a**2 + b**2 == c**2 // auto guard
            yield("(a=$a,b=$b,c=$c)")
        } == ["(a=4,b=3,c=5)", "(a=8,b=6,c=10)"]
    }

    void testWith1ParameterAutoYieldAutoGuard() {
        assert select {
            a: 1..10
            b: 1..a
            c: a..a+b
            a**2 + b**2 == c**2 // auto guard
            "(a=$a,b=$b,c=$c)"
        } == ["(a=4,b=3,c=5)", "(a=8,b=6,c=10)"]
    }
}
