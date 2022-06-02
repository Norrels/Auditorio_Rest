package br.com.senai.sp.auditorio.rest;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.senai.sp.auditorio.model.Usuario;
import br.com.senai.sp.auditorio.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/sup/private")
public class SuporteRestController {
	
	@Autowired
	private UsuarioRepository usuarioRep;
	
	@RequestMapping(value = "", method = RequestMethod.GET)
	public Iterable<Usuario> getUsuario() {
		return usuarioRep.findAll();
	}
	
	@RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> criarUsuario(@RequestBody Usuario sup) {
		Usuario s = usuarioRep.save(sup);
		return ResponseEntity.created(URI.create("api/sup/private/" + s.getId())).body(s);
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> atualizarUsuario(@RequestBody Usuario s, @PathVariable("id") Long id){
		usuarioRep.save(s);
		return ResponseEntity.ok().build();
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<Usuario> getUsuarioById(@PathVariable("id") Long id){
		Optional<Usuario> opt = usuarioRep.findById(id);
		if(opt.isPresent()) {
			return ResponseEntity.ok(opt.get());
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Object> deletaUsuarioById(@PathVariable Long id) {
		usuarioRep.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
