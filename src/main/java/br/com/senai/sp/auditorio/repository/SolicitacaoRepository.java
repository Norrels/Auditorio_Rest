package br.com.senai.sp.auditorio.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import br.com.senai.sp.auditorio.model.Evento;
import br.com.senai.sp.auditorio.model.Solicitacao;


public interface SolicitacaoRepository extends PagingAndSortingRepository<Solicitacao, Long>{
	@Query("SELECT e FROM Solicitacao e WHERE start = :s ")
	public List<Solicitacao> findByStart (@Param("s") String start);
	
	@Query("SELECT e FROM Solicitacao e WHERE start = :s AND periodo = :p ")
	public List<Solicitacao> findByStartAndPeriodo (@Param("s") String start,@Param("p") String periodo);
	
	@Query("SELECT e FROM Solicitacao e WHERE periodo = :s")
	public List<Solicitacao> findByPeriodo (@Param("s") String start);
	
	@Query("SELECT e FROM Solicitacao e WHERE usuario.id = :s AND status = 2")
	public List<Solicitacao> findByIdUsuario (@Param("s") Long id);
	
	@Query("SELECT e FROM Solicitacao e WHERE usuario.id = :s AND status = 2")
	public Page<Solicitacao> findByIdUsuarioPage (@Param("s") Long id, Pageable page);
	
	@Query("SELECT e FROM Solicitacao e WHERE e.title LIKE %:t% or e.periodo LIKE %:t% or e.start LIKE %:t% or e.description LIKE %:t% or e.usuario.nome LIKE %:t%")
	public Page<Solicitacao> buscarPorText (@Param("t") String palavra, Pageable page);
	
	@Query("SELECT e.title, e.start, usuario FROM Solicitacao e")
	public Iterable<Solicitacao> autoComplete ();
	
	@Query("SELECT s FROM Solicitacao s WHERE usuario.id != :s AND status = 2")
	public List<Solicitacao> buscarSemCertoId (@Param("s") Long id);
	
	
}
