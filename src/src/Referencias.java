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
public class Referencias {

    private final Conexao con = new Conexao();
    //Variáveis auxiliadoras
    private String log = "";
    private String referencias = "";
    private int post_id = 0;
    private String cod_produto = "";
    private final ArrayList<String> codigo = new ArrayList<>();
    private final ArrayList<String> ref_produto = new ArrayList<>();

    //metodo main para testar a classe
    /*public static void main(String [] args){
        Referencias execute = new Referencias();
        System.out.println(execute.update_query("84359314"));
    }
     */
    
    //Nossos produto tem um campo chamado referencia, que são varios código
    //que representa o mesmo produto
    private Boolean ReferenciadoProduto(String codigo) {
        try {
            //query de busca
            String SQL_local = "SELECT REFERENCIA FROM REFERENCIAS WHERE PRODUTO ='" + codigo + "'";
            PreparedStatement ps = con.getConexaoLocal().prepareStatement(SQL_local);
            ResultSet rs = ps.executeQuery();
            //Recupera as Referencia dos Produtos de Acordo com o codigo
            while (rs.next()) {
                if (!"".equals(rs.getString("REFERENCIA")) && rs.getString("REFERENCIA") != null) {
                    referencias += rs.getString("REFERENCIA") + " ";
                }
            }
            //Retorna true se o referencia for diferente de vazio
            return !"".equals(referencias);
        } catch (SQLException ex) {
            log += "Referências Error\n" + ex + "\n";
            return false;
        }
    }

    //Recupera o Pos ID  e o SKU do produto
    private Boolean getPost_id_and_Meta_Value(String codigo) {
        try {
            String condicao = "";
            //faz a consulta em um produto especifico
            if (!"".equals(codigo)) {
                condicao = " AND meta_value ='" + codigo + "' ORDER BY post_id DESC";
                //query de consulta
                String query = "SELECT post_id, meta_value FROM ofvw_postmeta WHERE meta_key = '_sku'" + condicao;
                PreparedStatement ps = con.getConexaoWordpress().prepareStatement(query);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    //traz somente os post cujo meta value é diferente vazio ou nulo
                    if (!"".equals(rs.getString("meta_value")) && rs.getString("meta_value") != null) {
                        post_id = (rs.getInt("post_id"));
                        cod_produto = (rs.getString("meta_value"));
                    }
                }
                if (post_id != 0 && !"".equals(cod_produto)) {
                    log += "Post_id e SKU do produto Carregado com Sucesso\n";
                    return true;
                } else {
                    log += "Post do produto " + codigo + " não foi localizado\n";
                    return false;
                }
            } else {
                return false;
            }
        } catch (SQLException | IndexOutOfBoundsException ex) {
            log += " Erro ao Carregar Post_id e SKU ***Post não existe***\n Se o erro persistir contate o administrador\n";
            return false;
        }

    }

    //Executa a query de atualização
    private void ExecuteUpdate(String ref_aux) {
        try {
            Statement stmt = con.getConexaoWordpress().createStatement();
            //referencia ficará abaixo do nome do produto
            stmt.execute("UPDATE ofvw_posts SET post_excerpt ='<h3 style=\"text-align: center; font-weight:bold; padding:0px; margin:0px;\">Referencias " + ref_aux + "</h3>' WHERE ID =" + post_id);
            //informa a referencia que foi inserida no produto
            log += "\nReferencia inserida-> " + ref_aux + "\nProduto " + cod_produto + " Post " + post_id + "\n";
        } catch (SQLException e) {
            log += "\nNÃO FOI POSSIVEL EXCUTAR O SQL\n" + e + "\nCONTATE O ADMINISTRADOR DO SISTEMA\n\n";
        }
    }

    //Método que inseri a referência no porduto
    public String update_query(String codigo) {
        /*Caso todos os métodos retornar true fará a atualização*/
        if (getPost_id_and_Meta_Value(codigo) != false && ReferenciadoProduto(cod_produto) != false) {
            ExecuteUpdate(referencias);
            return log;
        } else {
            return "Referencia Não Encontrada Verifique:\n 1-> Produto não Cadastro na Base Local\n 2-> Produto da Base local não passui referencia\n 3-> Produto não está Cadastrado no Site\n"
                    + "\n" + log;
        }
    }
}
