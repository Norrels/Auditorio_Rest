package br.com.senai.sp.auditorio.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
@RequestMapping("/api/tarefas")
public class EventoRestController {

	@Autowired
	private EventoRepository repository;
	
	@Autowired
	private SolicitacaoRepository repositorySolic;
	
	@Autowired
	private UsuarioRepository repositoryUsuario;
	
	@RequestMapping(value = "", method = RequestMethod.GET)
	public Iterable<Evento> getEvento() {
		String[] a_ = null;
		int ano = 0;
		int mes = 0;
		int dia = 0;
		for (Solicitacao soliq : repositorySolic.findAll()) {
				a_ = soliq.getStart().split("-");
				ano = Integer.parseInt(a_[0]);
				mes = Integer.parseInt(a_[1]);
				dia = Integer.parseInt(a_[2]);
				
				if(dia > 1) {
					dia -= 1;
				}
				LocalDate dataDb = LocalDate.of(ano, mes, dia);
				LocalDate dataAtual = LocalDate.now();
				
				if(dataAtual.isAfter(dataDb)) {
					System.out.println("é antes mano cancela rapa dataAgora = " + dataAtual + " data do db = " + dataDb);
					soliq.setStatus("0");
					repositorySolic.save(soliq);
				}			
		}
		return repository.findAll();
	}

	@RequestMapping(value = "page/{id}", method = RequestMethod.GET)
	public ResponseEntity<Object> getElementPages(@PathVariable("id") int page) {
		PageRequest pageable = PageRequest.of(page - 1, 7, Sort.by(Sort.Direction.ASC, "id"));
		Page<Evento> pagina = repository.findAll(pageable);
		return ResponseEntity.ok(pagina);
	}

	@RequestMapping(value = "/buscar/{palavra-chave}/{pagina}", method = RequestMethod.GET)
	public  ResponseEntity<Object> buscar(@PathVariable("palavra-chave") String palavra, @PathVariable("pagina") int page) {
		PageRequest pageable = PageRequest.of(page - 1, 7, Sort.by(Sort.Direction.ASC, "id"));
		Page<Evento> pagina = repository.buscarPorText(palavra, pageable);
		return ResponseEntity.ok(pagina);
	}
	
	@RequestMapping(value = "/autocomplete", method = RequestMethod.GET)
	public Iterable<Evento> autoComplete (String palavra) {
		return repository.autoComplete();
	}
	
	@RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> criarEvento(@RequestBody Evento evento) {
		int cont = 0;
		boolean tlz = false;
		boolean four = false;
		boolean ent = false;
		System.out.println("Passou pelo metodo post");
		try {
			for (Evento qtd : repository.findByStart(evento.getStart())) {
				// Deleta os outros eventos, ao salvar um evento com todo periodo
				if (evento.getPeriodo().equals("4")) {
					try {
						System.out.println("Passou aquii");
						repository.deleteById(qtd.getId());
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
			
			for(Solicitacao soliq : repositorySolic.findByStart(evento.getStart())) {
				if(soliq.getPeriodo().equals(evento.getPeriodo())) {
					System.out.println("Entrou aqui 22222222222222222222222");
					return new ResponseEntity<Object>(HttpStatus.IM_USED);
				}
				
			}
			
			
			if (four) {
				try {
					repository.save(evento);
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
				for (Evento periodo : repository.findByStartAndPeriodo(evento.getStart(), evento.getPeriodo())) {
					if (periodo.getPeriodo().equals(evento.getPeriodo())) {
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
						repository.save(evento);
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
	
public static String executeEditarSolic(Solicitacao s, Usuario user, Evento e, String situacao) {
		
		HttpURLConnection connection = null;
		String[] a_ = s.getStart().split("-");
		String ano;
		String mes;
		String dia;
		ano = (a_[0]);
		mes = (a_[1]);
		dia = (a_[2]);
		String mes_string = dia + "/" + mes + "/" + ano;
		
		try {
			// Create connection
			URL url = new URL("https://emailapi-production.up.railway.app/mundacaEvento");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			String jsonInputString = 
					"{\"nome\":"+ "\"" + user.getNome() 
					+ "\",\"email\":\"" + user.getEmail() + 
					"\", \"evento\":\"" + e.getTitle() + 
					"\", \"situacao\":\"" + situacao + 
					"\", \"dia\":\"" + mes_string + "\"}";
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
		} catch (Exception er) {
			er.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> atualizarEvento(@RequestBody Evento e, @PathVariable("id") Long id) {
		Evento evento = repository.findById(id).get();
		boolean four = false;
		boolean ent = false;
		
		if (evento.getId() > 0) {
			if (evento.getStart().equals(e.getStart())) {
				System.out.println("Entoru no if do teste fazer oq");
				int cont = 0;
				int iguais = 0;
				System.out.println("Passou aqui ! 1");
												
				try {
					for (Evento qtd : repository.findByStart(e.getStart())) {

						if (e.getPeriodo().equals("4")) {
							try {
								repository.deleteById(qtd.getId());
								four = true;
							} catch (Exception error) {
								error.printStackTrace();
								System.out.println(e);
								Erro erro = new Erro(HttpStatus.INTERNAL_SERVER_ERROR,
										"Contate o Suporte, não foi possivel deletar", e.getClass().getName());
								return new ResponseEntity<Object>(error, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						
						cont++;
						
					}
					
					for(Solicitacao soliq : repositorySolic.buscarSolicAndamentos(e.getStart())) {
						if(soliq.getPeriodo().equals(e.getPeriodo())) {
							System.out.println("Entrou aqui 22222222222222222222222");
							return new ResponseEntity<Object>(HttpStatus.IM_USED);
						}
						
					}

					if (four) {
						repository.save(e);
						try {
							return ResponseEntity.ok().build();
						} catch (Exception error) {
							System.out.println(error);
							Erro erro = new Erro(HttpStatus.INTERNAL_SERVER_ERROR,
									"Contate o Suporte, não foi possivel salvar o evento", e.getClass().getName());
							return new ResponseEntity<Object>(error, HttpStatus.INTERNAL_SERVER_ERROR);
						}
					}

					if (cont <= 2) {
						for (Evento periodo : repository.findByStartAndPeriodo(e.getStart(), e.getPeriodo())) {
							System.out.println(periodo.getId() + " ID e ID do front : " + e.getId());
							if (periodo.getPeriodo().equals(e.getPeriodo()) && periodo.getId().equals(e.getId())) {
								System.out.println("Iguais = 0");
								iguais = 0;
								break;
							}
							if (periodo.getId() == e.getId() && periodo.getPeriodo().equals(e.getPeriodo()) && periodo.getTitle().equals(e.getTitle()) && periodo.getDescription().equals(e.getDescription())) {
								iguais++;
								System.out.println("Passou " + iguais);
								break;
							} else if (!(periodo.getId() == e.getId())) {
								if(periodo.getPeriodo().equals(e.getPeriodo())) {
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
								if(evento.getSolicitacao().getId() != null) {
									
									Solicitacao s = repositorySolic.findById(evento.getSolicitacao().getId()).get();
									
									e.setUsuario(s.getUsuario());
									e.setSolicitacao(evento.getSolicitacao());
									
									new Thread() { 
										 public void run() {
											 ;
										 executeEditarSolic(s ,repositoryUsuario.findById(e.getUsuario().getId()).get(), evento, "editado");	 
									 }; }.start();
									 
									repository.save(e);
									s.setSolicitacao(e.getTitle(), e.getPeriodo(), e.getStart(), e.getDescription(), e.getColor(), s.getStatus());
									repositorySolic.save(s);
									
									return ResponseEntity.ok().build();
								}
								repository.save(e);
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
					for (Evento qtd : repository.findByStart(e.getStart())) {
						System.out.println("!1111");
						
//						if(!(e.getTitle() == qtd.getTitle() || e.getDescription() == qtd.getTitle())){
//							try {
//								repository.save(e);
//								return ResponseEntity.ok().build();
//							} catch (Exception error) {
//								error.printStackTrace();
//								Erro err = new Erro(HttpStatus.INTERNAL_SERVER_ERROR, "Contate o Suporte",
//										error.getClass().getName());
//								return new ResponseEntity<Object>(err, HttpStatus.INTERNAL_SERVER_ERROR);
//							}
//						}
						
						if (e.getPeriodo().equals("4")) {
							try {
								repository.deleteById(qtd.getId());
								four = true;
							} catch (Exception error) {
								error.printStackTrace();
								System.out.println(e);
								Erro erro = new Erro(HttpStatus.INTERNAL_SERVER_ERROR,
										"Contate o Suporte, não foi possivel deletar", e.getClass().getName());
								return new ResponseEntity<Object>(error, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						if (!qtd.getPeriodo().equals(e.getPeriodo()) && qtd.getPeriodo().equals("4")) {
							return new ResponseEntity<Object>(HttpStatus.IM_USED);
						}
						if (qtd.getPeriodo().equals(e.getPeriodo())) {
							return new ResponseEntity<Object>(HttpStatus.IM_USED);
						}
						cont++;
					}
					if (four) {
						repository.save(e);
						try {
							return ResponseEntity.ok().build();
						} catch (Exception error) {
							System.out.println(error);
							Erro erro = new Erro(HttpStatus.INTERNAL_SERVER_ERROR,
									"Contate o Suporte, não foi possivel salvar o evento", e.getClass().getName());
							return new ResponseEntity<Object>(error, HttpStatus.INTERNAL_SERVER_ERROR);
						}
					}
					if (cont <= 2) {
						for (Evento periodo : repository.findByStartAndPeriodo(e.getStart(), e.getPeriodo())) {
							if (periodo.getPeriodo().equals(e.getPeriodo()) && e.getPeriodo().equals("4")) {
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
							
							if(evento.getSolicitacao().getId() != null) {
								
								Solicitacao s = repositorySolic.findById(evento.getSolicitacao().getId()).get();
								
								e.setUsuario(s.getUsuario());
								e.setSolicitacao(evento.getSolicitacao());
								
								new Thread() { 
									 public void run() {
										 ;
									 executeEditarSolic(s, repositoryUsuario.findById(e.getUsuario().getId()).get(), evento, "editado");	 
								 }; }.start();
								 
								repository.save(e);
								s.setSolicitacao(e.getTitle(), e.getPeriodo(), e.getStart(), e.getDescription(), e.getColor(), s.getStatus());
								repositorySolic.save(s);
								
								return ResponseEntity.ok().build();
							}
							/* if(evento.getSolicitacao().getId() != null) {
								
								Solicitacao s = repositorySolic.findById(evento.getSolicitacao().getId()).get();
								
								e.setUsuario(s.getUsuario());
								e.setSolicitacao(evento.getSolicitacao());
								
								new Thread() { 
									 public void run() {
										 ;
									 executeEditarSolic(repositoryUsuario.findById(e.getUsuario().getId()).get(), s, "Editada");	 
								 }; }.start();
								
								repository.save(e);
								
								return ResponseEntity.ok().build();
							} */
							try {
								System.err.println(" PASSOU NO SALVAR ");
								repository.save(e);
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
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<Object> getEventoById(@PathVariable("id") Long idEvento) {
		Evento e = repository.findById(idEvento).get();
		if (e.getId() > 0) {
			return new ResponseEntity<Object>(e, HttpStatus.OK);
		} else {
			Erro error = new Erro(HttpStatus.NOT_FOUND, "Não foi possivel encontrar o Evento", "NOT_FOUND");
			return new ResponseEntity<Object>(error, HttpStatus.NOT_FOUND);
		}
	}

public static String executeDeletarSolic(Usuario user, Evento e, String situacao) {
		
		HttpURLConnection connection = null;
		String[] a_ = e.getStart().split("-");
		String ano;
		String mes;
		String dia;
		ano = (a_[0]);
		mes = (a_[1]);
		dia = (a_[2]);
		String mes_string = dia + "/" + mes + "/" + ano;
		
		try {
			// Create connection
			URL url = new URL("https://emailapi-production.up.railway.app/mundacaEvento");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			String jsonInputString = 
					"{\"nome\":"+ "\"" + user.getNome() 
					+ "\",\"email\":\"" + user.getEmail() + 
					"\", \"evento\":\"" + e.getTitle() + 
					"\", \"situacao\":\"" + situacao + 
					"\", \"dia\":\"" + mes_string + "\"}";
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
		} catch (Exception er) {
			er.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Object> deletaTarefa(@PathVariable Long id) {
		Evento e = repository.findById(id).get();
		if(e.getSolicitacao().getId() != null) {
			new Thread() { 
				 public void run() {
					 executeDeletarSolic(repositoryUsuario.findById(e.getUsuario().getId()).get(), e, "excluido");	 
			 }; }.start();
		}
		if (e.getId() > 0) {
			try {
				repository.deleteById(id);
				repositorySolic.deleteById(e.getSolicitacao().getId());
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
}
