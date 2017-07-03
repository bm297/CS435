import java.util.Arrays;

// MinHeap implementation
public class MinPQ<T extends Comparable<T>> {
    private static final int DEFAULT_CAPACITY = 2;
    private T[] array;      // Pointer to heap array
    private int size;       // Size of heap

    @SuppressWarnings("unchecked")
    public MinPQ () {
        array = (T[])new Comparable[DEFAULT_CAPACITY];  
        size = 0;
    }    

    // Insert element into heap
    public void insert(T value) {
        // Resize if needed
        if (size >= array.length - 1) array = this.resize();        
        
        size++;
        int index = size;
        array[index] = value;
        bubbleUp();
    }
    
    // Check if heap is empty
    public boolean isEmpty() {
        return size == 0;
    }

    // Return size of heap
    public int size() {
    	return size;
    }
    
    public T peek() {
        if (this.isEmpty()) {
            throw new IllegalStateException();
        }
        
        return array[1];
    }

    // Remove element from heap
    public T remove() {
    	T result = peek();
    	array[1] = array[size];
    	array[size] = null;
    	size--;
    	siftdown();      // Put element in its correct place
    	return result;
    }
    
    // Put element in its correct place
    private void siftdown() {
        int index = 1;
        while (hasLeftChild(index)) {
            int smallerChild = leftIndex(index);
            if (hasRightChild(index)
                && array[leftIndex(index)].compareTo(array[rightIndex(index)]) > 0) {
                smallerChild = rightIndex(index);
        } if (array[index].compareTo(array[smallerChild]) > 0) {
            swap(index, smallerChild);
        } else {
            break;
        }
        index = smallerChild;
    }        
}

    // Put element in its correct place 
private void bubbleUp() {
    int index = this.size;
    while (hasParent(index)
        && (parent(index).compareTo(array[index]) > 0)) {         
        swap(index, parentIndex(index));
    index = parentIndex(index);
}        
}

    // Checks if index has parent
public boolean hasParent(int i) {
    return i > 1;
}

    // Returns the pos of leftchild
public int leftIndex(int i) {
    return i * 2;
}

    // Returns the pos of rightchild
public int rightIndex(int i) {
    return i * 2 + 1;
}

    // Checks if index has leftchild
public boolean hasLeftChild(int i) {
    return leftIndex(i) <= size;
}

    // Checks of index has rightchild
public boolean hasRightChild(int i) {
    return rightIndex(i) <= size;
}

    // Returns the parent element
public T parent(int i) {
    return array[parentIndex(i)];
}

    // Returns the pos of parent
public int parentIndex(int i) {
    return i / 2;
}

    // Resize the array
private T[] resize() {
    return Arrays.copyOf(array, array.length * 2);
}

    // Swap elements
private void swap(int index1, int index2) {
    T tmp = array[index1];
    array[index1] = array[index2];
    array[index2] = tmp;        
}

    // String representation of heap
public String toString() {
    return Arrays.toString(array);
}
}