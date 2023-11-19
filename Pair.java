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

    public String toString(){
        return "item 1: " + item1.toString() + " item2: "  + item2.toString();
    }

    @Override
    public boolean equals(Object obj){
        if(this.getClass() != obj.getClass()){
            return false;
        }
        Pair<?,?> otherPair = (Pair<?,?>)obj;
        if(otherPair.getItem1().equals(this.item1) && otherPair.getItem2().equals(this.item2)){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode(){
        int hash = item1.hashCode();
        return hash;
    }
}
