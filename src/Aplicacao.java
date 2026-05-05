import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.lang.reflect.InvocationTargetException;

/**
 * Aplicação principal do sistema de comércio (AEDs II).
 */
public class Aplicacao {

	/** Edite com o seu primeiro e segundo nome (teste da fila, tarefa 1). */
	private static final String PRIMEIRO_NOME = "Victor";
	private static final String SEGUNDO_NOME = "Fonseca";

	/** Nome do arquivo de produtos (raiz do projeto, relativo ao diretório de execução). */
	private static String nomeArquivoDados;

	/** Nome do arquivo de pedidos persistidos. */
	private static final String NOME_ARQUIVO_PEDIDOS = "pedidos.txt";
	/** Formato antigo (pilha): primeiro pedido gravado = mais recente. */
	private static final String CABECALHO_ARQUIVO_PEDIDOS_V1 = "PEDIDOS_V1";
	/** Formato atual (fila): primeiro pedido gravado = mais antigo (frente da fila). */
	private static final String CABECALHO_ARQUIVO_PEDIDOS_V2 = "PEDIDOS_V2";

	/** Scanner para leitura de dados do teclado */
	private static Scanner teclado;

	/** Vetor de produtos cadastrados */
	private static Produto[] produtosCadastrados;

	/** Quantidade de produtos cadastrados atualmente no vetor */
	private static int quantosProdutos = 0;

	/** Fila de pedidos finalizados aguardando processamento (fim = mais recente). */
	private static Fila<Pedido> filaPedidos = new Fila<>();

	/** Pilha de produtos dos pedidos finalizados (topo = mais recentemente pedido). */
	private static Pilha<Produto> pilhaProdutosRecentes = new Pilha<>();

	/** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
	private static void pausa() {
		System.out.println("Digite enter para continuar...");
		teclado.nextLine();
	}

	/** Cabeçalho principal da CLI do sistema */
	private static void cabecalho() {
		System.out.println("AEDs II COMÉRCIO DE COISINHAS");
		System.out.println("=============================");
	}

	private static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {

		T valor;

		System.out.println(mensagem);
		try {
			valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			return null;
		}
		return valor;
	}

	/**
	 * Imprime o menu principal, lê a opção do usuário e a retorna (int).
	 * 
	 * @return Um inteiro com a opção do usuário.
	 */
	private static int menu() {
		cabecalho();
		System.out.println("1 - Listar todos os produtos");
		System.out.println("2 - Procurar por um produto, por código");
		System.out.println("3 - Procurar por um produto, por nome");
		System.out.println("4 - Iniciar novo pedido");
		System.out.println("5 - Fechar pedido");
		System.out.println("6 - Listar produtos dos pedidos mais recentes");
		System.out.println("0 - Sair");
		System.out.print("Digite sua opção: ");
		return Integer.parseInt(teclado.nextLine());
	}

	/**
	 * Lê os dados de um arquivo-texto e retorna um vetor de produtos. Arquivo-texto no formato
	 * N (quantidade de produtos) <br/>
	 * tipo;descrição;preçoDeCusto;margemDeLucro;[dataDeValidade] <br/>
	 * Deve haver uma linha para cada um dos produtos. Retorna um vetor vazio em caso de problemas com o arquivo.
	 * 
	 * @param nomeArquivoDados Nome do arquivo de dados a ser aberto.
	 * @return Um vetor com os produtos carregados, ou vazio em caso de problemas de leitura.
	 */
	private static Produto[] lerProdutos(String nomeArquivoDados) {

		Scanner arquivo = null;
		int numProdutos;
		String linha;
		Produto produto;
		Produto[] vetorProdutos;

		try {
			arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));

			numProdutos = Integer.parseInt(arquivo.nextLine());
			vetorProdutos = new Produto[numProdutos];

			for (int i = 0; i < numProdutos; i++) {
				linha = arquivo.nextLine();
				produto = Produto.criarDoTexto(linha);
				vetorProdutos[i] = produto;
			}
			quantosProdutos = numProdutos;

		} catch (IOException | RuntimeException excecaoArquivo) {
			vetorProdutos = new Produto[0];
			quantosProdutos = 0;
		} finally {
			if (arquivo != null) {
				arquivo.close();
			}
		}

		return vetorProdutos;
	}

	/**
	 * Localiza um produto no vetor de produtos cadastrados, a partir do código de produto informado pelo usuário, e o retorna.
	 * Em caso de não encontrar o produto, retorna null
	 */
	private static Produto localizarProduto() {

		Produto produto = null;
		Boolean localizado = false;

		cabecalho();
		System.out.println("Localizando um produto...");
		Integer idProduto = lerOpcao("Digite o código identificador do produto desejado: ", Integer.class);
		if (idProduto == null) {
			return null;
		}
		for (int i = 0; (i < quantosProdutos && !localizado); i++) {
			if (produtosCadastrados[i].hashCode() == idProduto.intValue()) {
				produto = produtosCadastrados[i];
				localizado = true;
			}
		}

		return produto;
	}

	/**
	 * Localiza um produto no vetor de produtos cadastrados, a partir do nome de produto informado pelo usuário, e o retorna.
	 * A busca não é sensível ao caso. Em caso de não encontrar o produto, retorna null
	 * 
	 * @return O produto encontrado ou null, caso o produto não tenha sido localizado no vetor de produtos cadastrados.
	 */
	private static Produto localizarProdutoDescricao() {

		Produto produto = null;
		Boolean localizado = false;
		String descricao;

		cabecalho();
		System.out.println("Localizando um produto...");
		System.out.println("Digite o nome ou a descrição do produto desejado:");
		descricao = teclado.nextLine();
		for (int i = 0; (i < quantosProdutos && !localizado); i++) {
			if (produtosCadastrados[i].descricao.equals(descricao)) {
				produto = produtosCadastrados[i];
				localizado = true;
			}
		}

		return produto;
	}

	private static Produto localizarProdutoPorCodigo(int codigoProduto) {
		for (int i = 0; i < quantosProdutos; i++) {
			if (produtosCadastrados[i].hashCode() == codigoProduto) {
				return produtosCadastrados[i];
			}
		}
		return null;
	}

	private static void mostrarProduto(Produto produto) {

		cabecalho();
		String mensagem = "Dados inválidos para o produto!";

		if (produto != null) {
			mensagem = String.format("Dados do produto:\n%s", produto);
		}

		System.out.println(mensagem);
	}

	/** Lista todos os produtos cadastrados, numerados, um por linha */
	private static void listarTodosOsProdutos() {

		cabecalho();
		System.out.println("\nPRODUTOS CADASTRADOS:");
		for (int i = 0; i < quantosProdutos; i++) {
			System.out.println(String.format("%02d - %s", (i + 1), produtosCadastrados[i].toString()));
		}
	}

	/**
	 * Inicia um novo pedido.
	 * Permite ao usuário escolher e incluir produtos no pedido.
	 * 
	 * @return O novo pedido
	 */
	public static Pedido iniciarPedido() {

		Integer formaPagamento = lerOpcao(
				"Digite a forma de pagamento do pedido, sendo 1 para pagamento à vista e 2 para pagamento a prazo",
				Integer.class);
		if (formaPagamento == null) {
			return null;
		}
		Pedido pedido = new Pedido(LocalDate.now(), formaPagamento.intValue());
		Produto produto;
		Integer numProdutos;
		Integer quantidade;

		listarTodosOsProdutos();
		System.out.println("Incluindo produtos no pedido...");
		numProdutos = lerOpcao("Quantos produtos serão incluídos no pedido?", Integer.class);
		if (numProdutos == null || numProdutos.intValue() < 1) {
			return pedido;
		}
		for (int i = 0; i < numProdutos.intValue(); i++) {
			produto = localizarProdutoDescricao();
			if (produto == null) {
				System.out.println("Produto não encontrado");
				i--;
			} else {
				quantidade = lerOpcao("Quantos itens desse produto serão incluídos no pedido?", Integer.class);
				if (quantidade != null) {
					pedido.incluirProduto(produto, quantidade.intValue());
				}
			}
		}

		return pedido;
	}

	/**
	 * Finaliza um pedido: enfileira o pedido e registra os produtos vendidos na pilha de recentes.
	 * 
	 * @param pedido O pedido que deve ser finalizado.
	 * @return null após finalizar (pedido em memória não deve ser reutilizado).
	 */
	public static Pedido finalizarPedido(Pedido pedido) {

		if (pedido == null) {
			System.out.println("Não há pedido em andamento para finalizar.");
			return null;
		}
		if (pedido.getQuantidadeDeItens() == 0) {
			System.out.println("O pedido não contém itens. Inclua produtos antes de fechar.");
			return pedido;
		}

		filaPedidos.enfileirar(pedido);
		ItemDePedido[] itens = pedido.getItensDoPedido();
		for (int i = 0; i < pedido.getQuantidadeDeItens(); i++) {
			pilhaProdutosRecentes.empilhar(itens[i].getProduto());
		}

		System.out.println("Pedido finalizado com sucesso.\n");
		System.out.println(pedido);
		return null;
	}

	public static void listarProdutosPedidosRecentes() {

		cabecalho();
		if (pilhaProdutosRecentes.vazia()) {
			System.out.println("Ainda não há produtos de pedidos finalizados.");
			return;
		}

		Integer k = lerOpcao("Informe K (quantos produtos mais recentes deseja listar): ", Integer.class);
		if (k == null || k.intValue() < 1) {
			System.out.println("Valor inválido para K.");
			return;
		}

		try {
			Pilha<Produto> maisRecentes = pilhaProdutosRecentes.subPilha(k.intValue());
			System.out.println("\n--- " + k + " produto(s) mais recente(s) (do mais recente ao mais antigo nesta lista) ---\n");
			while (!maisRecentes.vazia()) {
				Produto p = maisRecentes.desempilhar();
				System.out.println(p);
				System.out.println();
			}
		} catch (IllegalArgumentException ex) {
			System.out.println(ex.getMessage());
		}
	}

	/**
	 * Tarefa 1: {@link Fila} de caracteres com primeiro e segundo nome, contagem de ocorrências,
	 * e demonstração de enfileirar / desenfileirar.
	 */
	private static void executarTestesPreliminaresFila() {

		System.out.println("========== Testes preliminares (fila / nome) ==========");
		Fila<Character> fila = new Fila<>();

		for (int i = 0; i < PRIMEIRO_NOME.length(); i++) {
			fila.enfileirar(PRIMEIRO_NOME.charAt(i));
		}
		for (int i = 0; i < SEGUNDO_NOME.length(); i++) {
			fila.enfileirar(SEGUNDO_NOME.charAt(i));
		}

		System.out.println("Primeiro nome: " + PRIMEIRO_NOME + " | Segundo nome: " + SEGUNDO_NOME);
		System.out.println("Ocorrências de 'o': " + fila.contarOcorrencias('o'));
		System.out.println("Ocorrências de 'i': " + fila.contarOcorrencias('i'));
		System.out.println("Ocorrências de 'z': " + fila.contarOcorrencias('z'));

		System.out.println("Desenfileirando 3 caracteres (FIFO):");
		for (int i = 0; i < 3; i++) {
			System.out.println("  -> " + fila.desenfileirar());
		}

		System.out.println("Após 3 desenfileiramentos, ocorrências de 'o': " + fila.contarOcorrencias('o'));

		fila.enfileirar('!');
		fila.enfileirar('?');
		System.out.println("Após enfileirar '!' e '?', ocorrências de '!': " + fila.contarOcorrencias('!'));

		System.out.println("Conteúdo restante da fila (impressão frente → fim):");
		fila.imprimir();
		System.out.println("======================================================\n");
	}

	private static void gravarPedidos(String nomeArquivo) {

		ArrayList<Pedido> ordemAntigoParaRecente = new ArrayList<>();
		while (!filaPedidos.vazia()) {
			ordemAntigoParaRecente.add(filaPedidos.desenfileirar());
		}
		for (Pedido p : ordemAntigoParaRecente) {
			filaPedidos.enfileirar(p);
		}

		DateTimeFormatter isoData = DateTimeFormatter.ISO_LOCAL_DATE;
		try (PrintWriter out = new PrintWriter(new FileWriter(nomeArquivo, Charset.forName("UTF-8")))) {
			out.println(CABECALHO_ARQUIVO_PEDIDOS_V2);
			out.println(ordemAntigoParaRecente.size());
			for (Pedido pedido : ordemAntigoParaRecente) {
				out.println(pedido.getIdPedido());
				out.println(isoData.format(pedido.getDataPedido()));
				out.println(pedido.getFormaDePagamento());
				out.println(pedido.getQuantidadeDeItens());
				ItemDePedido[] itens = pedido.getItensDoPedido();
				for (int i = 0; i < pedido.getQuantidadeDeItens(); i++) {
					Produto prod = itens[i].getProduto();
					out.println(prod.hashCode() + ";" + itens[i].getQuantidade() + ";"
							+ String.format("%.2f", itens[i].getPrecoVenda()).replace(",", "."));
				}
			}
		} catch (IOException e) {
			System.err.println("Não foi possível gravar os pedidos: " + e.getMessage());
		}
	}

	/** Reconstrói a pilha de produtos recentes a partir dos itens de um pedido. */
	private static void empilharProdutosDoPedido(Pedido pedido) {
		ItemDePedido[] itens = pedido.getItensDoPedido();
		for (int i = 0; i < pedido.getQuantidadeDeItens(); i++) {
			pilhaProdutosRecentes.empilhar(itens[i].getProduto());
		}
	}

	private static Pedido lerProximoPedidoDoArquivo(Scanner arq, DateTimeFormatter isoData) throws IOException {

		int idPedido = Integer.parseInt(arq.nextLine().trim());
		LocalDate data = LocalDate.parse(arq.nextLine().trim(), isoData);
		int forma = Integer.parseInt(arq.nextLine().trim());
		int qtdItens = Integer.parseInt(arq.nextLine().trim());
		ItemDePedido[] itens = new ItemDePedido[qtdItens];
		for (int i = 0; i < qtdItens; i++) {
			String linhaItem = arq.nextLine();
			String[] partes = linhaItem.split(";");
			int codProd = Integer.parseInt(partes[0].trim());
			int qtd = Integer.parseInt(partes[1].trim());
			double preco = Double.parseDouble(partes[2].trim().replace(",", "."));
			Produto prod = localizarProdutoPorCodigo(codProd);
			if (prod == null) {
				throw new IOException("Produto código " + codProd + " não encontrado no cadastro.");
			}
			itens[i] = new ItemDePedido(prod, qtd, preco);
		}
		return Pedido.restaurar(data, forma, idPedido, itens, qtdItens);
	}

	private static void carregarPedidos(String nomeArquivo) {

		File f = new File(nomeArquivo);
		if (!f.isFile()) {
			return;
		}

		try (Scanner arq = new Scanner(f, Charset.forName("UTF-8"))) {
			if (!arq.hasNextLine()) {
				return;
			}
			String cabecalho = arq.nextLine().trim();
			boolean formatoV2 = CABECALHO_ARQUIVO_PEDIDOS_V2.equals(cabecalho);
			boolean formatoV1 = CABECALHO_ARQUIVO_PEDIDOS_V1.equals(cabecalho);
			if (!formatoV2 && !formatoV1) {
				return;
			}
			int n = Integer.parseInt(arq.nextLine().trim());
			DateTimeFormatter isoData = DateTimeFormatter.ISO_LOCAL_DATE;
			ArrayList<Pedido> lista = new ArrayList<>(n);

			for (int p = 0; p < n; p++) {
				lista.add(lerProximoPedidoDoArquivo(arq, isoData));
			}

			if (formatoV2) {
				for (Pedido pedido : lista) {
					filaPedidos.enfileirar(pedido);
				}
				for (int i = lista.size() - 1; i >= 0; i--) {
					empilharProdutosDoPedido(lista.get(i));
				}
			} else {
				for (int i = lista.size() - 1; i >= 0; i--) {
					filaPedidos.enfileirar(lista.get(i));
				}
				for (int i = 0; i < lista.size(); i++) {
					empilharProdutosDoPedido(lista.get(i));
				}
			}
		} catch (Exception e) {
			System.err.println("Aviso: não foi possível carregar pedidos salvos (" + e.getMessage() + ").");
			while (!filaPedidos.vazia()) {
				filaPedidos.desenfileirar();
			}
			while (!pilhaProdutosRecentes.vazia()) {
				pilhaProdutosRecentes.desempilhar();
			}
		}
	}

	public static void main(String[] args) {

		teclado = new Scanner(System.in, Charset.forName("UTF-8"));

		nomeArquivoDados = "produtos.txt";
		produtosCadastrados = lerProdutos(nomeArquivoDados);
		if (quantosProdutos == 0) {
			System.out.println("Aviso: não foi possível carregar produtos.txt ou lista vazia.");
		}

		carregarPedidos(NOME_ARQUIVO_PEDIDOS);
		executarTestesPreliminaresFila();

		Pedido pedido = null;

		int opcao = -1;

		do {
			opcao = menu();
			switch (opcao) {
				case 1 -> listarTodosOsProdutos();
				case 2 -> mostrarProduto(localizarProduto());
				case 3 -> mostrarProduto(localizarProdutoDescricao());
				case 4 -> pedido = iniciarPedido();
				case 5 -> pedido = finalizarPedido(pedido);
				case 6 -> listarProdutosPedidosRecentes();
			}
			if (opcao != 0) {
				pausa();
			}
		} while (opcao != 0);

		gravarPedidos(NOME_ARQUIVO_PEDIDOS);
		teclado.close();
	}
}
