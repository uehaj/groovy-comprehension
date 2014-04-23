# groovy-comprehension

## Overview

groovy-comprehension Groovy extension module provides simple list comprehension functionality similar to that of Haskell, Scala or Python.
Let's look at simple example.

```groovy
import groovyx.comprehension.keyword.select

assert select(x*2) {
    x: [1,2,3]
} == [2, 4, 6]
```

Where `x` is a variable which covers each of the values in list `[1,2,3]`.
And whole of the `select (..) {...}` expression emits values of `x*2` for each `x` as list.

In the other words, above code represent a list which are represented roughly with following math notation:

<img src="http://latex.codecogs.com/gif.latex?\{x&space;*&space;2&space;|x&space;\in&space;[&space;1,2,3&space;]&space;\}" title="\{x * 2 |x \in [ 1,2,3 ] \}" />

Follwing example uses two variables `x` and `y`.

```groovy
import groovyx.comprehension.keyword.select

assert select([x,y]) {
    x: [1,2,3]
    y: [5,6,7]
} == [[1,5], [1,6], [1,7], [2,5],[2,6],[2,7], [3,5],[3,6],[3,7]]
```

In this case, all combinations for all value of `x` and `y` are generated.
You can also use groovy's range expression to specify sequential values.

```groovy
assert select([x,y]) {
    x: 1..3
    y: 5..7
} == [[1,5], [1,6], [1,7], [2,5],[2,6],[2,7], [3,5],[3,6],[3,7]]
```

## Guard and Auto Guard

You can specify guard clause to filter values.

```groovy
assert select([x,y]) {
    x: 1..3
    guard(x % 2 == 0)  // x is 2
    y: 5..7
    guard(y % 2 == 1) // y is 5 or 7
} == [[2,5], [2,7]]
```

Previous code can be written like following:

```groovy
assert select([x,y]) {
    x: 1..3
    x % 2 == 0
    y: 5..7
    y % 2 == 1
} == [[2,5], [2,7]]
```

When the expression in comprehension returns boolean value at runtime, you can ommit explicit specifing `guard`.

Above code are roughly correspondes to following math expression:

<a href="http://www.codecogs.com/eqnedit.php?latex=\{&space;\[&space;x,&space;y\]&space;|x&space;\in&space;[&space;1,2,3&space;]&space;\wedge&space;x&space;\%&space;2&space;=&space;0&space;\wedge&space;y&space;\in&space;[5,6,7]&space;\wedge&space;y&space;\%&space;2&space;=&space;1\}" target="_blank"><img src="http://latex.codecogs.com/gif.latex?\{&space;\[&space;x,&space;y\]&space;|x&space;\in&space;[&space;1,2,3&space;]&space;\wedge&space;x&space;\%&space;2&space;=&space;0&space;\wedge&space;y&space;\in&space;[5,6,7]&space;\wedge&space;y&space;\%&space;2&space;=&space;1\}" title="\{ \[ x, y\] |x \in [ 1,2,3 ] \wedge x \% 2 = 0 \wedge y \in [5,6,7] \wedge y \% 2 = 1\}" /></a>

## Import and Change Keyword

This comprehension fanctionality is enabled only when explicitly import the class `groovyx.comprehension.keyword.select`.
So existing code which uses `select` identifier is safe as far as you don't import the class.

You can change the comprehension keyword `select` to other word by using `import as`.

```groovy
import groovyx.comprehension.keyword.select as foreach
def list = foreach(n) { n:1..10 }
```

In this case, `foreach` can be used to specify comprehension instead of `select`.

## Explicit Yield

This form

```groovy
def list = select(n) { n:1..10 }
```

Is semantically equals to following:

```groovy
def list = select { n:1..10; yield(n) }
```

where `yield` is same meaning of that in Scala, and `return` of Haskell aka `unit` function.

## Examples

### Pythagorean Numbers

You can calcurate numbers `a`, `b` and `c` which satisfies the equation <img src="http://latex.codecogs.com/gif.latex?a^2&space;&plus;&space;b^2&space;=&space;c^2" title="a^2 + b^2 = c^2" />, where `a` is equal or less then 10.

```groovy
assert select {
    a: 1..10
    b: 1..a
    c: a..a+b
    a**2 + b**2 == c**2 // auto guard
    yield("(a=$a,b=$b,c=$c)")
} == ["(a=4,b=3,c=5)", "(a=8,b=6,c=10)"]
```

### Pythagorean Numbers(Infinite Stream Version)

On the Java SE 8, you can use infinite lazy stream of `java.util.stream.Stream` in comprehension.

```groovy
import groovyx.comprehension.keyword.select
import static java.util.stream.Collectors.toList

assert select ([a,b,c]) {
         a: iterate(1,{it+1})
         b: iterate(1,{it+1}).limit(a-1)
         c: iterate(a,{it+1}).limit(b)
         a**2 + b**2 == c**2
       }.skip(100).findFirst().get() == [144, 108, 180]
```

### Verbal Arithmetic

Try to solve following [verbal arithmetic](http://en.wikipedia.org/wiki/Verbal_arithmetic).

       SEND
    +) MORE
    ~~~~~~~~~~
      MONEY

Where alphabet S, E, N, D .. are correspond to one decimal digit different from each other.
You can solve above verbal arithmetic using comprehension:

```groovy
import groovyx.comprehension.keyword.select

def digits = 0..9
select("""\
  $S$E$N$D
+)$M$O$R$E
 $M$O$N$E$Y
""") {
    S:digits-0
    M:digits-S-0
    E:digits-S-M
    O:digits-S-M-E
    N:digits-S-M-E-O
    R:digits-S-M-E-O-N
    D:digits-S-M-E-O-N-R
    Y:digits-S-M-E-O-N-R-D
    (S*1000+E*100+N*10+D) + (M*1000+O*100+R*10+E) == (M*10000+O*1000+N*100+E*10+Y)
}.each { println it }
```

Supply possible values for each variables (`S`,`M`,`E` ...) and constraint that should be sutisfied, you can get the answer.
  
## How to use

### Through Maven Repository

groovy-comprehension jar are published at [jcenter](https://bintray.com/bintray/jcenter). So with gradle 1.7 or later:

```groovy
apply plugin: 'groovy'

repositories {
    jcenter() // specify jcenter
}

dependencies {
    groovy 'org.codehaus.groovy:groovy-all:2.2.1'
    compile 'org.jggug.kobo:groovy-comprehension:0.3'
//  compile 'org.jggug.kobo:groovy-comprehension:0.3:java8'
    testCompile 'junit:junit:4.11'
}
```

If you are using gradle version older then 1.7, instaead of `jcenter()` specify:

```groovy
repositories {
    maven {
        url "http://jcenter.bintray.com/"
    }
}
```

### Get the Jar by Hand

Download jars from [here](https://bintray.com/bintray/jcenter?filterByPkgName=groovy-comprehension) and make JVM classpath reach it. For example, specify -cp option to the jar or put the jar into `~/.groovy/lib`. 

### Grape/@Grab

You can use Groovy [Grape](http://groovy.codehaus.org/Grape)'s `@Grab` annotation:

```groovy
@Grab("org.jggug.kobo:groovy-comprehension:0.3")
import groovyx.comprehension.keyword.select
```

[Groovy 2.2 or later support JCenter as their standard grab resolver](http://groovy.codehaus.org/Groovy+2.2+release+notes#Groovy2.2releasenotes-Bintray'sJCenterrepository), you don't have to specify `@GrabResolver` annotation.

When you want to use Java 8 streams in comprehension, specifiy `java8` as classifier:

```groovy
@Grab("org.jggug.kobo:groovy-comprehension:0.3:java8")
import groovyx.comprehension.keyword.select

select(n) { n:1..10 }.each{
  println it
}
```

To use java 8 stream in comprehension, of cource you have to run groovy on java 8 jre/jdk JVM.

### <font color="red">Known Problems</font>

As far as tried with groovy 2.2/2.3b, MacOS X 10.9, extenstion method looks sometimes doesn't work on Java 7/8 JVM.
When you get following exception sometimes/always:

```
Caught: groovy.lang.MissingMethodException: No signature of method: groovy.lang.IntRange.bind() is applicable for argument types: (sample1_2$_run_closure1) values: [sample1_2$_run_closure1@5587f3a]
     :
```

Workaround is to download jars from [here](https://bintray.com/bintray/jcenter?filterByPkgName=groovy-comprehension) statically to ~/.groovy/lib.

Same problem encounters when using [timyates's groovy-stream](https://github.com/timyates/groovy-stream), so I think this is not the BUG of this module. This issues are reported [GROOVY-6446](http://jira.codehaus.org/browse/GROOVY-6446) and [GROOVY-6447](http://jira.codehaus.org/browse/GROOVY-6447). please vote!

## The Conversion

This feature is implemented with [global AST transformation](http://groovy.codehaus.org/Global+AST+Transformations) and [groovy extension method](http://groovy.codehaus.org/Creating+an+extension+module).

This code

```groovy
import groovyx.comprehension.keyword.select

def list = select("(a=$a,b=$b,c=$c)") {
   a: 1..10
   b: 1..a
   c: a..a+b
   a**2 + b**2 == c**2
}
```

is converted to:

```java
    public java.lang.Object run() {
        java.lang.Object list = (1..10).bind({ java.lang.Object a ->
            delegate.autoGuard((1.. a )).bind({ java.lang.Object b ->
                delegate.autoGuard(( a .. a + b )).bind({ java.lang.Object c ->
                    delegate.autoGuard( a ** 2 + b ** 2 == c ** 2).bind({ java.lang.Object $$0 ->
                        delegate.yield("(a=$a,b=$b,c=$c)")
                    })
                })
            })
        })
        list == ['(a=4,b=3,c=5)', '(a=8,b=6,c=10)']
        this.println(list)
    }
```

## Monad comprehension

Not only list and stream, any class which have follwing instance method can be used in comprehension.

* bind(Closure c)
* yield(x)
* autoGuard(boolean exp)

Those methods are needed on the meaning of duck typing (It is enough to have method but extends/implement perticuler class/interface). But as for convenience, class [groovyx.comprehension.monad.MonadPlus](https://github.com/uehaj/groovy-comprehension/blob/master/src/main/groovy/groovyx/comprehension/monad/MonadPlus.groovy) is available.

Because of this MonadPlus provices default `guard`, `autoGuard` methods, your class which extends MonadPlus class are available in comprehension if you define following methods.

* bind(Closure c)
* yield(x)
* mzero()

### Maybe Monad Example

Just an example, define Maybe monad and use it with comprehension.

```
import groovyx.comprehension.keyword.select;
import groovyx.comprehension.monad.MonadPlus

@Newify([Just,Nothing])
class MaybeMonadTest extends GroovyTestCase {
    void test01() {
        assert (select {
                    Just(1)
                    Just(3)
                    Nothing()
                    Just(7)
                }) == Nothing()
    }
    void test02() {
        assert (select {
                    Just(1)
                    Just(3)
                    Just(4)
                    Just(7)
                }) == Just(7)
    }
    void test03() {
        assert (((Just(1) >> Just(3)) >> Nothing()) >> Just(7)) == Nothing()
    }
}
```

## TODO

 * Support Set and Map
 * Make static type checking complient
 * Implement monadic combinator parser library using this comprehension
