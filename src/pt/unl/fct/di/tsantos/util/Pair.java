package pt.unl.fct.di.tsantos.util;

public class Pair<A, B> {

    protected A fst;
    protected B snd;

    public Pair(A fst, B snd) {
        this.fst = fst;
        this.snd = snd;
    }

    public A getFst() {
        return fst;
    }

    public void setFst(A fst) {
        this.fst = fst;
    }

    public B getSnd() {
        return snd;
    }

    public void setSnd(B snd) {
        this.snd = snd;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair<A, B> other = (Pair<A, B>) obj;
        if (this.fst != other.fst && (this.fst == null
                || !this.fst.equals(other.fst))) {
            return false;
        }
        if (this.snd != other.snd && (this.snd == null
                || !this.snd.equals(other.snd))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.fst != null ? this.fst.hashCode() : 0);
        hash = 67 * hash + (this.snd != null ? this.snd.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "(" + fst.toString() + ", " + snd.toString() + ")";
    }

    public static <A,B> Pair<A,B> sndNull(A fst, Class<B> c) {
        return new Pair<A,B>(fst, null);
    }

    public static <A,B> Pair<A,B> fstNull(B snd, Class<B> c) {
        return new Pair<A,B>(null, snd);
    }
}
