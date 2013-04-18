
package org.fcrepo.identifiers;

import com.google.common.base.Function;

public abstract class BasePidMinter implements PidMinter {

    @Override
    public Function<Object, String> makePid() {
        return new Function<Object, String>() {

            @Override
            public String apply(final Object input) {
                return mintPid();
            }
        };
    }

}
