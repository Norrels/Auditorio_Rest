package br.com.senai.sp.auditorio.rest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonObjectSerializer;
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

import com.fasterxml.jackson.core.JsonParser;

import br.com.senai.sp.auditorio.annotation.Administrador;
import br.com.senai.sp.auditorio.annotation.Professor;
import br.com.senai.sp.auditorio.annotation.Suporte;
import br.com.senai.sp.auditorio.model.Erro;
import br.com.senai.sp.auditorio.model.Evento;
import br.com.senai.sp.auditorio.model.Solicitacao;
import br.com.senai.sp.auditorio.model.Usuario;
import br.com.senai.sp.auditorio.repository.EventoRepository;
import br.com.senai.sp.auditorio.repository.SolicitacaoRepository;
import br.com.senai.sp.auditorio.repository.UsuarioRepository;

@CrossOrigin
@RestController
@RequestMapping("api/solic")
public class SolicitacaoRestController {

	@Autowired
	private SolicitacaoRepository repSolicitacao;

	@Autowired
	private EventoRepository repEvento;

	@Autowired
	private UsuarioRepository repUsuario;
	

	@RequestMapping(value = "", method = RequestMethod.GET)
	public Iterable<Solicitacao> getSolicitacao() {
		return repSolicitacao.findAll();
	}
	

	@RequestMapping(value = "/user/{id}/page/{page}", method = RequestMethod.GET)
	public ResponseEntity<Object> getSolicitacaoById(@PathVariable("id") Long id, @PathVariable("page") int page) {
		PageRequest pageable = PageRequest.of(page - 1, 7, Sort.by(Sort.Direction.ASC, "id"));
		Page<Solicitacao> pagina = repSolicitacao.findByIdUsuarioall(id, pageable);
		return ResponseEntity.ok(pagina);
	}
	

	@RequestMapping(value = "semId/{id}", method = RequestMethod.GET)
	public Iterable<Solicitacao> getSolicitacaoSemCertoId(@PathVariable("id") Long id) {
		return repSolicitacao.buscarSemCertoId(id);
	}
	
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<Object> getEventoById(@PathVariable("id") Long idSolicitacao) {
		Solicitacao s = repSolicitacao.findById(idSolicitacao).get();
		if (s.getId() > 0) {
			return new ResponseEntity<Object>(s, HttpStatus.OK);
		} else {
			Erro error = new Erro(HttpStatus.NOT_FOUND, "Não foi possivel encontrar o Evento", "NOT_FOUND");
			return new ResponseEntity<Object>(error, HttpStatus.NOT_FOUND);
		}
	}
	

	@RequestMapping(value = "page/{id}", method = RequestMethod.GET)
	public ResponseEntity<Object> getElementPages(@PathVariable("id") int page) {
		PageRequest pageable = PageRequest.of(page - 1, 7, Sort.by(Sort.Direction.ASC, "id"));
		Page<Solicitacao> pagina = repSolicitacao.findAll(pageable);
		return ResponseEntity.ok(pagina);
	}
	

	@RequestMapping(value = "/autocomplete", method = RequestMethod.GET)
  	public Iterable<Solicitacao> autoComplete() {
		return repSolicitacao.autoComplete();
	}
	
	@RequestMapping(value = "/autocomplete/{id}", method = RequestMethod.GET)
	public Iterable<Solicitacao> autoCompleteByUser(@PathVariable("id") Long id) {
		return repSolicitacao.autoCompleteByUser(id);
	}
	
	@RequestMapping(value = "/buscar/palavra/{palavra-chave}/{page}", method = RequestMethod.GET)
	public ResponseEntity<Object> buscar(@PathVariable("palavra-chave") String palavra,
			@PathVariable("page") int page) {
		PageRequest pageable = PageRequest.of(page - 1, 7, Sort.by(Sort.Direction.ASC, "id"));
		Page<Solicitacao> pagina = repSolicitacao.buscarPorText(palavra, pageable);
		return ResponseEntity.ok(pagina);
	}
	
	@RequestMapping(value = "/buscar/palavra/{palavra-chave}/user/{user}/page/{page}", method = RequestMethod.GET)
	public ResponseEntity<Object> buscarByProf(@PathVariable("palavra-chave") String palavra,
			@PathVariable("page") int page, @PathVariable("user") Long id) {
		PageRequest pageable = PageRequest.of(page - 1, 7, Sort.by(Sort.Direction.ASC, "id"));
		Page<Solicitacao> pagina = repSolicitacao.buscarPorTextByUser(palavra, pageable, id);
		return ResponseEntity.ok(pagina);
	}
	
	
	@RequestMapping(value = "/aprovar/{id}", method = RequestMethod.POST)
	public ResponseEntity<Object> setAprovado(@PathVariable("id") Long id) {
		Solicitacao solicitacao = repSolicitacao.findById(id).get();
		 new Thread() { 
			 public void run() { 
			 executePostAprovarReprovar(repUsuario.findById(solicitacao.getUsuario().getId()).get(), solicitacao, "Aprovada");	 
		 }; }.start();
		
		boolean four = false;
		Solicitacao solic = repSolicitacao.findById(id).get();
		for (Evento eve : repEvento.findByStart(solic.getStart())) {
			// Deleta os outros eventos, ao salvar um evento com todo periodo
			if (solic.getPeriodo().equals("4")) {
				try {
					System.out.println("Passou aquii");
					repEvento.deleteById(eve.getId());
					four = true;
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(e);
					Erro error = new Erro(HttpStatus.INTERNAL_SERVER_ERROR,
							"Contate o Suporte, não foi possivel deletar", e.getClass().getName());
					return new ResponseEntity<Object>(error, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}

		}
		if (four) {
			Evento e = new Evento();
			try {
				e.setSolicitacao(solic.getTitle(), solic.getPeriodo(), solic.getStart(), solic.getDescription(),
						solic.getColor(), solic.getUsuario(), solic);
				repEvento.save(e);
				solic.setStatus("1");
				repSolicitacao.save(solic);
				return ResponseEntity.ok(e);
			} catch (Exception erro) {
				System.out.println(erro);
				Erro error = new Erro(HttpStatus.INTERNAL_SERVER_ERROR,
						"Contate o Suporte, não foi possivel salvar o evento", e.getClass().getName());
				return new ResponseEntity<Object>(error, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		if (solic.getStatus().equals("2")) {
			Evento e = new Evento();
			try {
				e.setSolicitacao(solic.getTitle(), solic.getPeriodo(), solic.getStart(), solic.getDescription(),
						solic.getColor(), solic.getUsuario(), solic);
				repEvento.save(e);

				solic.setStatus("1");
				repSolicitacao.save(solic);
				return ResponseEntity.ok(e);
			} catch (Exception ex) {
				ex.printStackTrace();
				Erro error = new Erro(HttpStatus.INTERNAL_SERVER_ERROR, "Contate o Suporte, não foi possivel deletar",
						e.getClass().getName());
				return new ResponseEntity<Object>(error, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity<Object>(HttpStatus.IM_USED);
		}
	}
	
	@RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> criarEvento(@RequestBody Solicitacao sol) {
		Usuario user = repUsuario.findById(sol.getUsuario().getId()).get();
		
		 new Thread() { 
			 public void run() { 
					for (String u  : repUsuario.buscaEmailAdmOther()) {
						 executePost(user, sol, u); 
					}
							 
		 }; }.start();
		 

		int cont = 0;
		boolean tlz = false;
		boolean four = false;
		boolean ent = false;
		System.out.println("Passou pelo metodo post");
		try {
			for (Solicitacao qtd : repSolicitacao.findByStart(sol.getStart())) {
				// Deleta os outros eventos, ao salvar um evento com todo periodo
				if (sol.getPeriodo().equals("4") && !sol.getStatus().equals("0") && !qtd.getPeriodo().equals("4")) {
					try {

						System.out.println("Passou aquii" + "" + sol.getPeriodo());
						repSolicitacao.deleteById(qtd.getId());
						four = true;
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println(e);
						Erro error = new Erro(HttpStatus.INTERNAL_SERVER_ERROR,
								"Contate o Suporte, não foi possivel deletar", e.getClass().getName());
						return new ResponseEntity<Object>(error, HttpStatus.INTERNAL_SERVER_ERROR);
					}

				}

				if (qtd.getPeriodo().equals("4")) {
					ent = true;
				}
				cont++;
			}
			for (Evento qtd : repEvento.findByStart(sol.getStart())) {
				if (qtd.getPeriodo().equals(sol.getPeriodo()) || qtd.getPeriodo().equals("4")) {
					return new ResponseEntity<Object>(HttpStatus.IM_USED);
				}
			}

			if (four) {
				try {
					repSolicitacao.save(sol);
					return ResponseEntity.ok().build();
				} catch (Exception e) {
					System.out.println(e);
					Erro error = new Erro(HttpStatus.INTERNAL_SERVER_ERROR,
							"Contate o Suporte, não foi possivel salvar o evento", e.getClass().getName());
					return new ResponseEntity<Object>(error, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
			if (ent) {
				return new ResponseEntity<Object>(HttpStatus.IM_USED);
			}
			// Valida se não há mais de 2 eventos no dia, e caso não houver ele insere no
			// banco
			if (cont <= 2) {
				for (Solicitacao periodo : repSolicitacao.findByStartAndPeriodo(sol.getStart(), sol.getPeriodo())) {
					if (periodo.getPeriodo().equals(sol.getPeriodo())) {
						System.out.println("Entrou no primeiro if");
						tlz = true;
						break;
					} else {
						tlz = false;
						break;
					}
				}
				if (tlz) {
					System.out.println("Erro 226");
					return new ResponseEntity<Object>(HttpStatus.IM_USED);
				} else {
					try {
						System.out.println("Salvou no banco");
						repSolicitacao.save(sol);
						return ResponseEntity.ok().build();
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println(e);
						Erro error = new Erro(HttpStatus.INTERNAL_SERVER_ERROR, "Contate o Suporte",
								e.getClass().getName());
						return new ResponseEntity<Object>(error, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
			} else {
				return new ResponseEntity<Object>(HttpStatus.CONFLICT);
			}
		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			Erro erro = new Erro(HttpStatus.INTERNAL_SERVER_ERROR, "Registro Duplicado", e.getClass().getName());
			return new ResponseEntity<Object>(erro, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			Erro erro = new Erro(HttpStatus.INTERNAL_SERVER_ERROR, "Contante o suporte", e.getClass().getName());
			return new ResponseEntity<Object>(erro, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> atualizarEvento(@RequestBody Solicitacao sol, @PathVariable("id") Long id) {
		Solicitacao solDB = repSolicitacao.findById(id).get();
		boolean four = false;
		boolean ent = false;

		if (sol.getId() > 0) {
			System.err.println("Tem id !" + sol.getId());
			if (solDB.getStart().equals(sol.getStart())) {
				System.out.println("Entoru no if do teste fazer oq");
				int cont = 0;
				int iguais = 0;
				System.out.println("Passou aqui ! 1");

				try {
					for (Solicitacao qtd : repSolicitacao.findByStart(sol.getStart())) {
						if (sol.getPeriodo().equals("4") && !qtd.getPeriodo().equals("4")) {
							try {
								repSolicitacao.deleteById(qtd.getId());
								four = true;
							} catch (Exception error) {
								error.printStackTrace();
								System.out.println(error);
								Erro erro = new Erro(HttpStatus.INTERNAL_SERVER_ERROR,
										"Contate o Suporte, não foi possivel deletar", error.getClass().getName());
								return new ResponseEntity<Object>(error, HttpStatus.INTERNAL_SERVER_ERROR);
							}

						}
						cont++;
					}

					if (four) {

						try {
							repSolicitacao.save(sol);
							return ResponseEntity.ok().build();
						} catch (Exception error) {
							System.out.println(error);
							Erro erro = new Erro(HttpStatus.INTERNAL_SERVER_ERROR,
									"Contate o Suporte, não foi possivel salvar o evento", error.getClass().getName());
							return new ResponseEntity<Object>(error, HttpStatus.INTERNAL_SERVER_ERROR);
						}
					}

					if (cont <= 2) {
						for (Solicitacao periodo : repSolicitacao.findByStartAndPeriodo(sol.getStart(),
								sol.getPeriodo())) {
							System.out.println(periodo.getId() + " ID e ID do front : " + sol.getId());
							if (periodo.getPeriodo().equals(sol.getPeriodo()) && periodo.getId().equals(sol.getId())) {
								iguais = 0;
								break;
							}
							if (periodo.getId() == sol.getId() && periodo.getPeriodo().equals(sol.getPeriodo())
									&& periodo.getTitle().equals(sol.getTitle())
									&& periodo.getDescription().equals(sol.getDescription())) {
								iguais++;
								System.out.println("Passou " + iguais);
								break;
							} else if (!(periodo.getId() == sol.getId())) {
								if (periodo.getPeriodo().equals(sol.getPeriodo())) {
									iguais++;
									System.out.println("Passou " + iguais);
									break;
								}
							}
						}

						if (iguais == 1) {
							Erro error = new Erro(HttpStatus.CONFLICT, "Número Excedido", "MAXIMUM");
							return new ResponseEntity<Object>(error, HttpStatus.CONFLICT);
						} else {
							try {
								repSolicitacao.save(sol);
								return ResponseEntity.ok().build();
							} catch (Exception e2) {
								e2.printStackTrace();
								Erro error = new Erro(HttpStatus.INTERNAL_SERVER_ERROR, "Contate o Suporte",
										e2.getClass().getName());
								return new ResponseEntity<Object>(error, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}

					} else {
						Erro error = new Erro(HttpStatus.CONFLICT, "Número Excedido", "MAXIMUM");
						return new ResponseEntity<Object>(error, HttpStatus.CONFLICT);
					}
				} catch (DataIntegrityViolationException e2) {
					e2.printStackTrace();
					Erro erro = new Erro(HttpStatus.INTERNAL_SERVER_ERROR, "Registro Duplicado",
							e2.getClass().getName());
					return new ResponseEntity<Object>(erro, HttpStatus.INTERNAL_SERVER_ERROR);
				} catch (Exception e2) {
					e2.printStackTrace();
					Erro erro = new Erro(HttpStatus.INTERNAL_SERVER_ERROR, "Contante o suporte",
							e2.getClass().getName());
					return new ResponseEntity<Object>(erro, HttpStatus.INTERNAL_SERVER_ERROR);
				}

			} else {

				System.out.println("Passou aqui teste 18:26");

				int cont = 0;
				boolean tlz = false;
				try {
					for (Solicitacao qtd : repSolicitacao.findByStart(sol.getStart())) {
						System.err.println("!1111");
						/*
						 * if(!(e.getTitle() == qtd.getTitle() || e.getDescription() ==
						 * qtd.getTitle())){ try { repository.save(e); return
						 * ResponseEntity.ok().build(); } catch (Exception error) {
						 * error.printStackTrace(); Erro err = new
						 * Erro(HttpStatus.INTERNAL_SERVER_ERROR, "Contate o Suporte",
						 * error.getClass().getName()); return new ResponseEntity<Object>(err,
						 * HttpStatus.INTERNAL_SERVER_ERROR); } }
						 */
						if (sol.getPeriodo().equals("4") && !qtd.getPeriodo().equals("4")) {
							try {
								repSolicitacao.deleteById(qtd.getId());
								four = true;
							} catch (Exception error) {
								error.printStackTrace();
								System.out.println(error);
								Erro erro = new Erro(HttpStatus.INTERNAL_SERVER_ERROR,
										"Contate o Suporte, não foi possivel deletar", error.getClass().getName());
								return new ResponseEntity<Object>(error, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						if (!qtd.getPeriodo().equals(sol.getPeriodo()) && qtd.getPeriodo().equals("4")) {
							return new ResponseEntity<Object>(HttpStatus.IM_USED);
						}
						if (qtd.getPeriodo().equals(sol.getPeriodo())) {
							return new ResponseEntity<Object>(HttpStatus.IM_USED);
						}
						cont++;
						System.err.println(cont);
					}
					for (Evento qtd : repEvento.findByStart(sol.getStart())) {
						if (qtd.getPeriodo().equals(sol.getPeriodo()) || qtd.getPeriodo().equals("4")) {
							return new ResponseEntity<Object>(HttpStatus.IM_USED);
						}
					}
					if (four) {
						try {
							repSolicitacao.save(sol);
							return ResponseEntity.ok().build();
						} catch (Exception error) {
							System.out.println(error);
							Erro erro = new Erro(HttpStatus.INTERNAL_SERVER_ERROR,
									"Contate o Suporte, não foi possivel salvar o evento", error.getClass().getName());
							return new ResponseEntity<Object>(error, HttpStatus.INTERNAL_SERVER_ERROR);
						}
					}
					if (cont <= 2) {
						for (Solicitacao periodo : repSolicitacao.findByStartAndPeriodo(sol.getStart(),
								sol.getPeriodo())) {
							if (periodo.getPeriodo().equals(sol.getPeriodo()) && sol.getPeriodo().equals("4")) {
								tlz = true;
								break;
							} else {
								tlz = false;
								break;
							}
						}
						if (tlz) {
							return new ResponseEntity<Object>(HttpStatus.IM_USED);
						} else {
							try {
								System.err.println(" PASSOU NO SALVAR ");
								repSolicitacao.save(sol);
								System.err.println(" PASSOU DEPOIS DO SALVAR ");
								return ResponseEntity.ok().build();
							} catch (Exception e2) {
								e2.printStackTrace();
								Erro error = new Erro(HttpStatus.INTERNAL_SERVER_ERROR, "Contate o Suporte",
										e2.getClass().getName());
								return new ResponseEntity<Object>(error, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
					} else {
						Erro error = new Erro(HttpStatus.CONFLICT, "Número Excedido", "MAXIMUM");
						return new ResponseEntity<Object>(error, HttpStatus.CONFLICT);
					}
				} catch (DataIntegrityViolationException e2) {
					e2.printStackTrace();
					Erro erro = new Erro(HttpStatus.INTERNAL_SERVER_ERROR, "Registro Duplicado",
							e2.getClass().getName());
					return new ResponseEntity<Object>(erro, HttpStatus.INTERNAL_SERVER_ERROR);
				} catch (Exception e2) {
					e2.printStackTrace();
					Erro erro = new Erro(HttpStatus.INTERNAL_SERVER_ERROR, "Contante o suporte",
							e2.getClass().getName());
					return new ResponseEntity<Object>(erro, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		} else {
			Erro error = new Erro(HttpStatus.NOT_FOUND, "Não foi possivel encontrar o Evento", "NOT_FOUND");
			return new ResponseEntity<Object>(error, HttpStatus.NOT_FOUND);
		}
	}
	
	@RequestMapping(value = "buscar/{id}", method = RequestMethod.GET)
	public List<Solicitacao> buscaSolics(@PathVariable Long id) {
		return repSolicitacao.findByIdUsuario(id);
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Object> deletaTarefa(@PathVariable Long id) {
		Solicitacao s = repSolicitacao.findById(id).get();
		if (s.getId() > 0) {
			try {
				repSolicitacao.deleteById(id);
				return ResponseEntity.ok().build();
			} catch (Exception e2) {
				e2.printStackTrace();
				Erro error = new Erro(HttpStatus.INTERNAL_SERVER_ERROR, "Contate o Suporte", e2.getClass().getName());
				return new ResponseEntity<Object>(error, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			Erro error = new Erro(HttpStatus.NOT_FOUND, "Não foi possivel encontrar o Evento", "NOT_FOUND");
			return new ResponseEntity<Object>(error, HttpStatus.NOT_FOUND);
		}
	}
	
	@RequestMapping(value = "reprovar/{id}", method = RequestMethod.PUT)
	public ResponseEntity<Object> reprovarSolic(@PathVariable Long id) {
		
		Solicitacao solic = repSolicitacao.findById(id).get();
		 new Thread() { 
			 public void run() { 
			 executePostAprovarReprovar(repUsuario.findById(solic.getUsuario().getId()).get(), solic, "Negada");	 
		 }; }.start();
		
		solic.setStatus("0");
		repSolicitacao.save(solic);
		return ResponseEntity.ok(solic);
	}
	
	@RequestMapping(value = "buscar/{id}/page/{page}", method = RequestMethod.GET)
	public ResponseEntity<Object> buscaSolicPagina(@PathVariable Long id, @PathVariable int page) {
		PageRequest pageable = PageRequest.of(page - 1, 7, Sort.by(Sort.Direction.ASC, "id"));
		Page<Solicitacao> pagina = repSolicitacao.findByIdUsuarioPage(id, pageable);
		return ResponseEntity.ok(pagina);
	}
	
	@RequestMapping(value = "status/{status}", method = RequestMethod.GET)
	public List<Solicitacao> buscarPorStatus(@PathVariable String status) {
		return repSolicitacao.findByStatus(status);
	}

	public static String executePost(Usuario user, Solicitacao sol, String email) {
		HttpURLConnection connection = null;
		String[] a_ = sol.getStart().split("-");
		String ano;
		String mes;
		String dia;
		ano = (a_[0]);
		mes = (a_[1]);
		dia = (a_[2]);
		String data = dia + "/" + mes + "/" + ano;
		
		try {
			
			// Create connection
			URL url = new URL("https://emailapi-production.up.railway.app/novaSolicitacao");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			String jsonInputString = "{\"solicitante\":\"" + user.getNome()	+ "\",\"email\":[\"" + email + "\"],\"data\":\"" + data + "\"}";
			System.out.println(jsonInputString);
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Content-Language", "en-US");
			connection.setUseCaches(false);
			connection.setDoOutput(true);

			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = jsonInputString.getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	public static String executePostAprovarReprovar(Usuario user, Solicitacao sol, String situacao) {
		
		HttpURLConnection connection = null;
		String[] a_ = sol.getStart().split("-");
		String ano;
		String mes;
		String dia;
		ano = (a_[0]);
		mes = (a_[1]);
		dia = (a_[2]);
		String mes_string = dia + "/" + mes + "/" + ano;
		
		try {
			// Create connection
			URL url = new URL("https://emailapi-production.up.railway.app/situacaoSolic");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			String jsonInputString = "{\"nome\":\"" + user.getNome() + "\",\"email\":\"" + user.getEmail() + "\",\"solic\":{\"nome\":\"" + sol.getTitle() + "\", \"situacao\":\"" + situacao + "\", \"data\":\"" + mes_string + "\"}}";
			System.out.println(jsonInputString);
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Content-Language", "en-US");
			connection.setUseCaches(false);
			connection.setDoOutput(true);

			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = jsonInputString.getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
}
