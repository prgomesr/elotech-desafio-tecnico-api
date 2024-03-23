package br.com.elotech.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Pessoa extends EntidadeBase {

    private String nome;
    private String cpf;
    private LocalDate dataNascimento;

    @OneToMany(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contato> contatos;

    @CreationTimestamp
    private OffsetDateTime dataHoraCadastro;

    @UpdateTimestamp
    private OffsetDateTime dataHoraAtualizacao;
}
