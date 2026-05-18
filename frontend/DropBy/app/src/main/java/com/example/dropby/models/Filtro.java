package com.example.dropby.models;

public class Filtro {
    private String city;
    private String category;
    private double priceMax;

    // Costruttore vuoto
    public Filtro() {}

    // Getter e Setter (indispensabili per far funzionare .getCity() e .setCity())
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPriceMax() {
        return priceMax;
    }

    public void setPriceMax(double priceMax) {
        this.priceMax = priceMax;
    }
}
