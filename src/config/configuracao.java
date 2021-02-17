/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 *
 * @author JEFERSON
 */
public class configuracao {
    private static final String curDir = System.getProperty("user.dir");
    private static String url ="";
    private static Properties getConexaoLocal(String tipo) throws IOException {
        
        if(tipo.equals("remoto")){
            url = curDir+"\\properties_file\\conexao_remota.properties";
        }else if(tipo.equals("local")){
            url = curDir+"\\properties_file\\conexao_local.properties";
        }
        Properties props = new Properties();
        FileInputStream file = new FileInputStream(url);
        props.load(file);
        return props;
    }

    public void configura(String tipo, String host, String userName, String password, String nameDatabase) throws IOException {
        Properties prop = getConexaoLocal(tipo);
        prop.setProperty("host_ip", host);
        prop.setProperty("name_userName", userName);
        prop.setProperty("password", password);
        prop.setProperty("nameDatabase", nameDatabase);
        if(tipo.equals("remoto")){
            url = curDir+"\\properties_file\\conexao_remota.properties";
        }else if(tipo.equals("local")){
            url = curDir+"\\properties_file\\conexao_local.properties";
        }
        System.out.println(curDir);
        JOptionPane.showMessageDialog(null,curDir);
        File file = new File(url);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            prop.store(fos, "UTF-8");
        }
    }
    
    public Properties getConf_bdLocal() throws IOException{
        Properties prop = getConexaoLocal("local");
        return prop;
    }
    
    public Properties getConf_bdRemoto() throws IOException{
        Properties prop = getConexaoLocal("remoto");
        return prop;
    }
}
