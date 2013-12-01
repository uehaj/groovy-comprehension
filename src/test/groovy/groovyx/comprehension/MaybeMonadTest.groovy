package groovyx.comprehension

import groovyx.comprehension.keyword.select;
import groovyx.comprehension.monad.MonadPlus

class MaybeMonadTest extends GroovyTestCase {
    void test01() {
        println new Nothing() == new Nothing()
        
        assert (new Just(1) >>> { x->
                new Just("abc") >>> { y->
                yield("<"+x+y+">")
               }}).toString() == "Just <1abc>"
    }

    void test02() {
        assert (((new Just(1) >> new Just(3)) >> new Nothing()) >> new Just(7)) == new Nothing()
    }

    void test03() {
        assert (select {
                  x: new Just(1)
                  y: new Just("abc")
                  yield("<"+x+y+">")
                }).toString() == "Just <1abc>"
    }

    void test04() {
        assert (select {
                    new Just(1)
                    new Just(3)
                    new Nothing()
                    new Just(7)
                }) == new Nothing()
    }
    void test05() {
        assert (select {
                    new Just(1)
                    new Just(3)
                    new Just(4)
                    new Just(7)
                }) == new Just(7)
    }
    void test06() {
        assert (select {
                    new Just(1)
                    new Just(3)
                    false
                    new Just(7)
                }) == new Nothing()
    }
}

abstract class Maybe<T> extends MonadPlus {
    def yield(x) {
        new Just<T>(x)
    }
    def rightShiftUnsigned(@DelegatesTo(Maybe) Closure c) { // Haskell's >>=
        bind(c)
    }
    abstract bind(@DelegatesTo(Maybe) Closure c);
    def rightShift(value) { // Haskell's >>
        bind0(value)
    }
    abstract bind0(value);
    def mzero() {
        new Nothing()
    }
}

class Just<T> extends Maybe<T> {
    private T value
    T get() { value }
    Just(T x) { this.value = x }
    String toString() { "Just $value" }
    def bind(@DelegatesTo(Maybe)Closure c) {
        c.delegate = this
        return c.call(get())
    }
    def bind0(value) {
        return value
    }
    boolean equals(Maybe<T> rhs) {
        if (rhs == null || !(rhs instanceof Just<T>)) {
            return false
        }
        return get() == rhs.get()
    }
}

class Nothing<T> extends Maybe<T> {
    String toString() { "Nothing" }
    Nothing(){}
    def bind(@DelegatesTo(Maybe) Closure c) {
        return this
    }
    def bind0(value) {
        return this
    }
    boolean equals(Maybe<T> rhs) {
        if (rhs == null || !(rhs instanceof Nothing<T>)) {
            return false
        }
        return true
    }
}
