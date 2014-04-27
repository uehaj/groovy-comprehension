package groovyx.comprehension

import groovyx.comprehension.keyword.select
import groovyx.comprehension.extension.IteratorComprehensionExtension
import org.codehaus.groovy.runtime.DefaultGroovyMethods as DGM

class IteratorTest extends GroovyTestCase {
    void test00() {
        def tmp = (IteratorComprehensionExtension.bind([1,2,3].iterator()) {
                       it.collect{x->x*2}.iterator()
                   })
        assert tmp.collect() == [2,4,6]
    }
    
    void test01() {
        assert ((select(n*2) {
                 n: [1,2,3].iterator()
                 }).collect() == [2,4,6])
    }

    void test02() {
        assert select(n) {
          n: (0..14).iterator()
          where(n % 2 == 0)
          where(n % 3 == 0)
        }.collect() == [0, 6, 12]
    }

    void test03() {
        assert select ([n,m]) {
          n: (0..3).iterator()
          n % 2 == 0
          m: (100..110).iterator()
          m % 2 == 1
        }.collect() == [
            [0, 101], [0, 103], [0, 105], [0, 107], [0, 109],
            [2, 101], [2, 103], [2, 105], [2, 107], [2, 109]]
    }

    void test04() {
        def listManip5 = select ([a,b,c]) {
            a: (1..10).iterator()
            b: (1..a).iterator()
            c: (a..a+b).iterator()
            a**2 + b**2 == c**2
        }
        assert listManip5.collect() == [[4,3,5],[8,6,10]]
    }
    
}
