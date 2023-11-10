/**
 * A special class to make my life easier
 * It's basically a container that holds two of the same type of variable
 */
public class Pair<A, B> {
    private A item1;
    private B item2;

    public Pair(A item1, B item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    public A getItem1() {
        return item1;
    }

    public B getItem2() {
        return item2;
    }
}
