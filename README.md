Under construction
====================

<!--

groovy-comprehension
====================

Overview
-----------------

groovy-comprehension module provides 'list comprehension' functionality like Haskell,Scala or Python.

```
import groovyx.comprehension.keyword.select

assert select([x,y]) {
    x: [1,2,3]
    y: [5,6,7]
} == [[1,5], [1,6], [1,7], [2,5],[2,6],[2,7], [3,5],[3,6],[3,7]]
```

You can also use groovy's range for sequential value.

```
assert select([x,y]) {
    x: 1..3
    y: 5..7
} == [[1,5], [1,6], [1,7], [2,5],[2,6],[2,7], [3,5],[3,6],[3,7]]
```

Guard
-----------------

You can specify guard clause to filter values.

```
assert select([x,y]) {
    x: 1..3
    guard(x % 2 == 0)
    y: 5..7
    guard(y % 2 == 1)
} == [[2,5], [2,7]]
```

Auto Guard
-----------------

Previous code is same as follwing:

```
assert select([x,y]) {
    x: 1..3
    x % 2 == 0
    y: 5..7
    y % 2 == 1
} == [[2,5], [2,7]]
```

If the expression in comprehension has boolean value at runtime, it is
regarded as guard clause specified.

Syntax And Transformed Form
----------------------------------

General form is:

```
select (<YIELD EXPRESSION>) {
    <VAR1> : <LIST> or <RANGE>
    <GUARD1>
    <GUARD2>
      :
    <VAR2> : <LIST> or <RANGE>
    <GUARD3>
      :
}
```

Another form is:

```
select {
    <VAR1> : <LIST> or <RANGE>
    <GUARD1>
    <GUARD2>
      :
    <VAR2> : <LIST> or <RANGE>
    <GUARD3>
      :
    yield <YIELD EXPRESSION>
}
```

Both of above are translated into follwing code on AST.
(Actually it is little bit simplified concerned with auto guard.)
This module is implemented as Global AST transformation.

```
// select begin
    (<LIST> or <RANGE>).bind { <VAR1> ->
    delegate.guard(<GUARD1>).bind { _1 ->
    delegate.guard(<GUARD2>).bind { _2 ->
      :
    (<LIST> or <RANGE>).bind { <VAR2> ->
    delegate.guard(<GUARD3>).bind { _3 ->
      :
    yield(<YIELD EXPRESSION>)
    }}}}}
// select end
```


Another Example
-----------------

You can calcurate pythagorean numbers (a^2 + b^2 == c^2)
of which a is equal or less then 10 by following code.

```
assert select {
    a: 1..10
    b: 1..a
    c: a..a+b
    a**2 + b**2 == c**2 // auto guard
    yield("(a=$a,b=$b,c=$c)")
} == ["(a=4,b=3,c=5)", "(a=8,b=6,c=10)"]
```

Infinite stream
-----------------

On the Java8 envrionment, you can use java.util.stream.Stream as infinite lazy stream.

```
import groovyx.comprehension.keyword.select;
import static java.util.stream.Collectors.toList;

assert select ([a,b,c]) {
         a: iterate(1,{it+1})
         b: iterate(1,{it+1}).limit(a-1)
         c: iterate(a,{it+1}).limit(b)
         a**2 + b**2 == c**2
       }.skip(100).findFirst().get() == [144, 108, 180]
```

How to use
-------------

You can use @Grab annotation:

```
@GrabResolver(name="maven-repo", root="https://raw.github.com/uehaj/maven-repo/gh-pages/snapshot")
@Grab("org.jggug.kobo:groovy-comprehension:0.2")
import groovyx.comprehension.keyword.select;
```

Java8 streams is separated module for compatibility, you have to specify:

```
@GrabResolver(name="maven-repo", root="https://raw.github.com/uehaj/maven-repo/gh-pages/snapshot")
@Grab("org.jggug.kobo:groovy-java8-comprehension:0.2")
import groovyx.comprehension.keyword.select;

select(n) { n:1..10 }.each{
  println it
}
```


Change the keyword indicating comprehension
---------------------------------------------------------

You can change the keyword for comprehension with 'import as'.

```
import groovyx.comprehension.keyword.select as foreach

assert foreach(n) {
    n: 1..10
} == [1,2,3,4,5,6,7,8,9,10]

```

Monad comprehension
--------------------

Any class which have follwing instance method can be used with comprehension.

* bind(Closure c)
* yield(x)
* autoGuard(exp)
* mzero()

-->
