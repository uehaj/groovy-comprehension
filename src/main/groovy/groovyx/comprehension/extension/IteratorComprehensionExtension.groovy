/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.comprehension.extension
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam
import groovy.transform.stc.FirstParam.FirstGenericType
import org.codehaus.groovy.runtime.DefaultGroovyMethods as DGM;
/**
 * @author Uehara Junji(@uehaj)
 */
// TODO: rewrite in java for GDK DocGenerator 
class IteratorComprehensionExtension {

    static <T> Iterator<T> bind(@DelegatesTo.Target('a') Iterator<T> self, @DelegatesTo(target='a') @ClosureParams(FirstParam.FirstGenericType.class) Closure c) { // Haskell's >>=
        c.delegate = self
        Iterator current = null
        return new Iterator() {
            @Override
            public boolean hasNext() {
                if (current == null || !current.hasNext()) {
                    if (self.hasNext()) {
                        current = c(self.next())
                        while (!current.hasNext() && self.hasNext()) {
                            current = c(self.next())
                        }
                        return current.hasNext()
                    }
                    return false
                }
                return current.hasNext()
            }
            @Override
            public next() {
                return current.next()
            }
            @Override
            public void remove() { /* not implemented */ }
        }
    }

    static <T> Iterator<T> bind0(Iterator<T> self, value) { // Haskell's >>
        self.bind{_-> delegate.yield(value)}
    }

    static <T> Iterator<T> ap(Iterator<T> self, Iterator<T> xs) { // Haskell's <*>
        DGM.toList(self).bind({Closure f->
        xs.bind({x->
            if (f.parameterTypes.size() > 1) {
                // explicit partial application. (don't be curried automatically
                // dislike Haskel.)
                return [f.curry(x)]
            }
            else {
                return [f(x)]
            }
        })}).iterator()
    }
    
    static <T> Iterator<T> guard(Iterator<T> _, boolean cond) {
        if (cond) {
            return yield(null, null)
        }
        else {
            return mzero(null)
        }
    }

    static <T> Iterator<T> where(Iterator<T> _, value) { // alias of guard
        return guard(_, value)
    }

    static <T> Iterator<T> autoGuard(Iterator<T> _, value) {
        if (value instanceof Boolean) {
            return guard(_, value)
        }
        return value
    }

    static <T> Iterator<T> yield(Iterator<T> _, value) { // haskell's return
        return [value].iterator()
    }

    static <T> Iterator<T> mzero(Iterator<T> _) { // haskell's mzero
        return new Iterator() {
            @Override
            public boolean hasNext() {
                return false
            }
            @Override
            public next() {
                return null
            }
            @Override
            public void remove() { /* not implemented */ }
        }
    }
}
