package br.com.senai.sp.auditorio.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import br.com.senai.sp.auditorio.model.Evento;


public interface EventoRepository extends PagingAndSortingRepository<Evento, Long>{

	@Query("SELECT e FROM Evento e WHERE start = :s ")
	public List<Evento> findByStart (@Param("s") String start);
	
	@Query("SELECT e FROM Evento e WHERE start = :s AND periodo = :p ")
	public List<Evento> findByStartAndPeriodo (@Param("s") String start,@Param("p") String periodo);
	
	@Query("SELECT e FROM Evento e WHERE periodo = :s")
	public List<Evento> findByPeriodo (@Param("s") String start);
	
	@Query("SELECT e FROM Evento e WHERE e.title LIKE %:t% or e.periodo LIKE %:t% or e.start LIKE %:t% or e.description LIKE %:t% or e.usuario.nome LIKE %:t%")
	public Page<Evento> buscarPorText (@Param("t") String palavra, Pageable page);
	
	@Query("SELECT e.title, e.start, usuario.nome FROM Evento e")
	public Iterable<Evento> autoComplete ();
}
