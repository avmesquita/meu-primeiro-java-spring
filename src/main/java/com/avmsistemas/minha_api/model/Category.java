package com.avmsistemas.minha_api.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "categories") // Nome da tabela no banco de dados
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    // Relacionamento um-para-muitos com Product (uma categoria pode ter muitos produtos)
    // MappedBy indica que o campo "category" na entidade Product é o dono da relação
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products; // Opcional, pode ser útil para navegação bidirecional

    public Category() {
    }

    public Category(String name) {
        this.name = name;
    }


}