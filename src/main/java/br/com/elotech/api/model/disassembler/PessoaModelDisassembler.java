package br.com.elotech.api.model.disassembler;

import br.com.elotech.api.model.dto.PessoaInput;
import br.com.elotech.domain.model.Pessoa;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PessoaModelDisassembler {

    @Autowired
    private ModelMapper modelMapper;

    public Pessoa toDomainObject(PessoaInput input) {
        modelMapper.typeMap(PessoaInput.class, Pessoa.class).addMapping(PessoaInput::getContatos, Pessoa::setContatos);
        return modelMapper.map(input, Pessoa.class);
    }

    public void copyToDomainObject(PessoaInput input, Pessoa pessoa) {
        pessoa.getContatos().clear();
        modelMapper.typeMap(PessoaInput.class, Pessoa.class).addMappings(mapper -> mapper.skip(Pessoa::setContatos));
        modelMapper.map(input, pessoa);
    }

}
