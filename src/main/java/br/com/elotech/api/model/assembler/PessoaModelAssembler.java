package br.com.elotech.api.model.assembler;

import br.com.elotech.api.model.dto.PessoaModel;
import br.com.elotech.domain.model.Pessoa;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PessoaModelAssembler {

    @Autowired
    private ModelMapper modelMapper;

    public PessoaModel toModel(Pessoa pessoa) {
        return modelMapper.map(pessoa, PessoaModel.class);
    }

    public List<PessoaModel> toCollectionModel(Collection<Pessoa> pessoas) {
        return pessoas.stream().map(this::toModel).collect(Collectors.toList());
    }

}
