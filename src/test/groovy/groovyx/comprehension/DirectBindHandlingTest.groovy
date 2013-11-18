package groovyx.comprehension

import groovyx.comprehension.keyword.select;

class DirectBindHandlingTest extends GroovyTestCase {

    void test01() {
        def listManip0 = [1,2,3].bind({x->      // listManip0 = do x <- [1,2,3]
                         [4,5,6].bind({y->      //                 y <- [4,5,6]
                         [x*y]                     //                 pure x*y
                         })})
        assert listManip0 == [4,5,6,8,10,12,12,15,18]
    }


    void test02() {
        def listManip1 = [1,2,3] >>> {x->      // listManip1 = do x <- [1,2,3]
                         [4,5,6] >>> {y->      //                 y <- [4,5,6]
                         [x*y]                 //                 pure x*y
                         }}
        assert listManip1 == [4,5,6,8,10,12,12,15,18]
    }

    void test03() {
        def listManip2 = (1..10) >>> {z->
                         (1..z) >>> {x->
                         (x..z) >>> {y->
                         guard(x**2 + y**2 == z**2) >>
                         yield([x, y, z])
                         }}}
        assert listManip2 ==  [[3,4,5], [6,8,10]]
    }

    void test04() {
        def listManip4 = (1..10) >>> {a->
                         (1..a) >>> {b->
                         (a..a+b) >>> {c->
                         guard(a**2 + b**2 == c**2) >>> {_->
                         yield([a,b,c])
                         }}}}

        assert listManip4 == [[4,3,5], [8,6,10]]
    }

    void test05() {
        def listManip5 = (1..10) >>> {a->
                         (1..a) >>> {b->
                         (a..a+b) >>> {c->
                         guard(a**2 + b**2 == c**2) >>
                         yield([a,b,c])
                         }}}

        assert listManip5 == [[4,3,5], [8,6,10]]
    }

// リストモナドは非決定計算(可能性の計算)を表す。
// つまり「1 or 2 or 3」と「4 or 5 or 6」を掛け算した結果は、
// 「4 or 5 or 6 or 8 or 10 or 12 or 12 or 15 or 18」となる。
    void test06() {
        def listManip6 = [1,2,3] >>> {x->      // listManip6 = do x <- [1,2,3]
                         [4,5,6] >>> {y->      //                 y <- [4,5,6]
                         [x*y]                 //                 pure x*y
                         }}
        assert listManip6 == [1*4, 1*5, 1*6, 2*4, 2*5, 2*6, 3*4, 3*5, 3*6]
    }

// アプリカティブは、箱の中に対する透過的な演算を可能とする。
// モナドはアプリカティブでもある。
// リストモナド(非決定計算)に対して通常の関数(1引数関数(x+1))を適用する。
    void test07() {
        def listManip7 = [{it+1}].ap( [1,2,3] )  // listManip7 = pure (+1) <*> [1,2,3]
        assert listManip7 == [2,3,4]
    }

// アプリカティブは、箱の中に対する透過的な演算を可能とする。
// モナドはアプリカティブでもある。
// リストモナド(非決定計算)に対して通常の関数(2引数関数x+y)を適用する。
    void test08() {
        def listManip8 = [{x,y->x+y}].ap( [1,2,3] ).ap( [4,5,6] )
        // listManip8 = pure (+) <*> [1,2,3] <*> [4,5,6]
        assert listManip8 == [1+4, 1+5, 1+6, 2+4, 2+5, 2+6, 3+4, 3+5, 3+6]
        // 「1 or 2 or 3」と「4 or 5 or 6」を足し算した結果は、
        // 「5 or 6 or 7 or 6 or 7 or 8 or 7 or 8 or 9」となる。
        println listManip8
    }
}
