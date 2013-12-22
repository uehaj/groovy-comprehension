package groovyx.comprehension

import groovyx.comprehension.keyword.select;

import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.iterate;

class StreamTest extends GroovyTestCase {
    
    void test01() {
        assert select(n) {
          n: iterate(0,{it+1})
          where(n % 2 == 0)
        }.limit(10).collect(toList()) == [0, 2, 4, 6, 8, 10, 12, 14, 16, 18]
    }

    void test02() {
        assert select(n) {
          n: iterate(0,{it+1})
          where(n % 2 == 0)
        }.limit(10).collect(toList()) == [0, 2, 4, 6, 8, 10, 12, 14, 16, 18]
    }

    void test03() {
        assert select ([n,m]) {
          n: iterate(0,{it+1})
          n % 2 == 0
          m: iterate(100,{it+1}).limit(10)
          m % 2 == 1
        }.limit(10).collect(toList()) == [
            [0, 101], [0, 103], [0, 105], [0, 107], [0, 109],
            [2, 101], [2, 103], [2, 105], [2, 107], [2, 109]]

    }

    void test04() {
        def listManip5 = select ([a,b,c]) {
                           a: iterate(1,{it+1})
                           b: iterate(1,{it+1}).limit(a-1)
                           c: iterate(a,{it+1}).limit(b)
                           a**2 + b**2 == c**2
                         }.skip(100).findFirst().get()
        assert listManip5 == [144, 108, 180]
    }
    
}
