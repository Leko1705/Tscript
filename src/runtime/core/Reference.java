package runtime.core;

public class Reference implements Data {
    private boolean marked = false;
    private int refCount = 1;

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public void incRC(){
        refCount++;
    }

    public void decRC(){
        refCount--;
    }

    public int getRC(){
        return refCount;
    }
}
