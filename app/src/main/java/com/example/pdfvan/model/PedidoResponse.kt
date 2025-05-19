package com.example.pdfvan.model

data class PedidoResponse(
    val Pedidos: List<Pedido>
)

data class Pedido(
    val PDV_PedidoCodigo: String,
    val PDV_PedidoEmpDescricao: String,
    val PDV_PedidoEmpFantasia: String,
    val PDV_PedidoDataEmissao: String,
    val PDV_PedidoValorTotal: String,
    val PDV_PedidoCondicaoPgtoDescricao: String,
    val PDV_PedidoTipoFreteDescricao: String,
    val PDV_PedidoFreteValor: String,
    val PDV_PedidoObs: String,
    val PedidoObservacao: List<PedidoObservation>,
    val PedidoProdutos: List<Product>,
    val PDV_PedidoEnderecos: List<PedidoAddress>,
)

data class PedidoObservation(
    val PDV_PedidoObsDescricao: String
)

data class Product(
    val PDV_PedidoItemProduto: String,
    val PDV_PedidoItemProdutoNome: String,
    val PDV_PedidoItemQtdPedida: String,
    val PDV_PedidoItemValorUnitario: String,
    val PDV_PedidoItemDescontoValor: String,
    val PDV_PedidoItemValorTotal: String,
    val PedidoItemImposto: List<ProductTax>
)

data class ProductTax(
    val PDV_PedidoItemIImpostoFiscalDescricao: String,
    val PDV_PedidoItemIValor: String
)

data class PedidoAddress(
    val EMP_EnderecoLogradouro: String,
    val EMP_EnderecoNumero: Int,
    val EMP_EnderecoComplemento: String,
    val EMP_EnderecoBairro: String,
    val CID_Descricao: String,
    val CID_EstadoCodigo: String,
    val CID_LogradouroCEP: Int
)
