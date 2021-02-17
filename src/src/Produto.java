/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

/**
 *
 * @author JEFERSON
 */
public class Produto {
    private String descricao;
    private String codigo;
    private double precoBruto;
    private double precoDesconto;
    private double precoNovo;
    private int codigoPost;
    private String post_name;
    private String nomepeca;
    private double estoque;
    private String statusEstoque;

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public double getPrecoBruto() {
        return precoBruto;
    }

    public void setPrecoBruto(double precoBruto) {
        this.precoBruto = precoBruto;
    }

    public double getPrecoDesconto() {
        return precoDesconto;
    }

    public void setPrecoDesconto(double precoDesconto) {
        this.precoDesconto = precoDesconto;
    }

    public double getPrecoNovo() {
        return precoNovo;
    }

    public void setPrecoNovo(double precoNovo) {
        this.precoNovo = precoNovo;
    }

    public int getCodigoPost() {
        return codigoPost;
    }

    public void setCodigoPost(int codigoPost) {
        this.codigoPost = codigoPost;
    }

    public String getPost_name() {
        return post_name;
    }

    public void setPost_name(String post_name) {
        this.post_name = post_name;
    }

    public String getNomepeca() {
        return nomepeca;
    }

    public void setNomepeca(String nomepeca) {
        this.nomepeca = nomepeca;
    }

    public double getEstoque() {
        return estoque;
    }

    public void setEstoque(double estoque) {
        this.estoque = estoque;
    }

    public String getStatusEstoque() {
        return statusEstoque;
    }

    public void setStatusEstoque(String statusEstoque) {
        this.statusEstoque = statusEstoque;
    }
}
