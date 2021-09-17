package br.com.cod3r.cm.modelo;

import java.util.ArrayList;
import java.util.List;

public class Campo {

	private final int linha;
	private final int coluna;

	private boolean aberto = false;
	private boolean minado = false;
	private boolean marcado = false;

	private List<Campo> vizinhos = new ArrayList<>();
	private List<CampoObservador> observadores = new ArrayList<>();
	// private List<BiConsumer<Campo, CampoEvento>> observadores = new ArrayList<>();

	Campo(int linha, int coluna) {
		this.linha = linha;
		this.coluna = coluna;
	}
	
	public void registrarObservador(CampoObservador observador) {
		observadores.add(observador);
	}
	
	private void notificarObservadores(CampoEvento evento) {
		observadores.stream()
		.forEach(o -> o.eventoOcorreu(this, evento));
	}

	// metodo que vai ditar a regra de se pode ser vizinho ou nao do outro
	boolean adicionarVizinho(Campo vizinho) {
		boolean linhaDiferente = linha != vizinho.linha;
		boolean colunaDiferente = coluna != vizinho.coluna;
		boolean diagonal = linhaDiferente && colunaDiferente;

		int deltaLinha = Math.abs(linha - vizinho.linha);
		int deltaColuna = Math.abs(coluna - vizinho.coluna);
		int deltaGeral = deltaColuna + deltaLinha;

		if (deltaGeral == 1 && !diagonal) {
			vizinhos.add(vizinho);
			return true;
		} else if (deltaGeral == 2 && diagonal) {
			vizinhos.add(vizinho);
			return true;
		} else {
			return false;
		}
	}

	public void alternarMarcacao() {
		// se fechado, marcar
		if (!aberto) {
			marcado = !marcado;
			
			if(marcado) {
				notificarObservadores(CampoEvento.MARCAR);
			} else {
				notificarObservadores(CampoEvento.DESMARCAR);
			}
		}
	}

	public boolean abrir() {

		// abre se, fechado e nao marcado
		if (!aberto && !marcado) {
			aberto = true;

			// explode caso minado
			if (minado) {
				notificarObservadores(CampoEvento.EXPLODIR);
				return true;
			}
			
			setAberto(true);
						
			// laco pra ir abrindo ate onde eh seguro
			if (vizinhancaSegura()) {
				vizinhos.forEach(v -> v.abrir());
			}

			// retorna verdadeiro, o campo realmente foi aberto nessa execucao
			return true;
		} else {

			return false;
		}
	}

	// abrir vizinhos que nao tem bomba perto
	public boolean vizinhancaSegura() {
		return vizinhos.stream().noneMatch(v -> v.minado);
	}

	void minar() {
		minado = true;
	}
	
	public boolean isMinado() {
		return minado;
	}

	public boolean isMarcado() {
		return marcado;
	}

	void setAberto(boolean aberto) {
		this.aberto = aberto;
		
		if(aberto) {
			notificarObservadores(CampoEvento.ABRIR);
		}
	}

	public boolean isAberto() {
		return aberto;
	}

	public boolean isFechado() {
		return !isAberto();
	}

	public int getLinha() {
		return linha;
	}

	public int getColuna() {
		return coluna;
	}

	// se o objetivo foi alcancado (sem mina e aberto, seguros)
	// ou marcar aonde tem bomba
	boolean objetivoAlcancado() {
		boolean desvendado = !minado && aberto;
		boolean protegido = minado && marcado;
		return desvendado || protegido;
	}

	public int minasNaVizinhanca() {
		return (int) vizinhos.stream().filter(v -> v.minado).count();
	}

	void reiniciarJogo() {
		aberto = false;
		minado = false;
		marcado = false;
		notificarObservadores(CampoEvento.REINICIAR);
	}
}
