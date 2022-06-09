package br.com.senai.sp.auditorio.rest;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import br.com.senai.sp.auditorio.annotation.Administrador;
import br.com.senai.sp.auditorio.annotation.Suporte;
import br.com.senai.sp.auditorio.model.TipoDeUsuario;
import br.com.senai.sp.auditorio.model.TokenJWT;
import br.com.senai.sp.auditorio.model.Usuario;
import br.com.senai.sp.auditorio.repository.UsuarioRepository;

@CrossOrigin
@RestController
@RequestMapping("api/usuarios")
public class UsuarioRestController {

	public static final String EMISSOR = "Senai";
	public static final String SECRET = "F1Lh@sD3J32u2";

	@Autowired
	private UsuarioRepository repository;

	@RequestMapping(value = "", method = RequestMethod.GET)
	public Iterable<Usuario> getEvento() {
		return repository.findAll();
	}
	
	@RequestMapping(value = "/autocomplete", method = RequestMethod.GET)
	public Iterable<Usuario> autoComplete (String palavra) {
		return repository.autoComplete();
	}
	@Suporte
	@RequestMapping(value = "/autocompleteAdm", method = RequestMethod.GET)
	public Iterable<Usuario> autoCompleteAdm (String palavra) {
		return repository.autoCompleteAdm();
	}

	@RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Usuario> criarUsuario(@RequestBody Usuario usuario) {
		try {
			repository.save(usuario);
			return ResponseEntity.ok(usuario);
		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			return new ResponseEntity<Usuario>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Usuario>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/buscar/{palavra-chave}/{pagina}", method = RequestMethod.GET)
	public ResponseEntity<Object> buscar(@PathVariable("palavra-chave") String palavra, @PathVariable("pagina") int page) {
		PageRequest pageable = PageRequest.of(page - 1, 7, Sort.by(Sort.Direction.ASC, "id"));
		Page<Usuario> pagina = repository.buscarPorText(palavra, pageable);
		return ResponseEntity.ok(pagina);
	}
	
	@RequestMapping(value = "/buscarAdm/{palavra-chave}/{pagina}", method = RequestMethod.GET)
	public ResponseEntity<Object> buscarAdm(@PathVariable("palavra-chave") String palavra, @PathVariable("pagina") int page) {
		PageRequest pageable = PageRequest.of(page - 1, 7, Sort.by(Sort.Direction.ASC, "id"));
		Page<Usuario> pagina = repository.buscarPorTextAdm(palavra, pageable);
		return ResponseEntity.ok(pagina);
	}


	@RequestMapping(value = "page/{page}", method = RequestMethod.GET)
	public ResponseEntity<Object> getElementPages(@PathVariable("page") int page) {
		PageRequest pageable = PageRequest.of(page - 1, 7, Sort.by(Sort.Direction.ASC, "id"));
		Page<Usuario> pagina = repository.findByTipoUsuario(pageable, TipoDeUsuario.PROFESSOR);
		return ResponseEntity.ok(pagina);
	}
	
	@RequestMapping(value = "pageAdms/{page}", method = RequestMethod.GET)
	public ResponseEntity<Object> getAdminstrador (@PathVariable("page") int page) {
		PageRequest pageable = PageRequest.of(page - 1, 7, Sort.by(Sort.Direction.ASC, "id"));
		Page<Usuario> pagina = repository.findByTipoUsuario(pageable, TipoDeUsuario.ADMINISTRADOR);
		return ResponseEntity.ok(pagina);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public ResponseEntity<Void> deleteUser(@PathVariable("id") Long idUsuario) {
		try {
		System.out.println(idUsuario);
		Usuario user = repository.findById(idUsuario).get();
		user.setAtivo(false);
		repository.save(user);
		return ResponseEntity.noContent().build();
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<Usuario> getUsuarioById (@PathVariable("id") Long idUsuario) {
		// tenta buscar a skatepark
		Optional<Usuario> optional = repository.findById(idUsuario);
		// se o restaurante existir
		if (optional.isPresent()) {
			return ResponseEntity.ok(optional.get());
		} else {
			return ResponseEntity.notFound().build();
		}

	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> atualizarUsuario(@RequestBody Usuario usuario, @PathVariable("id") Long id) {
		Usuario usuario1 = repository.findById(usuario.getId()).get();
		
		System.out.println("Usuario ID : " + usuario.getId());
		System.out.println(usuario.getSenha());
		// validação do id
		if (id != usuario.getId()) {
			throw new RuntimeException("ID Inválido");
		}
		if (usuario.getSenha().equals(usuario1.getSenha())) {
			return new ResponseEntity<Object>(HttpStatus.CONFLICT);
		} else {
			try {
				System.out.println("Passou aqui");
					
				System.out.println(usuario1.getNome());
				
				usuario.setUsuario(usuario1.getNome(), usuario1.getEmail(), usuario1.getTipoUsuario(), usuario1.getMatricula(), usuario1.getDataNascimento(), usuario1.isAtivo());
				
				usuario.getSenha();
				repository.save(usuario);
				System.out.println("Passou após o salvar");
				return ResponseEntity.ok(usuario);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
				return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TokenJWT> login(@RequestBody Usuario usuario) {
		// buscar o usuario no banco de dados
		usuario = repository.findByEmailAndSenhaAndAtivo(usuario.getEmail(), usuario.getSenha(), true);
		// verifica se o usuário não é nulo
		if (usuario != null) {
			// variável para inserir dados no payload
			Map<String, Object> payload = new HashMap<String, Object>();
			payload.put("id", usuario.getId());
			payload.put("name", usuario.getNome());
			payload.put("email", usuario.getEmail());
			payload.put("TipoUser", usuario.getTipoUsuario().toString());
			// variável para data de expiração
			Calendar expiracao = Calendar.getInstance();
			// adiciona o tempo para a expiração
			expiracao.add(Calendar.HOUR, 1);
			// algoritmo para assinar o token
			Algorithm algoritmo = Algorithm.HMAC256(SECRET);
			// cria o token
			TokenJWT tokenJwt = new TokenJWT();
			// gera o token
			tokenJwt.setToken(JWT.create().withPayload(payload).withIssuer(EMISSOR).withExpiresAt(expiracao.getTime())
					.sign(algoritmo));
			return ResponseEntity.ok(tokenJwt);
		} else {
			return new ResponseEntity<TokenJWT>(HttpStatus.UNAUTHORIZED);
		}
	}
	
	@RequestMapping(value = "/acessar", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Usuario> primeiroAcesso(@RequestBody Usuario usuario) {
		// Retorna um usuario já cadastrado
		Usuario usuario1 = repository.findByMatriculaAndAtivo(usuario.getMatricula(), true);
		// Verifica se o usuário existe
		Usuario usuarioDB  = repository.findByMatriculaAndDataNascimento(usuario.getMatricula(), usuario.getDataNascimento());
		
		// Verifica se a matricula existe no banco
		// se ela existe ele faz outra verificação
		if (usuario != null) {
			System.out.println("caiuu aqui");
			// Se a matricula existe, ele verifica se a conta já foi cadastrada
			if (usuario1 != null) {
				return new ResponseEntity<Usuario>(HttpStatus.CONFLICT);
			}else if (repository.findByMatricula(usuario.getMatricula()) == null) {
				return new ResponseEntity<Usuario>(HttpStatus.UNPROCESSABLE_ENTITY);
				
				// buscar retorna mais que 4 valores !
			}else if (repository.findByDataNascimentoAndId(usuario.getDataNascimento(), usuarioDB.getId()) == null) {
				return new ResponseEntity<Usuario>(HttpStatus.BAD_REQUEST);
			} else {
			
				System.out.println(usuario);
				return ResponseEntity.ok(usuarioDB);
			}
		} else 
				return new ResponseEntity<Usuario>(HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@RequestMapping(value = "/cadastrar", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Usuario> cadastrar(@RequestBody Usuario usuario){

		if(repository.findByEmail(usuario.getEmail()) != null) {
			return new ResponseEntity<Usuario>(HttpStatus.UNPROCESSABLE_ENTITY);
		} else if(repository.findByMatricula(usuario.getMatricula()) != null) {
			return new ResponseEntity<Usuario>(HttpStatus.CONFLICT);
		} 
		
		if(repository.findByMatricula(usuario.getMatricula()) == null || repository.findByEmail(usuario.getEmail()) == null) {
			try {
				repository.save(usuario);
				return new ResponseEntity<Usuario>(HttpStatus.CREATED);
			} catch (Exception e) {
				return new ResponseEntity<Usuario>(HttpStatus.BAD_REQUEST);
			}
		}
		return new ResponseEntity<Usuario>(HttpStatus.BAD_REQUEST);
	} 
	
	
	@RequestMapping(value = "/email/adms", method = RequestMethod.GET)
	public Iterable<Usuario> getAdmEmail() {
		return repository.buscaEmailAdm();
	}

}
