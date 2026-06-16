package core.tm.tape;

public class Node1D<T> {
    private T data;
    private Node1D<T> left;
    private Node1D<T> right;

    protected Node1D(T data, Node1D<T> left, Node1D<T> right) {
        this.data = data;
        this.left = left;
        this.right = right;
    }

    protected T getData() {
        return data;
    }

    protected void setData(T data) {
        this.data = data;
    }

    protected Node1D<T> getLeft() {
        return left;
    }

    protected void setLeft(Node1D<T> left) {
        this.left = left;
    }

    protected Node1D<T> getRight() {
        return right;
    }

    protected void setRight(Node1D<T> right) {
        this.right = right;
    }
}
