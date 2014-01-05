# groovy-comprehension

## Overview

groovy-comprehension Groovy extension module provides list comprehension functionality similar to that of Haskell, Scala or Python.
Let's look at simple example.

```groovy
import groovyx.comprehension.keyword.select

assert select(x*2) {
    x: [1,2,3]
} == [2, 4, 6]
```

Where `x` is a variable which covers each of the values in list `[1,2,3]`.
And whole of the `select (..) {...}` expression emits values of `x*2` for each `x` as list.
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

## Import and Change Keyword

This comprehension fanctionality is enabled only when explicitly import the class `groovyx.comprehension.keyword.select`.
So existing code which uses `select` identifier is safe as far as you don't import the class.

You can change the comprehension keyword `select` to other word by using `import as`.

```groovy
import groovyx.comprehension.keyword.select as foreach
def list = foreach(n) { n:1..10 }
```

In this case, `foreach` can be used to specify comprehension instead of `select`.

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

You can calcurate pythagorean numbers (a^2 + b^2 == c^2) of which a is equal or less then 10 by following code.

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

On the Java SE 8 platform, you can use infinite lazy stream of `java.util.stream.Stream` in comprehension.

```groovy
import groovyx.comprehension.keyword.select;
import static java.util.stream.Collectors.toList;

assert select ([a,b,c]) {
         a: iterate(1,{it+1})
         b: iterate(1,{it+1}).limit(a-1)
         c: iterate(a,{it+1}).limit(b)
         a**2 + b**2 == c**2
       }.skip(100).findFirst().get() == [144, 108, 180]
```

### Verbal Arithmetic

Try to solve folowing [verbal arithmetic](http://en.wikipedia.org/wiki/Verbal_arithmetic):

       SEND
    +) MORE
    ~~~~~~~~~~
      MONEY

code to solve above with comprehension are:

```groovy
import groovyx.comprehension.keyword.select;

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

  
## How to use

### Gradle

groovy-comprehension jar are published at [jcenter](https://bintray.com/bintray/jcenter), so with gradle 1.7 or later:

```groovy
apply plugin: 'groovy'

repositories {
    jcenter() // specify jcenter
    mavenLocal()
    mavenCentral()
}

dependencies {
    groovy 'org.codehaus.groovy:groovy-all:2.2.1'
    compile 'org.jggug.kobo:groovy-comprehension:0.3'
//  compile 'org.jggug.kobo:groovy-comprehension:0.3:java8'
    testCompile 'junit:junit:4.11'
}
```

### Get the jar by hand

You can download jars from [here](https://bintray.com/bintray/jcenter?filterByPkgName=groovy-comprehension).
Put them on the classpath directory and specify -cp option to there.

### Grape/@Grab

You can use Groovy [Grape](http://groovy.codehaus.org/Grape)'s `@Grab` annotation:

```groovy
@Grab("org.jggug.kobo:groovy-comprehension:0.3")
import groovyx.comprehension.keyword.select;
```

When you want to use Java 8 streams, specifiy classifier `java8` for the Grab parameter:

```groovy
@Grab("org.jggug.kobo:groovy-comprehension:0.3:java8")
import groovyx.comprehension.keyword.select;

select(n) { n:1..10 }.each{
  println it
}
```

## Conversion

This feature is implemented as [global AST transformation](http://groovy.codehaus.org/Global+AST+Transformations) and [groovy extension method](http://groovy.codehaus.org/Creating+an+extension+module).

This code
```groovy
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

Any class which have follwing instance method can be used with comprehension.

* bind(Closure c)
* yield(x)
* autoGuard(exp)


