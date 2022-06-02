package br.com.senai.sp.auditorio.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import br.com.senai.sp.auditorio.util.HashUtil;
import lombok.Data;

@Entity
public class Usuario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String nome;
	@Column(unique = true)
	private String email;
	@JsonProperty(access = Access.WRITE_ONLY)
	private String senha;
	private TipoDeUsuario tipoUsuario;
	@Column(unique = true)
	private String matricula;
	private String dataNascimento;
	private boolean ativo;

	public void setUsuario (String nome, String email, TipoDeUsuario tipoUsuario, String matricula, String dataNascimento,
			boolean ativo) {
		this.nome = nome;
		this.email = email;
		this.tipoUsuario = tipoUsuario;
		this.matricula = matricula;
		this.dataNascimento = dataNascimento;
		this.ativo = ativo;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public TipoDeUsuario getTipoUsuario() {
		return tipoUsuario;
	}

	public void setTipoUsuario(TipoDeUsuario tipoUsuario) {
		this.tipoUsuario = tipoUsuario;
	}

	public String getMatricula() {
		return matricula;
	}

	public void setMatricula(String matricula) {
		this.matricula = matricula;
	}

	public String getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(String dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = HashUtil.hash(senha);
	}

}
