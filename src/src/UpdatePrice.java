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
public class UpdatePrice {
    
    /*Chama conexão com o Banco de Dados*/
    private final Conexao con = new Conexao();
    private String log = "";
    private final ArrayList<Integer> post_id = new ArrayList<>();
    private final ArrayList<Double> preco_local = new ArrayList<>();
    private final ArrayList<Double> preco_site = new ArrayList<>();
    private final ArrayList<String> produto = new ArrayList<>();
    private final ArrayList<Double> regular_price = new ArrayList<>();

    //metodo main para testar a classe
    /*public static void main(String[] args) {
        UpdatePrice upPrice = new UpdatePrice();
        System.out.println(upPrice.update_price("",-15, false, 0));
    }
   */ 
    
    private void getPrice_site(String condicao) {
        try {
            String query = "SELECT meta_value FROM ofvw_postmeta WHERE meta_key = '_sale_price' AND post_id IN(" + condicao + ") ORDER BY post_id DESC";
            PreparedStatement ps = con.getConexaoWordpress().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (!"".equals(rs.getString("meta_value")) && rs.getString("meta_value") != null) {
                    preco_site.add(Double.parseDouble(rs.getString("meta_value").replaceAll(",", ".")));
                }
            }
            log += "Preço do Site Carregado com Sucesso\n";
        } catch (SQLException ex) {
            log += " Erro ao Carregar Preço do Site\n **Post não existe***\n Se o erro persistir contate o administrador\n";
            //preco_site.add(2.5);
        }
    }

    private void getPost_id_and_Meta_Value(String codigo) {
        try {
            String condicao = " ORDER BY post_id DESC";
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
            if(post_id.get(0) > 0){
                log += "Post_id e SKU do produto Carregado com Sucesso\n";
            }else{
                log += "Post do produto "+codigo+" não foi localizado\n";
            }
        } catch (SQLException | IndexOutOfBoundsException ex) {
            log += " Erro ao Carregar Post_id e SKU ***Post não existe***\n Se o erro persistir contate o administrador\n";
            //post_id.add(1);
        }
    }

    private void getPreco_local(String codigo, double desconto) {
        try {
            String query = "SELECT PVISTA FROM PRODUTOS WHERE CODIGO ='" + codigo + "'";
            PreparedStatement ps = con.getConexaoLocal().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (!"".equals(rs.getDouble("PVISTA"))) {
                    if (desconto < 0) {
                        preco_local.add(rs.getDouble("PVISTA") + ((rs.getDouble("PVISTA") * desconto) / 100));
                    } else if (desconto > 0) {
                        preco_local.add(rs.getDouble("PVISTA") + ((rs.getDouble("PVISTA") * desconto) / 100));
                    } else {
                        preco_local.add(rs.getDouble("PVISTA"));
                    }
                    regular_price.add(rs.getDouble("PVISTA"));
                    //System.out.println(codigo + " "+rs.getDouble("PVISTA"));
                }else{
                    System.out.println(codigo);
                }
            }
            if(preco_local.get(0) > 0){
            }else{log +="*** Produto não exite na base Local***\nSe o erro Persistir contate o administrador\n";}
        } catch (SQLException ex) {
            log += " Erro ao Carregar Preço localhost\n*** Produto não exite na base Local***\nSe o erro Persistir contate o administrador\n";
            //preco_local.add(2.0);
            //regular_price.add(3.0);
        }
    }

    private void ExecuteUpdate(int post_id, double preco, double regular_price) {
        try {
            Statement stmt = con.getConexaoWordpress().createStatement();
            stmt.execute("UPDATE ofvw_postmeta SET meta_value ='" + String.format("%.2f", preco) + "' WHERE meta_key IN('_sale_price','_price') AND post_id =" + post_id);
            stmt.execute("UPDATE ofvw_postmeta SET meta_value ='" + String.format("%.2f", regular_price) + "' WHERE meta_key = '_regular_price' AND post_id =" + post_id);
            
        } catch (SQLException e) {
            log += "\nNÃO FOI POSSIVEL EXCUTAR O SQL\n" + e + "\nCONTATE O ADMINISTRADOR DO SISTEMA\n\n";
        }
    }

    public String update_price(String codigo, double desconto, boolean determinar_price, double _price) {
        String condicao = "";
        String virgula = ",";
        int cont = 0;
        //Popula o array post_id e o array produto meta_value
        getPost_id_and_Meta_Value(codigo);
        
        //Pega a quantidade de indice do array
        int qt_post = post_id.size();
        int qt_produto = produto.size();
        /*Cria uma query sql*/
        for (int i = 0; i < qt_post; i++) {
            if (i == qt_post - 1) {
                virgula = "";
            }
            condicao += post_id.get(i) + virgula;
        }
        //query para recuperar preço do site gerada
        //System.out.println("SELECT post_id,meta_value FROM ofvw_postmeta WHERE meta_key = '_sale_price' AND post_id IN("+condicao+")");
        ///Popula um array com todos os preços do site
        getPrice_site(condicao);
        //Verifica se o usuario deseja determinar o valor do pruduto
        if(determinar_price != true){
        for (int i = 0; i < qt_produto; i++) {
            //Popula o array com preço da base localhost
            getPreco_local(produto.get(i), desconto);

        }
        }else{
            //Preco determinados pelo usuario
            preco_local.add(_price);
            regular_price.add(_price + ((_price*15)/100));
        }
        log +="Busca em localhost efetuada";
        //pega a quantidade de indeco do array
        int qt_preco = preco_local.size();
        int qt_preco_site = preco_site.size();
        int qt_regular_price = regular_price.size();
        //Verifica se os array coincidem entre si para a atualização
        //System.out.println(qt_post+" "+qt_preco+" "+qt_preco_site+" "+qt_produto+" "+qt_regular_price);
        if (qt_post == qt_produto && qt_produto == qt_preco && qt_preco == qt_preco_site && qt_regular_price == qt_preco) {
            //executar a atualização de preço por post
            log += "\nLista de Produtos Atualizados\n";
            for (int i = 0; i < qt_preco; i++) {
                //Verifica os Preço, caso seja diferente ele executa o update
                if (!String.format("%.2f",preco_site.get(i)).equals(String.format("%.2f",preco_local.get(i)))) {
                    //Executa a atualização
                    ExecuteUpdate(post_id.get(i), preco_local.get(i), regular_price.get(i));
                    log += cont +"->" +i + " Preco " + String.format("%.2f", preco_site.get(i)) + " Atualizado para "
                            + String.format("%.2f", preco_local.get(i)) + " Base Calc("+String.format("%.2f", regular_price.get(i))+""+desconto+"%) "
                            + "Produto " + produto.get(i) + " Post "+post_id.get(i)+"\n\n";
                    //Quantidade de Produtos Atualizados
                    cont = cont + 1;
                    
                }
            }
            if (cont == 0) {
                log += "\n"+cont+" Produtos Atualizados";
                log += "\n\nOs preços do Site já se encontra Atualizado";
            }else{
                log += "Atualização Completada com Sucesso";
            }
            return log;
            
        } else {
            log += "\n\n***Os valores não são iguais\n Post "
                    + qt_post + "\n Preco " + qt_preco + "\n Produto " + qt_produto + "\nPreco Site " + qt_preco_site+"\n"+
                    "1 -> Verificar se há produto na lixeira do site\n"
                   +"2 -> Algum produto pode estar sem preco promocional\n\n"
                   +"3 -> Há Referencias no Site que não existe no Sistema Local\n\n Se o erro persistir contate o administrador";
                    //System.out.println(produto.get(2222));
            return log;
        }
    }
}
