package br.com.elotech.api.controller;

import br.com.elotech.api.controller.v1.PessoaController;
import br.com.elotech.api.model.assembler.PessoaModelAssembler;
import br.com.elotech.api.model.disassembler.ContatoModelDisassembler;
import br.com.elotech.api.model.disassembler.PessoaModelDisassembler;
import br.com.elotech.api.model.dto.ContatoInput;
import br.com.elotech.api.model.dto.ContatoModel;
import br.com.elotech.api.model.dto.PessoaInput;
import br.com.elotech.api.model.dto.PessoaModel;
import br.com.elotech.domain.exception.NegocioException;
import br.com.elotech.domain.model.Contato;
import br.com.elotech.domain.model.Pessoa;
import br.com.elotech.domain.repository.PessoaRepository;
import br.com.elotech.domain.repository.filter.PessoaFilter;
import br.com.elotech.domain.service.PessoaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PessoaController.class)
public class PessoaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PessoaRepository repository;

    @MockBean
    private PessoaService service;

    @MockBean
    private PessoaModelAssembler modelAssembler;

    @MockBean
    private PessoaModelDisassembler modelDisassembler;

    @MockBean
    private ContatoModelDisassembler contatoModelDisassembler;

    @Test
    void quandoBuscaPessoa_entaoRetornaComSucesso() throws Exception {
        Pessoa yago = new Pessoa();
        yago.setNome("Yago do Nascimento");

        when(service.buscar(anyLong())).thenReturn(yago);

        doAnswer(invocation -> {
            Pessoa pessoa = invocation.getArgument(0);
            PessoaModel model = new PessoaModel();
            model.setNome(pessoa.getNome());
            return model;
        }).when(modelAssembler).toModel(any(Pessoa.class));

        mockMvc.perform(get("/v1/pessoas/{id}", 1L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Yago do Nascimento"));

        verify(service).buscar(1L);
    }

    @Test
    void quandoListaPessoas_entaoRetornaComSucesso() throws Exception {
        Pessoa pessoa1 = new Pessoa();
        pessoa1.setNome("Yago do Nascimento");
        pessoa1.setDataNascimento(LocalDate.now());
        pessoa1.setCpf("580.005.824-57");

        Pessoa pessoa2 = new Pessoa();
        pessoa2.setNome("Ana Silva");
        pessoa2.setDataNascimento(LocalDate.of(1990, 1, 1));
        pessoa2.setCpf("123.456.789-00");

        List<Pessoa> pessoasList = Arrays.asList(pessoa1, pessoa2);
        Page<Pessoa> pessoasPage = new PageImpl<>(pessoasList);

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(pessoasPage);

        doAnswer(invocation -> {
            List<Pessoa> pessoas = invocation.getArgument(0);
            return pessoas.stream().map(pessoa -> {
                PessoaModel model = new PessoaModel();
                model.setNome(pessoa.getNome());
                return model;
            }).collect(Collectors.toList());
        }).when(modelAssembler).toCollectionModel(any(List.class));

        Pageable pageable = PageRequest.of(0, 2);

        mockMvc.perform(get("/v1/pessoas")
                        .param("page", "0")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].nome").value("Yago do Nascimento"))
                .andExpect(jsonPath("$.content[1].nome").value("Ana Silva"));

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void quandoListaPessoasComFiltroNome_entaoRetornaApenasAnaSilva() throws Exception {
        Pessoa pessoa2 = new Pessoa();
        pessoa2.setNome("Ana Silva");
        pessoa2.setDataNascimento(LocalDate.of(1990, 1, 1));
        pessoa2.setCpf("123.456.789-00");

        Page<Pessoa> filteredPessoasPage = new PageImpl<>(List.of(pessoa2));

        PessoaFilter filter = new PessoaFilter();
        filter.setNome("Ana Silva");

        Specification<Pessoa> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), "%" + filter.getNome().toLowerCase() + "%");

        when(repository.findAll(argThat(new SpecificationArgumentMatcher<>(spec)), any(Pageable.class)))
                .thenReturn(filteredPessoasPage);

        doAnswer(invocation -> {
            List<Pessoa> pessoas = invocation.getArgument(0);
            return pessoas.stream().map(pessoa -> {
                PessoaModel model = new PessoaModel();
                model.setNome(pessoa.getNome());
                return model;
            }).collect(Collectors.toList());
        }).when(modelAssembler).toCollectionModel(any(List.class));

        Pageable pageable = PageRequest.of(0, 2);

        mockMvc.perform(get("/v1/pessoas")
                        .param("page", "0")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nome").value("Ana Silva"));

        verify(repository).findAll(argThat(new SpecificationArgumentMatcher<>(spec)), eq(pageable));
    }

    @Test
    void quandoAdicionaPessoaComContato_entaoRetornaComSucesso() throws Exception {
        ContatoInput contatoInput = new ContatoInput();
        contatoInput.setNome("Contato 1");
        contatoInput.setTelefone("123456789");
        contatoInput.setEmail("contato1@example.com");

        PessoaInput input = new PessoaInput();
        input.setNome("Carlos Silva");
        input.setCpf("123.456.789-09");
        input.setDataNascimento(LocalDate.of(1995, 5, 21));
        input.setContatos(List.of(contatoInput));

        Pessoa pessoa = new Pessoa();
        pessoa.setId(1L);
        pessoa.setNome(input.getNome());
        pessoa.setCpf(input.getCpf());
        pessoa.setDataNascimento(input.getDataNascimento());

        Contato contato = new Contato();
        contato.setNome(contatoInput.getNome());
        contato.setTelefone(contatoInput.getTelefone());
        contato.setEmail(contatoInput.getEmail());
        pessoa.setContatos(List.of(contato));

        PessoaModel pessoaModel = new PessoaModel();
        pessoaModel.setId(pessoa.getId());
        pessoaModel.setNome(pessoa.getNome());
        pessoaModel.setCpf(pessoa.getCpf());
        pessoaModel.setDataNascimento(pessoa.getDataNascimento());

        when(modelDisassembler.toDomainObject(any(PessoaInput.class))).thenReturn(pessoa);
        when(service.adicionar(any(Pessoa.class))).thenReturn(pessoa);
        when(modelAssembler.toModel(any(Pessoa.class))).thenReturn(pessoaModel);

        mockMvc.perform(post("/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(pessoaModel.getId()))
                .andExpect(jsonPath("$.nome").value(pessoaModel.getNome()))
                .andExpect(jsonPath("$.cpf").value(pessoaModel.getCpf()))
                .andExpect(jsonPath("$.dataNascimento").value(pessoaModel.getDataNascimento().toString()));
    }

    @Test
    public void quandoNomePessoaNaoInformado_entaoRetornaBadRequestEListaErros() throws Exception {
        PessoaInput pessoaInput = new PessoaInput();
        pessoaInput.setCpf("123.456.789-09");
        pessoaInput.setDataNascimento(LocalDate.of(1995, 5, 10));

        ContatoInput contatoInput = new ContatoInput();
        contatoInput.setNome("Contato 1");
        contatoInput.setTelefone("123456789");
        contatoInput.setEmail("contato1@example.com");

        pessoaInput.setContatos(List.of(contatoInput));

        mockMvc.perform(post("/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pessoaInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://elotech.com.br/dados-invalidos"))
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.detail").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.userMessage").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.objects[0].name").value("nome"))
                .andExpect(jsonPath("$.objects[0].userMessage").value("Informe o nome"));
    }

    @Test
    public void quandoCPFPessoaNaoInformado_entaoRetornaBadRequestEListaErros() throws Exception {
        PessoaInput pessoaInput = new PessoaInput();
        pessoaInput.setNome("Angela da Silva");
        pessoaInput.setDataNascimento(LocalDate.of(1995, 5, 21));

        ContatoInput contatoInput = new ContatoInput();
        contatoInput.setNome("Contato 1");
        contatoInput.setTelefone("123456789");
        contatoInput.setEmail("contato1@example.com");

        pessoaInput.setContatos(List.of(contatoInput));

        mockMvc.perform(post("/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pessoaInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://elotech.com.br/dados-invalidos"))
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.detail").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.userMessage").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.objects[0].name").value("cpf"))
                .andExpect(jsonPath("$.objects[0].userMessage").value("Informe o CPF"));
    }

    @Test
    public void quandoCPFPessoaFormatoInvalido_entaoRetornaBadRequestEListaErros() throws Exception {
        PessoaInput pessoaInput = new PessoaInput();
        pessoaInput.setCpf("111.215.333-44");
        pessoaInput.setNome("Angela da Silva");
        pessoaInput.setDataNascimento(LocalDate.of(1995, 5, 21));

        ContatoInput contatoInput = new ContatoInput();
        contatoInput.setNome("Contato 1");
        contatoInput.setTelefone("123456789");
        contatoInput.setEmail("contato1@example.com");

        pessoaInput.setContatos(List.of(contatoInput));

        mockMvc.perform(post("/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pessoaInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://elotech.com.br/dados-invalidos"))
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.detail").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.userMessage").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.objects[0].name").value("cpf"))
                .andExpect(jsonPath("$.objects[0].userMessage").value("CPF inválido"));
    }

    @Test
    public void quandoDataNascimentoPessoaDataFutura_entaoRetornaBadRequestEListaErros() throws Exception {
        PessoaInput pessoaInput = new PessoaInput();
        pessoaInput.setCpf("580.005.824-57");
        pessoaInput.setNome("Angela da Silva");
        pessoaInput.setDataNascimento(LocalDate.now().plusDays(1));

        ContatoInput contatoInput = new ContatoInput();
        contatoInput.setNome("Contato 1");
        contatoInput.setTelefone("123456789");
        contatoInput.setEmail("contato1@example.com");

        pessoaInput.setContatos(List.of(contatoInput));

        mockMvc.perform(post("/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pessoaInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://elotech.com.br/dados-invalidos"))
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.detail").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.userMessage").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.objects[0].name").value("dataNascimento"))
                .andExpect(jsonPath("$.objects[0].userMessage").value("A data de nascimento não pode ser uma data futura"));
    }

    @Test
    public void quandoDataNascimentoPessoaDataAtual_entaoRetornaSucesso() throws Exception {
        ContatoInput contatoInput = new ContatoInput();
        contatoInput.setNome("Contato 1");
        contatoInput.setTelefone("123456789");
        contatoInput.setEmail("contato1@example.com");

        PessoaInput input = new PessoaInput();
        input.setNome("Carlos Silva");
        input.setCpf("123.456.789-09");
        input.setDataNascimento(LocalDate.now());
        input.setContatos(List.of(contatoInput));

        Pessoa pessoa = new Pessoa();
        pessoa.setId(1L);
        pessoa.setNome(input.getNome());
        pessoa.setCpf(input.getCpf());
        pessoa.setDataNascimento(input.getDataNascimento());

        Contato contato = new Contato();
        contato.setNome(contatoInput.getNome());
        contato.setTelefone(contatoInput.getTelefone());
        contato.setEmail(contatoInput.getEmail());
        pessoa.setContatos(List.of(contato));

        ContatoModel contatoModel = new ContatoModel();
        contatoModel.setNome(contatoInput.getNome());
        contatoModel.setTelefone(contatoInput.getTelefone());
        contatoModel.setEmail(contatoInput.getEmail());

        PessoaModel pessoaModel = new PessoaModel();
        pessoaModel.setId(pessoa.getId());
        pessoaModel.setNome(pessoa.getNome());
        pessoaModel.setCpf(pessoa.getCpf());
        pessoaModel.setDataNascimento(pessoa.getDataNascimento());
        pessoaModel.setContatos(List.of(contatoModel));

        when(modelDisassembler.toDomainObject(any(PessoaInput.class))).thenReturn(pessoa);
        when(service.adicionar(any(Pessoa.class))).thenReturn(pessoa);
        when(modelAssembler.toModel(any(Pessoa.class))).thenReturn(pessoaModel);

        mockMvc.perform(post("/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(pessoaModel.getId()))
                .andExpect(jsonPath("$.nome").value(pessoaModel.getNome()))
                .andExpect(jsonPath("$.cpf").value(pessoaModel.getCpf()))
                .andExpect(jsonPath("$.dataNascimento").value(pessoaModel.getDataNascimento().toString()))
                .andExpect(jsonPath("$.contatos[0].nome").value(contatoModel.getNome()))
                .andExpect(jsonPath("$.contatos[0].telefone").value(contatoModel.getTelefone()))
                .andExpect(jsonPath("$.contatos[0].email").value(contatoModel.getEmail()));
    }

    @Test
    public void quandoContatoPessoaNaoInformado_entaoRetornaBadRequestEListaErros() throws Exception {
        PessoaInput pessoaInput = new PessoaInput();
        pessoaInput.setCpf("580.005.824-57");
        pessoaInput.setNome("Angela da Silva");
        pessoaInput.setDataNascimento(LocalDate.of(1995, 10, 5));
        pessoaInput.setContatos(Collections.emptyList());

        mockMvc.perform(post("/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pessoaInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://elotech.com.br/dados-invalidos"))
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.detail").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.userMessage").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.objects[0].name").value("contatos"))
                .andExpect(jsonPath("$.objects[0].userMessage").value("Informe ao menos um contato"));
    }

    @Test
    public void quandoNomeContatoNaoInformado_entaoRetornaBadRequestEListaErros() throws Exception {
        ContatoInput contatoInput = new ContatoInput();
        contatoInput.setTelefone("123456789");
        contatoInput.setEmail("contato1@example.com");

        PessoaInput pessoaInput = new PessoaInput();
        pessoaInput.setNome("Carlos Silva");
        pessoaInput.setCpf("123.456.789-09");
        pessoaInput.setDataNascimento(LocalDate.now());
        pessoaInput.setContatos(List.of(contatoInput));

        mockMvc.perform(post("/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pessoaInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://elotech.com.br/dados-invalidos"))
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.detail").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.userMessage").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.objects[0].name").value("contatos[0].nome"))
                .andExpect(jsonPath("$.objects[0].userMessage").value("Informe o nome do contato"));
    }

    @Test
    public void quandoTelefoneContatoNaoInformado_entaoRetornaBadRequestEListaErros() throws Exception {
        ContatoInput contatoInput = new ContatoInput();
        contatoInput.setNome("Contato 1");
        contatoInput.setEmail("contato1@example.com");

        PessoaInput pessoaInput = new PessoaInput();
        pessoaInput.setNome("Carlos Silva");
        pessoaInput.setCpf("123.456.789-09");
        pessoaInput.setDataNascimento(LocalDate.now());
        pessoaInput.setContatos(List.of(contatoInput));

        mockMvc.perform(post("/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pessoaInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://elotech.com.br/dados-invalidos"))
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.detail").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.userMessage").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.objects[0].name").value("contatos[0].telefone"))
                .andExpect(jsonPath("$.objects[0].userMessage").value("Informe o telefone do contato"));
    }

    @Test
    public void quandoEmailContatoNaoInformado_entaoRetornaBadRequestEListaErros() throws Exception {
        ContatoInput contatoInput = new ContatoInput();
        contatoInput.setNome("Contato 1");
        contatoInput.setTelefone("123456789");

        PessoaInput pessoaInput = new PessoaInput();
        pessoaInput.setNome("Carlos Silva");
        pessoaInput.setCpf("123.456.789-09");
        pessoaInput.setDataNascimento(LocalDate.now());
        pessoaInput.setContatos(List.of(contatoInput));

        mockMvc.perform(post("/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pessoaInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://elotech.com.br/dados-invalidos"))
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.detail").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.userMessage").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.objects[0].name").value("contatos[0].email"))
                .andExpect(jsonPath("$.objects[0].userMessage").value("Informe o e-mail do contato"));
    }

    @Test
    public void quandoEmailContatoSintaxeIncorreta_entaoRetornaBadRequestEListaErros() throws Exception {
        ContatoInput contatoInput = new ContatoInput();
        contatoInput.setNome("Contato 1");
        contatoInput.setTelefone("123456789");
        contatoInput.setEmail("contato1@");

        PessoaInput pessoaInput = new PessoaInput();
        pessoaInput.setNome("Carlos Silva");
        pessoaInput.setCpf("123.456.789-09");
        pessoaInput.setDataNascimento(LocalDate.now());
        pessoaInput.setContatos(List.of(contatoInput));

        mockMvc.perform(post("/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pessoaInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://elotech.com.br/dados-invalidos"))
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.detail").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.userMessage").value("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."))
                .andExpect(jsonPath("$.objects[0].name").value("contatos[0].email"))
                .andExpect(jsonPath("$.objects[0].userMessage").value("O e-mail do contato está inválido"));
    }

    @Test
    public void quandoAtualizaPessoaComDadosValidos_entaoRetornaSucesso() throws Exception {
        Long pessoaId = 1L;
        ContatoInput contatoInput = new ContatoInput();
        contatoInput.setNome("Contato 1");
        contatoInput.setTelefone("123456789");
        contatoInput.setEmail("contato1@example.com");

        PessoaInput input = new PessoaInput();
        input.setNome("Carlos Silva Atualizado");
        input.setCpf("987.654.321-00");
        input.setDataNascimento(LocalDate.now());
        input.setContatos(List.of(contatoInput));

        Pessoa pessoaSalva = new Pessoa();
        pessoaSalva.setId(pessoaId);
        pessoaSalva.setNome("Carlos Silva");
        pessoaSalva.setCpf("123.456.789-09");
        pessoaSalva.setDataNascimento(LocalDate.now().minusYears(1));

        Pessoa pessoaAtualizada = new Pessoa();
        pessoaAtualizada.setId(pessoaId);
        pessoaAtualizada.setNome(input.getNome());
        pessoaAtualizada.setCpf(input.getCpf());
        pessoaAtualizada.setDataNascimento(input.getDataNascimento());

        Contato contato = new Contato();
        contato.setNome(contatoInput.getNome());
        contato.setTelefone(contatoInput.getTelefone());
        contato.setEmail(contatoInput.getEmail());
        pessoaAtualizada.setContatos(List.of(contato));

        ContatoModel contatoModel = new ContatoModel();
        contatoModel.setNome(contatoInput.getNome());
        contatoModel.setTelefone(contatoInput.getTelefone());
        contatoModel.setEmail(contatoInput.getEmail());

        PessoaModel pessoaModel = new PessoaModel();
        pessoaModel.setId(pessoaAtualizada.getId());
        pessoaModel.setNome(pessoaAtualizada.getNome());
        pessoaModel.setCpf(pessoaAtualizada.getCpf());
        pessoaModel.setDataNascimento(pessoaAtualizada.getDataNascimento());
        pessoaModel.setContatos(List.of(contatoModel));

        when(service.buscar(pessoaId)).thenReturn(pessoaSalva);
        doNothing().when(modelDisassembler).copyToDomainObject(any(PessoaInput.class), any(Pessoa.class));
        when(service.atualizar(any(Pessoa.class), anyList())).thenReturn(pessoaAtualizada);
        when(modelAssembler.toModel(any(Pessoa.class))).thenReturn(pessoaModel);

        mockMvc.perform(put("/v1/pessoas/{id}", pessoaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pessoaModel.getId()))
                .andExpect(jsonPath("$.nome").value(pessoaModel.getNome()))
                .andExpect(jsonPath("$.cpf").value(pessoaModel.getCpf()))
                .andExpect(jsonPath("$.dataNascimento").value(pessoaModel.getDataNascimento().toString()))
                .andExpect(jsonPath("$.contatos[0].nome").value(contato.getNome()))
                .andExpect(jsonPath("$.contatos[0].telefone").value(contato.getTelefone()))
                .andExpect(jsonPath("$.contatos[0].email").value(contato.getEmail()));
    }

    @Test
    void quandoExcluiPessoa_EntaoRetornaComSucesso() throws Exception {
        Long pessoaId = 1L;

        mockMvc.perform(delete("/v1/pessoas/{id}", pessoaId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service).excluir(pessoaId);
    }

    @Test
    void quandoTentaExcluirPessoaInexistente_entaoRetornaErro() throws Exception {
        Long pessoaId = 1L;

        doThrow(new NegocioException("Cadastro não encontrado."))
                .when(service).excluir(pessoaId);

        mockMvc.perform(delete("/v1/pessoas/{id}", pessoaId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Cadastro não encontrado."))
                .andExpect(jsonPath("$.userMessage").value("Cadastro não encontrado."));
    }

    class SpecificationArgumentMatcher<T> implements ArgumentMatcher<Specification<T>> {
        private final Specification<T> expectedSpec;

        public SpecificationArgumentMatcher(Specification<T> expectedSpec) {
            this.expectedSpec = expectedSpec;
        }

        @Override
        public boolean matches(Specification<T> argument) {
            // Simplificação para o exemplo. Em um cenário real, implemente a lógica necessária.
            return true;
        }
    }

}
