package br.com.senai.sp.auditorio.model;

public enum TipoDeUsuario {
	PROFESSOR("professor"), ADMINISTRADOR("adm"), SUPORTE("suporte");

	String tipo;

	private TipoDeUsuario(String tipo) {
		this.tipo = tipo;
	}

	@Override
	public String toString() {
		return this.tipo;
	}
}
