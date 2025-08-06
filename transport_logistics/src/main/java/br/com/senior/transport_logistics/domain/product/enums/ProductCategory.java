package br.com.senior.transport_logistics.domain.product.enums;

public enum ProductCategory {

    ELECTRONICS("Eletrônicos"),
    CLOTHING("Roupas"),
    FOOD("Alimentos"),
    BOOKS("Livros"),
    FURNITURE("Móveis"),
    TOYS("Brinquedos"),
    SPORTS("Esportes"),
    BEAUTY("Beleza"),
    AUTOMOTIVE("Automotivo"),
    HOME_APPLIANCES("Eletrodomésticos");

    private String category;

    ProductCategory(String category) {
        this.category = category;
    }
}
