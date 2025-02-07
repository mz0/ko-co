package org.example.list;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class LinkedListTest {
    @Test void testConstructor() {
        LinkedList list = new LinkedList();
        assertThat(list.size()).isEqualTo(0);
    }

    @Test void testAdd() {
        LinkedList list = new LinkedList();

        list.add("one");
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo("one");
    }

}
