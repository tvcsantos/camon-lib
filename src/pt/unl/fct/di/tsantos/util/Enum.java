/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pt.unl.fct.di.tsantos.util;

import java.io.Serializable;
import java.io.IOException;
import java.util.*;

/** An enumerated type.  Enums are interned and can be compared with ==. */
public class Enum implements Internable, Serializable
{
    /** The name of the enum.  Used for debugging and interning. */
    private String name;

    /** The intern cache. */
    private static Map<EnumKey, Enum> cache =
            new HashMap<EnumKey, Enum>();

    protected Enum(String name) {
	this.name = name;

        // intern the enum and make sure this one is unique
        Enum intern = internEnum();

        if (intern != this) {
            throw new RuntimeException(
            /*throw new InternalCompilerError(*/"Duplicate enum \"" + name +
                                            "\"; this=" + this + " (" +
                                            this.getClass().getName() +
                                            "), intern=" + intern + " (" +
                                            intern.getClass().getName() + ")");
        }
    }

    /** For serialization. */
    private Enum() { }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /*@Override
    public boolean equals(Object o) {
	return this == o;
    }*/

    @Override
    public String toString() {
	return name;
    }

    private static class EnumKey {
        private Enum e;

	EnumKey(Enum e) {
	    this.e = e;
	}

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final EnumKey other = (EnumKey) obj;
            if (this.e != other.e && (this.e == null
                    || !this.e.equals(other.e))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + (this.e != null ? this.e.hashCode() : 0);
            return hash;
        }

        /*@Override
	public boolean equals(Object o) {
	    return o instanceof EnumKey
	        && e.name.equals(((EnumKey) o).e.name)
	        && e.getClass() == ((EnumKey) o).e.getClass();
	}*/

	/*public int hashCode() {
	    return e.getClass().hashCode() ^ e.name.hashCode();
	}*/

        @Override
        public String toString() {
            return e.toString();
        }
    }

    public Object intern() {
        return internEnum();
    }

    public Enum internEnum() {
        EnumKey k = new EnumKey(this);

	Enum e = (Enum) cache.get(k);

	if (e == null) {
	    cache.put(k, this);
	    return this;
	}

	return e;
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        intern();
    }
}
