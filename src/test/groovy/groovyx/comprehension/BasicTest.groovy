package groovyx.comprehension

import groovyx.comprehension.keyword.select;

class BasicTest extends GroovyTestCase {
    void testDirectFlatMap() {
        def listManip1 = [1, 2, 3].bind({ xx ->
                         [5, 6, 7].bind({ yy ->
                         yield([xx,yy])
           })
        })
        assert listManip1 == [[1,5], [1,6], [1,7], [2,5],[2,6],[2,7], [3,5],[3,6],[3,7]]
    }
    void testBasic() {
        def listManip2 = select([x,y]) {
            x:[1,2,3]
            y:[5,6,7]
        }
        assert listManip2 == [[1,5], [1,6], [1,7], [2,5],[2,6],[2,7], [3,5],[3,6],[3,7]]
    }
    void testYieldsString() {
        def listManip2 = select("x=$x,y=$y,z=$z") { x:[1,2]; y:[3,4]; z:[5,6] }
        assert listManip2 == ["x=1,y=3,z=5", "x=1,y=3,z=6", "x=1,y=4,z=5", "x=1,y=4,z=6", "x=2,y=3,z=5", "x=2,y=3,z=6", "x=2,y=4,z=5", "x=2,y=4,z=6"]
    }
    void testUseRangeWithParen() {
        def listManip2 = select([x,y,z]) { x:(1..3); y:(4..6); z:(7..8) }
        assert listManip2 == [[1, 4, 7], [1, 4, 8], [1, 5, 7], [1, 5, 8], [1, 6, 7], [1, 6, 8],
                              [2, 4, 7], [2, 4, 8], [2, 5, 7], [2, 5, 8], [2, 6, 7], [2, 6, 8],
                              [3, 4, 7], [3, 4, 8], [3, 5, 7], [3, 5, 8], [3, 6, 7], [3, 6, 8]]
    }
    void testUseRangeWithoutParen() {
        def listManip2 = select([x,y,z]) { x:1..3; y:4..6; z:7..8 }
        assert listManip2 == [[1, 4, 7], [1, 4, 8], [1, 5, 7], [1, 5, 8], [1, 6, 7], [1, 6, 8],
                              [2, 4, 7], [2, 4, 8], [2, 5, 7], [2, 5, 8], [2, 6, 7], [2, 6, 8],
                              [3, 4, 7], [3, 4, 8], [3, 5, 7], [3, 5, 8], [3, 6, 7], [3, 6, 8]]
    }
}
