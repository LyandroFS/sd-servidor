package entidade;

import java.io.Serializable;

public class Carteira implements Serializable{
	private static final long serialVersionUID = 1L;
	private int numeroCarteira;
	private String nome;
	private Double saldo;
	
	public Carteira() {
		super();
	}
	
	public Carteira(int numeroCarteira, String nome, Double saldo) {
		super();
		this.numeroCarteira = numeroCarteira;
		this.nome = nome;
		this.saldo = saldo;
	}

	public int getNumeroCarteira() {
		return numeroCarteira;
	}

	public void setNumeroCarteira(int numeroCarteira) {
		this.numeroCarteira = numeroCarteira;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Double getSaldo() {
		return saldo;
	}

	public void setSaldo(Double saldo) {
		this.saldo = saldo;
	}	
}