package br.com.elotech.api.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContatoInput {

    private Long id;

    @NotBlank(message = "Informe o nome do contato")
    private String nome;

    @NotBlank(message = "Informe o telefone do contato")
    private String telefone;

    @NotBlank(message = "Informe o e-mail do contato")
    @Email(message = "O e-mail do contato está inválido")
    private String email;

}
