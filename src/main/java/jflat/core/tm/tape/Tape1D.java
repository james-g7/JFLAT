package core.tm.tape;

public class Tape1D <T> {
    private Node1D<T> cur = new Node1D<>(null, null, null);


    public Tape1D() {}

    public T read() {
        return cur.getData();
    }

    public void write(T data) {
        cur.setData(data);
    }

    public void moveRight() {
        if (cur.getRight() == null) {
            Node1D<T> newNode = new Node1D<>(null, cur, null);
            cur.setRight(newNode);
            cur = newNode;
        } else {
            cur = cur.getRight();
        }
    }

    public void moveLeft() {
        if (cur.getLeft() == null) {
            Node1D<T> newNode = new Node1D<>(null, null, cur);
            cur.setLeft(newNode);
            cur = newNode;
        } else {
            cur = cur.getLeft();
        }
    }
}
