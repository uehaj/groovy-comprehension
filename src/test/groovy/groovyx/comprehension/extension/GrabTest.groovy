/**
 * Before run this test, you have to do follwing command:
 *
 * % gradle uploadArchives
 *
 * This installs maven pom to /tmp/myRepo directory.
 */
package groovyx.comprehension.extension
import groovyx.comprehension.extension.ListComprehensionExtension

class GrabTestCase extends GroovyTestCase {
    void testClassForName() {
        assert Class.forName("groovyx.comprehension.extension.ListComprehensionExtension") == groovyx.comprehension.extension.ListComprehensionExtension.class
        assert [].mzero() == []
    }
    void testGrabAnnotation() {
        Process p = "groovy src/test/resources/GrabTest.groovy".execute()
        String err = p.err.text
        String output = p.in.text
        assert (err+output).startsWith("[]")
    }
}
