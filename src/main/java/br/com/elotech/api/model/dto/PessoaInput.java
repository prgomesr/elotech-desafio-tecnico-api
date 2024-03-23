package br.com.elotech.api.model.dto;

import br.com.elotech.core.validation.DataNascimento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PessoaInput {

    private Long id;

    @NotBlank(message = "Informe o nome")
    private String nome;

    @NotBlank(message = "Informe o CPF")
    @CPF(message = "CPF inválido")
    private String cpf;

    @DataNascimento(message = "A data de nascimento não pode ser uma data futura")
    private LocalDate dataNascimento;

    @NotEmpty(message = "Informe ao menos um contato")
    private List<@Valid ContatoInput> contatos;

}
