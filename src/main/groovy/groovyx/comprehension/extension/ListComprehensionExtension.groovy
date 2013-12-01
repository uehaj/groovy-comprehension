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

/**
 * @author Uehara Junji(@uehaj)
 */
class ListComprehensionExtension {

    static List bind(List self, @DelegatesTo(List) Closure c) { // Haskell's >>=
        c.delegate = self
        self.collect(c).inject([], {acc,elem->acc+elem})
    }

    static List bind0(List self, value) { // Haskell's >>
        self.collect{_->value}.inject([], {acc,elem->acc+elem})
    }

    static List ap(List self, List xs) { // Haskell's <*>
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
    
    static List guard(List _, boolean cond) {
        if (cond) {
            return yield(null, null)
        }
        else {
            return mzero(null)
        }
    }

    static List where(List _, value) { // alias of guard
        return guard(_, value)
    }

    static List autoGuard(List _, value) {
        if (value instanceof Boolean) {
            return guard(_, value)
        }
        return value
    }

    static List yield(List _, value) { // haskell's return
        return [value]
    }

    static List mzero(List _) { // haskell's mzero
        return []
    }
}
