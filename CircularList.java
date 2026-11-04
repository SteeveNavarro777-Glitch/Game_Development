import java.util.NoSuchElementException;
import java.util.Objects;

/** A singly linked circular list: O(1) append, current and advance. */
public final class CircularList<T> {
    private static final class Node<T> {
        final T value;
        Node<T> next;
        Node(T value) { this.value = value; }
    }
    private Node<T> tail, cursor;
    private int size;

    public void add(T value) {
        Node<T> node = new Node<>(Objects.requireNonNull(value));
        if (tail == null) { node.next = node; cursor = node; }
        else { node.next = tail.next; tail.next = node; }
        tail = node;
        size++;
    }
    public T current() {
        if (cursor == null) throw new NoSuchElementException("Empty circular list");
        return cursor.value;
    }
    public T advance() {
        if (cursor == null) throw new NoSuchElementException("Empty circular list");
        cursor = cursor.next;
        return cursor.value;
    }
    public int size() { return size; }
}
