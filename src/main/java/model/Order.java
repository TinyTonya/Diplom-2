package model;

import java.util.List;

public class Order {
    private List<String> ingredients;

    public Order() {
    }

    public Order(List<String> ingredients) {
        this.ingredients=ingredients;
    }

    public List<String> getIngredient() {
        return ingredients;
    }

    public void setIngredient(List<String> ingredient) {
        this.ingredients=ingredients;
    }
}
