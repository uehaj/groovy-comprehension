package groovyx.comprehension

import groovyx.comprehension.keyword.select;

class PythagoreanTest extends GroovyTestCase {

    void testWith2Parameters() {
        assert select("(a=$a,b=$b,c=$c)") {
            a: (1..10)
            b: (1..a)
            c: (a..a+b)
            guard(a**2 + b**2 == c**2)
        } == ["(a=4,b=3,c=5)", "(a=8,b=6,c=10)"]
    }

    void testWith1Parameter() {
        assert select {
            a: (1..10)
            b: (1..a)
            c: (a..a+b)
            guard(a**2 + b**2 == c**2)
            yield("(a=$a,b=$b,c=$c)")
        } == ["(a=4,b=3,c=5)", "(a=8,b=6,c=10)"]
    }

    void testWith1ParameterAutoYield() {
        assert select {
            a: (1..10)
            b: (1..a)
            c: (a..a+b)
            guard(a**2 + b**2 == c**2)
            "(a=$a,b=$b,c=$c)"
        } == ["(a=4,b=3,c=5)", "(a=8,b=6,c=10)"]
    }

    void testWith2ParametersAutoGuard() {
        assert select("(a=$a,b=$b,c=$c)") {
            a: (1..10)
            b: (1..a)
            c: (a..a+b)
            a**2 + b**2 == c**2 // auto guard
        } == ["(a=4,b=3,c=5)", "(a=8,b=6,c=10)"]
    }

    void testWith1ParameterAutoGuard() {
        assert select {
            a: (1..10)
            b: (1..a)
            c: (a..a+b)
            a**2 + b**2 == c**2 // auto guard
            yield("(a=$a,b=$b,c=$c)")
        } == ["(a=4,b=3,c=5)", "(a=8,b=6,c=10)"]
    }

    void testWith1ParameterAutoYieldAutoGuard() {
        assert select {
            a: (1..10)
            b: (1..a)
            c: (a..a+b)
            a**2 + b**2 == c**2 // auto guard
            "(a=$a,b=$b,c=$c)"
        } == ["(a=4,b=3,c=5)", "(a=8,b=6,c=10)"]
    }
}
