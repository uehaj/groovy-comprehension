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
import org.codehaus.groovy.runtime.DefaultGroovyMethods as DGM
/**
 * @author Uehara Junji(@uehaj)
 */
// TODO: rewrite in java for GDK DocGenerator 
class ListComprehensionExtension {

    static <T> Collection<T> bind(@DelegatesTo.Target('a') Collection<T> self, @DelegatesTo(target='a') @ClosureParams(FirstParam.FirstGenericType.class) Closure c) { // Haskell's >>=
        c.delegate = self
        DGM.asList(self).collect(c).inject([], {acc,elem->acc+elem})
    }

    static <T> Collection<T> bind0(Collection<T> self, value) { // Haskell's >>
        DGM.asList(self).collect().inject([], {acc,elem->acc+elem})
    }

    static <T> Collection<T> ap(Collection<T> self, Collection<T> xs) { // Haskell's <*>
        DGM.asList(self).bind({Closure f->
        xs.bind({x->
            if (f.parameterTypes.size() > 1) {
                // explicit partial application. (don't be curried automatically
                // dislike Haskel.)
                return [f.curry(x)]
            }
            else {
                return [f(x)]
            }
        })})
    }
    
    static <T> Collection<T> guard(Collection<T> _, boolean cond) {
        if (cond) {
            return yield(null, null)
        }
        else {
            return mzero(null)
        }
    }

    static <T> Collection<T> where(Collection<T> _, value) { // alias of guard
        return guard(_, value)
    }

    static <T> Collection<T> autoGuard(Collection<T> _, value) {
        if (value instanceof Boolean) {
            return guard(_, value)
        }
        return value
    }

    static <T> Collection<T> yield(Collection<T> _, value) { // haskell's return
        return [value]
    }

    static <T> Collection<T> mzero(Collection<T> _) { // haskell's mzero
        return []
    }
}
