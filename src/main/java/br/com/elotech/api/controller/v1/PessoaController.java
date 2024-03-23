package br.com.elotech.api.controller.v1;

import br.com.elotech.api.model.assembler.PessoaModelAssembler;
import br.com.elotech.api.model.disassembler.ContatoModelDisassembler;
import br.com.elotech.api.model.disassembler.PessoaModelDisassembler;
import br.com.elotech.api.model.dto.ContatoInput;
import br.com.elotech.api.model.dto.PessoaInput;
import br.com.elotech.api.model.dto.PessoaModel;
import br.com.elotech.domain.model.Contato;
import br.com.elotech.domain.model.Pessoa;
import br.com.elotech.domain.repository.PessoaRepository;
import br.com.elotech.domain.repository.filter.PessoaFilter;
import br.com.elotech.domain.service.PessoaService;
import br.com.elotech.infra.repository.spec.PessoaSpecs;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/v1/pessoas", produces = MediaType.APPLICATION_JSON_VALUE)
public class PessoaController {

    @Autowired
    private PessoaRepository repository;

    @Autowired
    private PessoaService service;

    @Autowired
    private PessoaModelAssembler modelAssembler;

    @Autowired
    private PessoaModelDisassembler modelDisassembler;

    @Autowired
    private ContatoModelDisassembler contatoModelDisassembler;

    @GetMapping
    public Page<PessoaModel> listar(PessoaFilter filter, Pageable pageable) {
        Page<Pessoa> pessoasPage = repository.findAll(PessoaSpecs.comFiltro(filter), pageable);

        List<PessoaModel> pessoasModel = modelAssembler.toCollectionModel(pessoasPage.getContent());

        return new PageImpl<>(pessoasModel, pageable, pessoasPage.getTotalElements());
    }

    @GetMapping("{id}")
    public PessoaModel buscar(@PathVariable Long id) {
        return modelAssembler
                .toModel(service.buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PessoaModel adicionar(@RequestBody @Valid PessoaInput input) {

        Pessoa pessoa = modelDisassembler.toDomainObject(input);

        Pessoa pessoaSalva = service.adicionar(pessoa);

        return modelAssembler.toModel(pessoaSalva);
    }

    @PutMapping("{id}")
    public PessoaModel atualizar(@RequestBody @Valid PessoaInput input, @PathVariable Long id) {
        Pessoa pessoaSalva = service.buscar(id);

        modelDisassembler.copyToDomainObject(input, pessoaSalva);

        List<Contato> contatos = new ArrayList<>();

        for (ContatoInput contatoInput : input.getContatos()) {
            Contato contato = contatoModelDisassembler.toDomainObject(contatoInput);
            contatos.add(contato);
        }

        pessoaSalva = service.atualizar(pessoaSalva, contatos);

        return modelAssembler.toModel(pessoaSalva);
    }

    @DeleteMapping({"{id}"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
