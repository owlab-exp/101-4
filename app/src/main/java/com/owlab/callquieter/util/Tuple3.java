package com.owlab.callquieter.util;

import java.util.Objects;

/**
 * Created by ernest on 7/3/16.
 */
public class Tuple3 <T1, T2, T3> {
    public final T1 t1;
    public final T2 t2;
    public final T3 t3;

    public Tuple3(T1 t1, T2 t2, T3 t3) {
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Tuple3<?, ?, ?>) {
            Tuple3<?, ?, ?> other = (Tuple3<?, ?, ?>) o;
            return Objects.equals(this.t1, other.t1) && Objects.equals(this.t2, other.t2) && Objects.equals(this.t3, other.t3);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(t1, t2, t3);

    }
}
