/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

/**
 *
 * @author JEFERSON
 */
public class Banco_de_Dados {
    private String tipo;
    private String host_ip;
    private String userName;
    private String password;
    private String nameDatabase;

    public Banco_de_Dados(String tipo, String host_ip, String userName, String password, String nameDatabase) {
        this.tipo = tipo;
        this.host_ip = host_ip;
        this.userName = userName;
        this.password = password;
        this.nameDatabase = nameDatabase;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getHost_ip() {
        return host_ip;
    }

    public void setHost_ip(String host_ip) {
        this.host_ip = host_ip;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNameDatabase() {
        return nameDatabase;
    }

    public void setNameDatabase(String nameDatabase) {
        this.nameDatabase = nameDatabase;
    }
}
