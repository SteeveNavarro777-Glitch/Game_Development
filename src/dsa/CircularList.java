/* FILE PURPOSE: DSA LINKED LIST | Custom circular singly linked nodes for patrol waypoints.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.util.NoSuchElementException;
import java.util.Objects;

/** A singly linked circular list: O(1) append, current and advance. */
public final class CircularList<T> {
    // [SECTION: DSA - LINKED LIST NODES] Each node stores data and the next-node reference.
    private static final class Node<T> {
        final T value;
        Node<T> next;
        // [BLOCK: FUNCTION] CircularList.Node - Initialize Node state.
        Node(T value) { this.value = value; }
    }
    // [SECTION: OOP - ENCAPSULATION] Only this class can change the links, cursor or size.
    private Node<T> tail, cursor;
    private int size;

    // [BLOCK: DSA: LINKED LIST] CircularList.add - Append a node and reconnect the tail to the first node; O(1).
    public void add(T value) {
        Node<T> node = new Node<>(Objects.requireNonNull(value));
        if (tail == null) {
            // First node points to itself: the circular-list invariant.
            node.next = node;
            cursor = node;
        } else {
            // Insert after the old tail, while preserving its link to the first node.
            node.next = tail.next;
            tail.next = node;
        }
        tail = node;
        size++;
    }
    // [BLOCK: DSA: LINKED LIST] CircularList.current - Read the cursor node without changing traversal; O(1).
    public T current() {
        if (cursor == null) throw new NoSuchElementException("Empty circular list");
        return cursor.value;
    }
    // [BLOCK: DSA: LINKED LIST] CircularList.advance - Follow next, wrapping naturally through the circular link; O(1).
    public T advance() {
        if (cursor == null) throw new NoSuchElementException("Empty circular list");
        cursor = cursor.next;
        return cursor.value;
    }
    // [BLOCK: DSA: LINKED LIST] CircularList.size - Return the maintained node count; O(1).
    public int size() { return size; }
}
