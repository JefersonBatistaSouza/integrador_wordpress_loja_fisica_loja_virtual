/*
 * Cadastro de Produto no Woocommerce
 */
package src;

import config.Conexao;
import java.sql.*;
import java.text.Normalizer;
import javax.swing.JOptionPane;

/**
 *
 * @author Jeferson
 */
public class cadastraProduto {
    //Variável de log
    private String log = "";
    private final Produto p = new Produto();
    private final Conexao con = new Conexao();
    
    //Metodo main para teste a classe
   /* public static void main(String [] args){
        String codigo = "225141";
        cadastraProduto execute = new cadastraProduto();
        System.out.println(execute.criarPostWoocommerce(codigo));
        Referencias ref = new Referencias();
        System.out.println(ref.update_query(codigo));
    }
   */
    
    //verificar se o produto ja está cadastrado no Site
    private String verificaCadastro(String codigo) {
        String situacao = "";
        try {
            String SQL = "SELECT if(STRCMP(meta_value,'" + codigo + "') = 0,'s','n') AS situacao FROM ofvw_postmeta WHERE meta_key='_sku' AND meta_value='" + codigo + "'";
            PreparedStatement ps = con.getConexaoWordpress().prepareStatement(SQL);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                situacao = rs.getString("situacao");
            }
            return situacao;//Retorna S para sim e N para nao
        } catch (SQLException ex) {
            log += "Não foi Possivel verificar o cadastro do Produto\n"
                    + "contate o administrador do Sistema" + ex;
            return situacao;
        }
    }
    //Metodo para remover carcteres especiais
    private static String removeAccents(String str) {
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = str.replaceAll("[^\\p{ASCII}]", "");
        return str;//retorna a String ja formatada
    }
    //Pegar dados do Produto do Banco de dados Local
    private Boolean getDadosProduto(String codigo) {
        try {
            if (!"".equals(codigo)) {//Verifica se campo codigo está vazio
                p.setCodigo(codigo);
                //Query de busca dos dados do produto
                String SQL = "SELECT CODIGO,DESCRICAO,ESTOQUE,PVISTA FROM PRODUTOS WHERE CODIGO ='" + p.getCodigo() + "'";
                PreparedStatement ps = con.getConexaoLocal().prepareStatement(SQL);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    p.setPrecoBruto(rs.getDouble("PVISTA"));
                    //Cadastrando o produto já com 15% de desconto
                    p.setPrecoNovo(p.getPrecoBruto() - ((p.getPrecoBruto() * 15) / 100));
                    //Descricao padrão do produto
                    p.setDescricao("<h1 style=\"text-align: center;\">" + rs.getString("DESCRICAO") + "</h1>\n"
                            + "\n"
                            + "<strong>" + rs.getString("DESCRICAO") + "\n"
                            + "<strong>CÓDIGOS: " + rs.getString("CODIGO") + "</strong>\n"
                            + "PARCELAMOS E DESPACHAMOS PARA TODO O BRASIL PARA MELHOR LHE SERVIR.\n"
                            + "TEMOS PREÇO DIFERENCIADO PARA REVENDA, BASTA FAZER O CADASTRO COM CNPJ DE REVENDA DE PEÇAS "
                            + "PARA OBTER UM MELHOR DESCONTO E ACIMA DE TUDO CONTAMOS COM UMA EQUIPE ESPECIALIZADA NO ASSUNTO\n"
                            + "\n<strong>NÃO É A PEÇA QUE VOCE PROCURA?</strong>\n"
                            + "\nENTÃO ACESSE O <strong><a href=\"https://comandaariquemes.com/buscaproduto\">"
                            + "SISTEMA DE PEÇAS</a></strong> SÃO MAIS DE 20 MIL PEÇAS PARA TRATORES AGRICOLAS"
                            + " E MAQUINAS PESADAS\n\n<a href=\"https://tratormag.com.br/loja/\">VOLTAR A LOJA E CONTINUAR COMPRANDO</a>");
                    //removendo caracteres especias do nome da peca
                    p.setNomepeca(rs.getString("DESCRICAO").replaceAll("[-*\",]", " ").replaceAll("\\ s", "-").replaceAll("\"", " ").replaceAll("[./]", " "));
                    //remover assentos das palavras
                    p.setPost_name(removeAccents(p.getNomepeca()));
                    p.setEstoque(rs.getDouble("ESTOQUE"));
                    //Verifica se o produto cadastrado tem em estoque ou não
                    if (p.getEstoque() <= 0) {
                        p.setStatusEstoque("'outofstock'");
                    } else {
                        p.setStatusEstoque("'instock'");
                    }
                }
                return true; //retorna true caso os dados forem carregado corretamente
            } else {
                //menssagem de codigo inválido
                JOptionPane.showMessageDialog(null, "Por favor digite um código válido");
                return false;
            }
        } catch (SQLException | NullPointerException ex) {
            log+="Erro ao carregar dados do Produto\nPor favor contate o administrador do sistema\n"+ex;
            return false;
        }
    }
    //Cria a query Insert da tabela Post
    private String insertPost() {
        String query = "/*CADASTRANDO PRODUTO CODIGO " + p.getCodigo() + "*/\n"
                + "INSERT INTO `ofvw_posts` (`post_author`, `post_date`, `post_date_gmt`, `post_content`, `post_title`, `post_excerpt`,\n"
                + "`post_status`, `comment_status`, `ping_status`, `post_password`, `post_name`, `to_ping`, `pinged`, `post_modified`,\n"
                + "`post_modified_gmt`, `post_content_filtered`, `post_parent`, `guid`, `menu_order`, `post_type`, `post_mime_type`,\n"
                + "`comment_count`) VALUES\n"
                + "(1,CURRENT_TIME(),CURRENT_TIME(), \n"
                + " '" + p.getDescricao() + "',\n"
                + " '" + p.getNomepeca() + "', '', 'publish', 'open', 'closed', '', \n"
                + "'" + p.getPost_name().replaceAll(" ", "-").toLowerCase() + "', '', '',CURRENT_TIME(),CURRENT_TIME(), '', 0,\n"
                + " \"https://tratormag.com.br/wp-content/uploads/2017/11/mllogo.png\", 0, 'product', 'image/jpeg', 0);\n";
        return query; //retorna a query pronta para ser executada
    }

    //Busca o ultimo registro da tabela post
    private int getMaxPostMeta() {
        try {
            String query = "SELECT MAX(ID) AS POST_ID  FROM ofvw_posts";
            PreparedStatement ps = con.getConexaoWordpress().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                p.setCodigoPost(rs.getInt("POST_ID"));
            }
            return p.getCodigoPost(); // retorna o ultimo registro
        } catch (SQLException ex) {
            log+="Não foi possivel recuperar o ultimo registro\nContate o administrador do sistema\n"+ex;
            return 0; //retorna 0 parverificação
        }
    }

    //Criar a query de inserção na tabela postMeta
    private String insertPostMeta(int codigoPost) {
        String query = "INSERT INTO ofvw_postmeta(post_id,meta_key,meta_value)values\n"
                + "(" + codigoPost + ",'_sku','" + p.getCodigo() + "'),\n"
                + "(" + codigoPost + ",'_regular_price'," + p.getPrecoBruto() + "),\n"
                + "(" + codigoPost + ",'_sale_price'," + p.getPrecoNovo() + "),\n"
                + "(" + codigoPost + ",'_manage_stock','yes'),\n"
                + "(" + codigoPost + ",'_stock'," + p.getEstoque() + "),\n"
                + "(" + codigoPost + ",'_stock_status'," + p.getStatusEstoque() + "),\n"
                + "(" + codigoPost + ",'_yoast_wpseo_primary_product_cat',90),\n"
                + "(" + codigoPost + ",'_yoast_wpseo_focuskw_text_input','" + p.getNomepeca() + "'),\n"
                + "(" + codigoPost + ",'_yoast_wpseo_focuskw','" + p.getNomepeca() + "'),\n"
                + "(" + codigoPost + ",'_yoast_wpseo_metadesc','" + p.getNomepeca() + " PEÇAS PARA VALMET, KOMATSU D50, MF, CAT, MULLER TS22, CASE, W20A, W20E, W20B LIGUE 693535-4550'),\n"
                + "(" + codigoPost + ",'_yoast_wpseo_linkdex','" + p.getNomepeca() + "'),\n"
                + "(" + codigoPost + ",'_yoast_wpseo_content_score',70),\n"
                + "(" + codigoPost + ",'_price'," + p.getPrecoNovo() + "),\n"
                + "(" + codigoPost + ",'slide_template',default),\n"
                + "(" + codigoPost + ", '_thumbnail_id', '6278');";
        return query;
    }

    //Cadastra o Post
    private Boolean cadastraPost() {
        try {
            Statement stmt = con.getConexaoWordpress().createStatement();
            stmt.executeUpdate(insertPost());
            log += " Post Inserido com Sucesso\n";
            return true;
        } catch (SQLException | NullPointerException ex) {
            log += ex + "\nContate o administrador do Sistema";
            return false;
        }
    }

    //Cadastra a postMeta do Produto
    private Boolean cadastraPostMeta() {
        //pega ultimo post cadastrado
        int codigoPost = getMaxPostMeta();
        if (codigoPost != 0) { //Verifica se getMaxPostMeta retorno 0
            try {
                Statement stmt = con.getConexaoWordpress().createStatement();
                stmt.executeUpdate(insertPostMeta(codigoPost));
                log += "PostMeta cadastrada para o\nPost: " + codigoPost + "\nCodigo Peça: " + p.getCodigo() + "\nNome da peça: " + p.getNomepeca();
                return true;
            } catch (SQLException ex) {
                log += ex + "\n contato o administrado do sistema";
                return false;
            }
        } else {
            log += "Erro ao Recuperar o codigo do Post\nStatus " + codigoPost;
            return false;
        }

    }
    //Metodo responsável pela criação do produto
    public String criarPostWoocommerce(String codigo) {
        if (!"s".equals(verificaCadastro(codigo))) {
            if (getDadosProduto(codigo) == true) {
                //log+="Dados do Produto "+codigo+ " Carregado com Sucesso\n";
                if (cadastraPost() == true) {
                    //log+="Post Cadastrado com Sucesso";
                    cadastraPostMeta();
                } else {
                    log += "\nErro ao cadastrar o Post\nProduto " + p.getCodigo() + "\nPossivel causa do Erro:\n\nCódigo foi digitado, incorretamente!"
                            + "\nProduto não Existe na Base Local"
                            + "\nCaso o erro persista contate o Administrador do Sistema";
                }
            }
            return log;
        }else{
            return "Produto "+codigo+" Já está Cadastrado no Site";
        }
    }
}
