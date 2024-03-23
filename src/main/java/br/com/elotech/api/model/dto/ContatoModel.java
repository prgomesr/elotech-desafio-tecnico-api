package br.com.elotech.api.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContatoModel {

    private Long id;
    private String nome;
    private String telefone;
    private String email;

}
