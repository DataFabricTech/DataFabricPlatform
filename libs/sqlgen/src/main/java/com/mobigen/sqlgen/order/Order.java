package com.mobigen.sqlgen.order;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class Order implements Comparable<Order> {
    private Integer order;
    private String field;
    private Direction direction;

    public Order(Integer order, String field, String direction) {
        this.order = order;
        this.field = field;
        this.direction = Direction.valueOf(direction);
    }

    public String getStatement() {
        return field + " " + direction.name();
    }

    @Override
    public int compareTo(Order o) {
        if (this.order > o.order) {
            return 1;
        } else if (this.order.equals(o.order)) {
            return 0;
        } else {
            return -1;
        }
    }
}
