/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

import config.Conexao;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author jeferson
 */
public class UpdateStock {
    private String virgula = ",";
    private String log = "";
    private final ArrayList<Double> stock_local = new ArrayList<>();
    private final ArrayList<Double> stock_site = new ArrayList<>();
    private final ArrayList<String> produto = new ArrayList<>();
    private final ArrayList<Integer> post_id = new ArrayList<>();
    //Classe conexão
    private final Conexao con = new Conexao();
    
    //Metodo main para Testar a atualização do estoque
   /* public static void main(String[] args) {
        UpdateStock execute = new UpdateStock();
        System.out.println(execute.update_stock("",false,0.0));
    }
   */  
    //Pega todo os valores em estoque dos produto no site
    private void getStock_site(String condicao) {
        try {
            String query = "SELECT meta_value FROM ofvw_postmeta WHERE meta_key = '_stock' AND post_id IN(" + condicao + ") ORDER BY post_id DESC";
            PreparedStatement ps = con.getConexaoWordpress().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                //atribui estoque 0 se meta value vazio ou nulo
                if ("".equals(rs.getString("meta_value")) || rs.getString("meta_value") == null) {
                    stock_site.add(0.0);
                } else {
                    stock_site.add(Double.parseDouble(rs.getString("meta_value").replaceAll(",", ".")));
                }
            }
            log += "Estoque do Site Carregado com Sucesso\n";
        } catch (SQLException ex) {
            log += " Erro ao Carregar estoque do Site\n **Post não existe***\n Se o erro persistir contate o administrador\n";
        }
    }

    //Pega todo os valores em estoque dos produto no banco local de acordo com o codigo
    //cadastrados no site
    private void getStock_local(String codigo) {
        try {
            String query = "SELECT ESTOQUE FROM PRODUTOS WHERE CODIGO ='" + codigo + "'";
            PreparedStatement ps = con.getConexaoLocal().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                if (!"".equals(rs.getDouble("ESTOQUE"))) {
                    stock_local.add(rs.getDouble("ESTOQUE"));
                }
            }
        } catch (SQLException ex) {
            log += "***Erro ao Recuperar Stock local***" + ex;
        }
    }

    //Recuperar o ID do Post e o SKU do produto
    private void getPost_id_and_Meta_Value(String codigo) {
        try {
            String condicao = " ORDER BY post_id DESC";
            //se campo codigo estiver preenchido, recuperará
            // o ID  e SKU apenas do produto informado
            if (!"".equals(codigo)) {
                condicao = " AND meta_value ='" + codigo + "' ORDER BY post_id DESC";
            }
            String query = "SELECT post_id, meta_value FROM ofvw_postmeta WHERE meta_key = '_sku'" + condicao;
            PreparedStatement ps = con.getConexaoWordpress().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (!"".equals(rs.getString("meta_value")) && rs.getString("meta_value") != null) {
                    post_id.add(rs.getInt("post_id"));
                    produto.add(rs.getString("meta_value"));
                }
            }
            if (post_id.size() == produto.size()) {
                log += "Post_id e SKU do produto Carregado com Sucesso\n";
            } else {
                log += "Não foi possivel carregar o SKU  e o ID  do produto\n Verifique se o Post existe\n"
                        + "Caso o erro persista contate o administrador do sistema";
            }
        } catch (SQLException | IndexOutOfBoundsException ex) {
            log += " Erro ao Carregar Post_id e SKU ***Post não existe***\n Se o erro persistir contate o administrador\n";
        }
    }
    //Executar a query de atualização de estoque e atualização de status
    private void ExecuteUpdate(String query_stock, String query_stock_status) {
        try {
            Statement stmt = con.getConexaoWordpress().createStatement();
            stmt.execute(query_stock);
            stmt.execute(query_stock_status);
        } catch (SQLException e) {
            log += "\nNão foi possível executar o SQL\n" + e + "\nCONTATE O ADMINISTRADOR DO SISTEMA\n\n";
        }
    }
    //Metodo responsavel por gerar as querys de atualização
    public String update_stock(String codigo, boolean determinar_stock, double _stock) {
        //Array auxiliador
        ArrayList<Integer> aux_post_id = new ArrayList<>();
        //condição em que vai rodar a query
        String condicao = "";
        int cont = 0;
        //query de atualização
        String query_stock = "UPDATE ofvw_postmeta SET meta_value = CASE post_id\n";
        String query_stock_status = "UPDATE ofvw_postmeta SET meta_value = CASE post_id\n";
        String status_stock = "";
        //Popula o array post_id e o array produto meta_value
        getPost_id_and_Meta_Value(codigo);
        //Pega a quantidade de indice do array
        int qt_post = post_id.size();
        int qt_produto = produto.size();
        /*Cria uma condicao para query sql*/
        for (int i = 0; i < qt_post; i++) {
            if (i == qt_post - 1) {
                virgula = "";
            }
            //cria a condicão da query
            condicao += post_id.get(i) + virgula;
        }

        ///Popula um array com o estoque do site
        getStock_site(condicao);
        //Verifica se o Estoque vai ser determinado pelo Usuario
        if (determinar_stock == false) {
            for (int i = 0; i < qt_produto; i++) {
                //Popula o array com stock da base localhost
                getStock_local(produto.get(i));
            }
        } else {
            //Valor do Estoque determinado pelo Usuario
            stock_local.add(_stock);
        }
        //pega a quantidade de indece do array
        int qt_stock_local = stock_local.size();
        int qt_stock_site = stock_site.size();
        //Verifica se os array coincidem entre si para a atualização
        //System.out.println(qt_post+" "+qt_stock_local+" "+qt_stock_site+" "+qt_produto);
        if (qt_post == qt_produto && qt_produto == qt_stock_local && qt_stock_local == qt_stock_site) {
            //Criar a query de atualização do estoque
            for (int i = 0; i < qt_post; i++) {
                //Gera a condicao somente se o estoque do site for diferente
                //do estoque banco de dados local
                if (!stock_local.get(i).equals(stock_site.get(i))) {
                    //Gerando corpo da query
                    query_stock += " WHEN " + post_id.get(i) + " THEN '" + stock_local.get(i) + "'\n";
                    //alterando o status do estoque de acordo com o stock local
                    if (stock_local.get(i) == 0.0) {
                        status_stock = "outofstock";
                    } else {
                        status_stock = "instock";
                    }
                    //queery gerada
                    query_stock_status += " WHEN " + post_id.get(i) + " THEN '" + status_stock + "'\n";
                    //log dos preço que foram atualizados 
                    log += cont + "-" + i + " Stock site " + stock_site.get(i) + " Atualizado para " + stock_local.get(i)
                            + " Produto " + produto.get(i) + " Post " + post_id.get(i) + " Status " + status_stock + "\n";
                   //recebera o post dos produtos atualizados
                    aux_post_id.add(post_id.get(i));
                    cont = cont + 1;
                }
            }
            //verifica se a quantidade de produtos atualizado foi maior que 0
            if (cont > 0) {
                condicao = "";
                virgula = ",";
                for (int i = 0; i < cont; i++) {
                    if (i == cont - 1) {
                        virgula = "";
                    }
                    //adiciona uma virgula após cada post da condicao
                    condicao += aux_post_id.get(i) + virgula;
                }
                //adiciona a condião a query para atualizar o estoque
                query_stock += " END\n WHERE meta_key='_stock' AND post_id IN(" + condicao + ")";
                //adiciona a condição a query para atualizar o status do estoque
                query_stock_status += "END\n WHERE meta_key='_stock_status' AND post_id IN(" + condicao + ")";
                //Executa as query gerado
                ExecuteUpdate(query_stock, query_stock_status);
            } else {
                // Caso cont seja igual, nenhum produto foi atualizado
                log += "\nO estoque já se Encontra Atualizado\n";
            }
            //retorna a quantidade produto que foi atualizados
            return log += "\n\n" + cont + " produtos com estoque Atualizados";
        } else {
            //todos os array deve ter quantidade de indice identicos
            log += "\n\n***Os valores não são iguais\nContate o Administrador do Sistema\n Post "
                    + qt_post + "\n stock_local " + qt_stock_local + "\n Produto " + qt_produto + "\n stock Site " + qt_stock_site;
            return log;
        }

    }
}
