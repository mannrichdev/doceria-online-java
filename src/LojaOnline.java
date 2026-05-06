import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class LojaOnline {

    static List<Produto> produtos = new ArrayList<>();
    static List<ItemCarrinho> carrinho = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        produtos.add(new Produto(1, "Brigadeiro", 3.50));
        produtos.add(new Produto(2, "Beijinho", 3.00));
        produtos.add(new Produto(3, "Brownie", 6.50));
        produtos.add(new Produto(4, "Cupcake", 7.00));
        produtos.add(new Produto(5, "Pão de Mel", 5.50));
        produtos.add(new Produto(6, "Trufa de Chocolate", 4.50));

        HttpServer servidor = HttpServer.create(
                new InetSocketAddress(8080), 0
        );

        servidor.createContext("/", exchange -> {
            try {
                paginaInicial(exchange);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        servidor.createContext("/adicionar", exchange -> {
            try {
                adicionarAoCarrinho(exchange);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        servidor.createContext("/carrinho", exchange -> {
            try {
                mostrarCarrinho(exchange);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        servidor.createContext("/remover", exchange -> {
            try {
                removerDoCarrinho(exchange);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        servidor.start();

        System.out.println("Loja online rodando em: http://localhost:8080");
    }

    public static void paginaInicial(HttpExchange exchange) throws Exception {

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='pt-br'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Doceria Online Java</title>");

        html.append("<style>");
        html.append("body { font-family: Arial; background:#fff3f3; margin:0; padding:0; }");
        html.append("header { background:#8B1E3F; color:white; padding:20px; text-align:center; }");
        html.append(".container { width:85%; margin:30px auto; display:grid; grid-template-columns:repeat(3,1fr); gap:20px; }");
        html.append(".produto { background:white; padding:20px; border-radius:12px; box-shadow:0 0 8px #d9a7b0; text-align:center; }");
        html.append(".produto h2 { color:#8B1E3F; }");
        html.append(".preco { color:#2e7d32; font-size:20px; font-weight:bold; }");
        html.append("button { background:#D81B60; color:white; border:none; padding:10px 15px; border-radius:6px; cursor:pointer; }");
        html.append("button:hover { background:#AD1457; }");
        html.append(".carrinho-link { display:block; text-align:center; margin:20px; }");
        html.append("</style>");

        html.append("</head>");
        html.append("<body>");

        html.append("<header>");
        html.append("<h1>Doceria Online Java</h1>");
        html.append("<p>Escolha seus doces favoritos</p>");
        html.append("</header>");

        html.append("<div class='container'>");

        for (Produto produto : produtos) {

            html.append("<div class='produto'>");

            html.append("<h2>")
                .append(produto.getNome())
                .append("</h2>");

            html.append("<p class='preco'>R$ ")
                .append(String.format("%.2f", produto.getPreco()))
                .append("</p>");

            html.append("<a href='/adicionar?id=")
                .append(produto.getId())
                .append("'>");

            html.append("<button>Adicionar ao Carrinho</button>");

            html.append("</a>");

            html.append("</div>");
        }

        html.append("</div>");

        html.append("<div class='carrinho-link'>");
        html.append("<a href='/carrinho'><button>Ver Carrinho</button></a>");
        html.append("</div>");

        html.append("</body>");
        html.append("</html>");

        enviarResposta(exchange, html.toString());
    }

    public static void adicionarAoCarrinho(HttpExchange exchange) throws Exception {

        String query = exchange.getRequestURI().getQuery();
        int id = Integer.parseInt(query.split("=")[1]);

        Produto produtoSelecionado = null;

        for (Produto produto : produtos) {
            if (produto.getId() == id) {
                produtoSelecionado = produto;
                break;
            }
        }

        if (produtoSelecionado != null) {

            boolean encontrado = false;

            for (ItemCarrinho item : carrinho) {
                if (item.getProduto().getId() == id) {
                    item.aumentarQuantidade();
                    encontrado = true;
                    break;
                }
            }

            if (!encontrado) {
                carrinho.add(new ItemCarrinho(produtoSelecionado));
            }
        }

        mostrarCarrinho(exchange);
    }

    public static void mostrarCarrinho(HttpExchange exchange) throws Exception {

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='pt-br'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Carrinho</title>");

        html.append("<style>");
        html.append("body { font-family: Arial; background:#fff3f3; padding:30px; }");
        html.append(".caixa { background:white; width:700px; margin:auto; padding:30px; border-radius:12px; box-shadow:0 0 8px #d9a7b0; }");
        html.append("h1 { color:#8B1E3F; text-align:center; }");
        html.append(".item { margin:15px 0; padding:10px; border-bottom:1px solid #ddd; }");
        html.append(".total { font-size:24px; color:#2e7d32; font-weight:bold; margin-top:20px; }");
        html.append("a { text-decoration:none; color:white; background:#D81B60; padding:10px 15px; border-radius:6px; }");
        html.append("a:hover { background:#AD1457; }");
        html.append("</style>");

        html.append("</head>");
        html.append("<body>");

        html.append("<div class='caixa'>");
        html.append("<h1>Carrinho de Compras</h1>");

        double total = 0;

        if (carrinho.isEmpty()) {
            html.append("<p>Seu carrinho está vazio.</p>");
        }

        for (ItemCarrinho item : carrinho) {

            double subtotal =
                    item.getProduto().getPreco()
                    * item.getQuantidade();

            total += subtotal;

            html.append("<div class='item'>");

            html.append(item.getProduto().getNome());
            html.append(" | Quantidade: ");
            html.append(item.getQuantidade());
            html.append(" | Subtotal: R$ ");
            html.append(String.format("%.2f", subtotal));

            html.append(" <a href='/remover?id=");
            html.append(item.getProduto().getId());
            html.append("'>Remover</a>");

            html.append("</div>");
        }

        html.append("<div class='total'>");
        html.append("Total: R$ ");
        html.append(String.format("%.2f", total));
        html.append("</div>");

        html.append("<br><br>");
        html.append("<a href='/'>Voltar para loja</a>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        enviarResposta(exchange, html.toString());
    }

    public static void removerDoCarrinho(HttpExchange exchange) throws Exception {

        String query = exchange.getRequestURI().getQuery();
        int id = Integer.parseInt(query.split("=")[1]);

        carrinho.removeIf(item ->
                item.getProduto().getId() == id
        );

        mostrarCarrinho(exchange);
    }

    public static void enviarResposta(HttpExchange exchange, String resposta) throws Exception {

        byte[] bytes = resposta.getBytes("UTF-8");

        exchange.getResponseHeaders().add(
                "Content-Type",
                "text/html; charset=UTF-8"
        );

        exchange.sendResponseHeaders(200, bytes.length);

        OutputStream os = exchange.getResponseBody();

        os.write(bytes);

        os.close();
    }
}

class Produto {

    private int id;
    private String nome;
    private double preco;

    public Produto(int id, String nome, double preco) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public double getPreco() {
        return preco;
    }
}

class ItemCarrinho {

    private Produto produto;
    private int quantidade;

    public ItemCarrinho(Produto produto) {
        this.produto = produto;
        this.quantidade = 1;
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void aumentarQuantidade() {
        quantidade++;
    }
}