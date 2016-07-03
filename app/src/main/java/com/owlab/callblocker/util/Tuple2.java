package com.owlab.callblocker.util;

import java.util.Objects;

/**
 * Created by ernest on 7/3/16.
 */
public class Tuple2<T1, T2> {
    public final T1 t1;
    public final T2 t2;

    public Tuple2(T1 t1, T2 t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Tuple2<?, ?>) {
            Tuple2<?, ?> other = (Tuple2<?, ?>) o;
            return Objects.equals(this.t1, other.t1) && Objects.equals(this.t2, other.t2);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(t1, t2);

    }
}
