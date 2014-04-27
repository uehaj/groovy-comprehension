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

import java.util.stream.Stream

/**
 * @author Uehara Junji(@uehaj)
 */
class StreamComprehensionExtension {

    static <T> Stream<T> bind(Stream<T> self, @DelegatesTo(Stream) Closure c) { // Haskell's >>=
        c.delegate = self
        self.flatMap(c)
    }

    static <T> Stream<T> bind0(Stream<T> self, value) { // Haskell's >>
        self.bind{_-> delegate.yield(value)}
    }

    static <T> Stream<T> ap(Stream<T> self, Stream<T> xs) { // Haskell's <*>
        self.bind({Closure f->
        xs.bind({x->
            if (f.parameterTypes.size() > 1) {
                // Haskellの様に引数が足り無ければ自動的に
                // 部分適用になるということは無いので、明示的に部分適用。
                return [f.curry(x)]
            }
            else {
                return [f(x)]
            }
        })})
    }

    static <T> Stream<T> guard(Stream<T> _, boolean cond) {
        if (cond) {
            return yield(null, null)
        }
        else {
            return mzero(null)
        }
    }

    static <T> Stream<T> where(Stream<T> _, value) { // alias of guard
        return guard(_, value)
    }

    static <T> Stream<T> autoGuard(Stream<T> _, value) {
        if (value instanceof Boolean) {
            return guard(_, value)
        }
        return value
    }

    static <T> Stream<T> yield(Stream<T> _, value) { // haskell's return
        return Stream.<T>of(value)
    }

    static <T> Stream<T> mzero(Stream<T> _) { // haskell's mzero
        return Stream.<T>empty()
    }
}
