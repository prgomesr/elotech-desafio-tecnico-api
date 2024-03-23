package br.com.elotech.infra.repository.spec;

import br.com.elotech.domain.model.Pessoa;
import br.com.elotech.domain.repository.filter.PessoaFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;

public class PessoaSpecs {

    public static Specification<Pessoa> comFiltro(PessoaFilter filtro) {
        return (root, query, builder) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filtro.getNome())) {
                predicates
                        .add(builder.like(builder.lower(root.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
