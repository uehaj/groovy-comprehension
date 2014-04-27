@GrabResolver(name="maven-repo", root="file:///tmp/myRepo")
@Grab("org.jggug.kobo:groovy-comprehension:0.3")
import groovyx.comprehension.extension.ListComprehensionExtension

class GrabTest {
    public static void main(String[] args) {
        assert Class.forName("groovyx.comprehension.extension.ListComprehensionExtension") == groovyx.comprehension.extension.ListComprehensionExtension.class
        println ([].mzero())
    }
}
