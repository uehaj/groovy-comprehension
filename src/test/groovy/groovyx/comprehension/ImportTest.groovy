package groovyx.comprehension

class ImportTest extends GroovyTestCase {

   void testImportNormal() {
       assert new GroovyShell().evaluate('''
import groovyx.comprehension.keyword.select;
select {
    x:[1,2,3]
    yield x
}
       ''') == [1,2,3]
   }
    
   void testImportAs() {
       assert new GroovyShell().evaluate('''
import groovyx.comprehension.keyword.select as foreach;
foreach {
    x:[1,2,3]
    yield x
}
       ''') == [1,2,3]
   }

   void testImportNormalAndAs() {
       assert new GroovyShell().evaluate('''
import groovyx.comprehension.keyword.select;
import groovyx.comprehension.keyword.select as foreach;
def a = foreach {
    x:[1,2,3]
    yield x
}
def b = select {
    x:[4,5,6]
    yield x
}
[a,b]
       ''') == [[1,2,3], [4,5,6]]
   }
    
   void testImportKeyword() {
       def message = shouldFail (groovy.lang.MissingMethodException) {
       assert new GroovyShell().evaluate('''
import groovyx.comprehension.keyword.select as foreach;
def a = foreach {
    x:[1,2,3]
    yield x
}
def b = select {
    x:[4,5,6]
    yield x
}
[a,b]
       ''')
       }
       assert message.startsWith('No signature of method: Script1.select()')
   }
}
