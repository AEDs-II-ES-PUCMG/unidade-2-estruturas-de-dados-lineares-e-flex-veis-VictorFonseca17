import java.util.ArrayList;
import java.util.NoSuchElementException;

public class Pilha<E> {

	private Celula<E> topo;
	private Celula<E> fundo;

	public Pilha() {

		Celula<E> sentinela = new Celula<E>();
		fundo = sentinela;
		topo = sentinela;

	}

	public boolean vazia() {
		return fundo == topo;
	}

	public void empilhar(E item) {

		topo = new Celula<E>(item, topo);
	}

	public E desempilhar() {

		E desempilhado = consultarTopo();
		topo = topo.getProximo();
		return desempilhado;

	}

	public E consultarTopo() {

		if (vazia()) {
			throw new NoSuchElementException("Nao há nenhum item na pilha!");
		}

		return topo.getItem();

	}

	/**
	 * Cria e devolve uma nova pilha contendo os primeiros numItens elementos
	 * do topo da pilha atual.
	 * 
	 * Os elementos são mantidos na mesma ordem em que estavam na pilha original.
	 * Caso a pilha atual possua menos elementos do que o valor especificado,
	 * uma exceção será lançada.
	 *
	 * @param numItens o número de itens a serem copiados da pilha original.
	 * @return uma nova instância de Pilha<E> contendo os numItens primeiros elementos.
	 * @throws IllegalArgumentException se a pilha não contém numItens elementos.
	 */
	public Pilha<E> subPilha(int numItens) {

		if (numItens < 0) {
			throw new IllegalArgumentException("numItens não pode ser negativo.");
		}
		if (numItens == 0) {
			return new Pilha<E>();
		}

		ArrayList<E> ordemDoTopoParaBaixo = new ArrayList<>();
		Pilha<E> aux = new Pilha<E>();

		while (!vazia()) {
			E e = desempilhar();
			ordemDoTopoParaBaixo.add(e);
			aux.empilhar(e);
		}
		while (!aux.vazia()) {
			empilhar(aux.desempilhar());
		}

		int n = ordemDoTopoParaBaixo.size();
		if (numItens > n) {
			throw new IllegalArgumentException(
					String.format("A pilha contém apenas %d elemento(s), mas foram solicitados %d.", n, numItens));
		}

		Pilha<E> resultado = new Pilha<E>();
		for (int i = numItens - 1; i >= 0; i--) {
			resultado.empilhar(ordemDoTopoParaBaixo.get(i));
		}
		return resultado;
	}
}
