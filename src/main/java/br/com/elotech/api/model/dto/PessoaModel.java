package br.com.elotech.api.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PessoaModel {

    private Long id;
    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private List<ContatoModel> contatos;

}
