package model;

import java.util.List;

public class ListOfOrders {
    private List<Order> orders;

    public ListOfOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Order> getOrders() {
        return orders;
    }
}
