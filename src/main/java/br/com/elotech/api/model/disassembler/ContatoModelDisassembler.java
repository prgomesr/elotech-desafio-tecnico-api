package br.com.elotech.api.model.disassembler;

import br.com.elotech.api.model.dto.ContatoInput;
import br.com.elotech.domain.model.Contato;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContatoModelDisassembler {

    @Autowired
    private ModelMapper modelMapper;

    public Contato toDomainObject(ContatoInput input) {
        return modelMapper.map(input, Contato.class);
    }

}
