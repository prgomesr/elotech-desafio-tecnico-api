package br.com.elotech.domain.service;

import br.com.elotech.domain.exception.NegocioException;
import br.com.elotech.domain.model.Contato;
import br.com.elotech.domain.model.Pessoa;
import br.com.elotech.domain.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PessoaService {

    @Autowired
    private PessoaRepository pessoaRepository;

    public List<Pessoa> listar() {
        return pessoaRepository.findAll();
    }

    @Transactional
    public Pessoa adicionar(Pessoa pessoa) {
        setaPessoaEmContatos(pessoa.getContatos(), pessoa);
       return pessoaRepository.save(pessoa);
    }

    public Pessoa buscar(Long id) {
        return pessoaRepository.findById(id).orElseThrow(() -> new NegocioException("Cadastro não encontrado."));
    }

    @Transactional
    public void setaPessoaEmContatos(List<Contato> contatos, Pessoa pessoa) {
        contatos.forEach(contato -> contato.setPessoa(pessoa));
    }

    public Pessoa atualizar(Pessoa pessoa, List<Contato> contatos) {
       pessoa.getContatos().clear();
       pessoa.getContatos().addAll(contatos);
       setaPessoaEmContatos(contatos, pessoa);
       return pessoaRepository.save(pessoa);
    }

    @Transactional
    public void excluir(Long id) {
        Pessoa pessoa = buscar(id);
        pessoaRepository.delete(pessoa);
    }

}
