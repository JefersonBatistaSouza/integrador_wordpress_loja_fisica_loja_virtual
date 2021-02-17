/*
 *Conexão Banco de Dados
 */
package config;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Jeferson
 */
public class Conexao  {
    private static Connection conexaoLocal;
    private static Connection conexaoWordpress;
    configuracao conf = new configuracao();
    //Conexao com banco de dados Local
    public Connection getConexaoLocal(){
        try {
            Properties props = conf.getConf_bdLocal();
            String host = props.getProperty("host_ip");
            String nameDatase = props.getProperty("nameDatabase");
            String password = props.getProperty("password");
            String userName = props.getProperty("name_userName");
            try {
                if (conexaoLocal == null) {
                    Class.forName("org.firebirdsql.jdbc.FBDriver");
                    conexaoLocal = DriverManager.getConnection("jdbc:firebirdsql:"+host+nameDatase, userName, password);
                }
                return conexaoLocal;
            } catch (ClassNotFoundException | SQLException ex) {
                JOptionPane.showMessageDialog(null, "Erro Conexão com o Banco local\n" + ex, "Erro Conexão", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Arquivo de COnfiguraçã Error\n" + ex, "Erro Arquivo Configuração", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    //Conexão com o seu Wordpress
    public Connection getConexaoWordpress() {
        try {
            Properties props = conf.getConf_bdRemoto();
            String host = props.getProperty("host_ip");
            String nameDatase = props.getProperty("nameDatabase");
            String password = props.getProperty("password");
            String userName = props.getProperty("name_userName");
            try {
                if (conexaoWordpress == null) {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    conexaoWordpress = DriverManager.getConnection("jdbc:mysql://"+host+"/"+nameDatase, userName, password);
                }
                return conexaoWordpress;
            } catch (ClassNotFoundException | SQLException ex) {
                JOptionPane.showMessageDialog(null, "Erro na Conexão com o Banco Remoto\n" + ex, "Erro Conexão", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } catch (IOException ex) {
           JOptionPane.showMessageDialog(null, "Arquivo de Configuraçã Error\n" + ex, "Erro Arquivo Configuração", JOptionPane.ERROR_MESSAGE);
           return null;
        }
    }

}
