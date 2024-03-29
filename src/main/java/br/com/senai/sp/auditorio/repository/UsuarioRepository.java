package br.com.senai.sp.auditorio.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import br.com.senai.sp.auditorio.model.TipoDeUsuario;
import br.com.senai.sp.auditorio.model.Usuario;

public interface UsuarioRepository extends PagingAndSortingRepository<Usuario, Long> {
	
	public Usuario findByEmailAndSenhaAndAtivo(String email, String senha, boolean ativo);
	
	public Usuario findByMatriculaAndAtivo(String matricula, boolean ativo);
	
	public Usuario findByMatriculaAndDataNascimento(String matricula, String data);
	
	public Page<Usuario> findByTipoUsuario(Pageable page, TipoDeUsuario tipo);
	

	@Query("SELECT u FROM Usuario u WHERE (u.tipoUsuario = 0) AND (u.nome LIKE %:t% or u.matricula LIKE %:t% or u.email LIKE %:t%)")
	public Page<Usuario> buscarPorText (@Param("t") String usuario, Pageable page);
	
	@Query("SELECT u FROM Usuario u WHERE (u.tipoUsuario = 1) AND (u.nome LIKE %:t% or u.matricula LIKE %:t% or u.email LIKE %:t%)")
	public Page<Usuario> buscarPorTextAdm (@Param("t") String usuario, Pageable page);
	
	@Query("SELECT u.nome, u.email, u.matricula FROM Usuario u WHERE u.tipoUsuario = 0")
	public Iterable<Usuario> autoComplete ();
	
	@Query("SELECT u.nome, u.email, u.matricula FROM Usuario u WHERE u.tipoUsuario = 1")
	public Iterable<Usuario> autoCompleteAdm ();
	
	public Usuario findByMatricula(String Matricula);
	
	public Usuario findByDataNascimentoAndId(String Data, Long id);
	
	@Query("Select u.email from Usuario u where u.tipoUsuario = 1 and u.ativo = true")
	public List<String> buscaEmailAdmOther();
	
	public Usuario findByEmail(String email);
	


}
