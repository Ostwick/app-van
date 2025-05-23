package com.example.pdfvan.model

data class PedidoResponse(
    val Pedidos: List<Pedido>,
    val Filtros: Filtros,
    val Mensagens: List<Mensagem>
)

data class Pedido(
    val PDV_PedidoFilial: String,
    val PDV_PedidoFilialNomeFantasia: String,
    val PDV_PedidoFilialNomeRazaoSocial: String,
    val PDV_PedidoCodigo: String,
    val PDV_PedidoEmpCodigo: String,
    val PDV_PedidoEmpDescricao: String,
    val PDV_PedidoEmpFantasia: String,
    val PDV_PedidoConsumidorFinal: String,
    val PDV_PedidoObsProducao: String,
    val PDV_PedidoObs: String,
    val PDV_PedidoObsFinanceiras: String,
    val PDV_PedidoDataEmissao: String,
    val PDV_PedidoRepresentante: String,
    val PDV_PedidoTipoMovimentoCodigo: Int,
    val PDV_PedidoTipoMovimentoDescricao: String,
    val PDV_PedidoCondicaoPgtoCodigo: Int,
    val PDV_PedidoDescontoPerc: Double,
    val PDV_PedidoDescontoValor: String,
    val PDV_PedidoDataDigitacao: String,
    val PDV_PedidoDataEntrega: String,
    val PDV_PedidoCondicaoPgtoDescricao: String,
    val PDV_PedidoSituacao: String,
    val PDV_PedidoAprovacao: String,
    val PDV_PedidoSomaFrete: String,
    val PDV_PedidoSomaST: String,
    val PDV_PedidoEndereco: Int,
    val PDV_PedidoValorAcrescimo: String,
    val PDV_PedidoUsuario: String,
    val PDV_PedidoCCF: Int,
    val PDV_PedidoCOO: Int,
    val PDV_PedidoECF_Serie: String,
    val PDV_PedidoValorTotal: String,
    val PDV_PedidoLocalEntrega: String,
    val PDV_PedidoBancoCobranca: String,
    val PDV_PedidoContaCobranca: String,
    val PDV_PedidoTipoPagamento: String,
    val PDV_PedidoTipoPedido: String,
    val PDV_PedidoTipoCod: Int,
    val PDV_PedidoEmpEntCodigo: Int,
    val PDV_PedidoEmpEntEndereco: Int,
    val PDV_PedidoOperadora: String,
    val PDV_PedidoOperadoraBandeira: Int,
    val PDV_PedidoCRMCodigo: Int,
    val PDV_PedidoCRMDescricao: String,
    val PDV_PedidoTabelaPreco: Int,
    val PDV_PedidoTabelaPrecoDescricao: String,
    val PDV_PedidoOrdemCompra: String,
    val PDV_PedidoTransportadorCodigo: String,
    val PDV_PedidoTransportadorDescricao: String,
    val PDV_PedidoRedespachoCodigo: String,
    val PDV_PedidoRedespachoDescricao: String,
    val PDV_PedidoFreteRedespacho: Int,
    val PDV_PedidoFreteValor: String,
    val PDV_PedidoVolumes: String,
    val PDV_PedidoPesoBruto: String,
    val PDV_PedidoPesoLiquido: String,
    val PDV_PedidoValorFinanceiro: String,
    val PDV_PedidoTipoFreteCodigo: Int,
    val PDV_PedidoTipoFreteDescricao: String,
    val PDV_PedidoUsuarioAlteracao: String,
    val PDV_PedidoDataHoraAlteracao: String,
    val PDV_PedidoEmpCobCodigo: Int,
    val PDV_PedidoEmpCobEndereco: Int,
    val PDV_PedidoEmpContato: Int,
    val PDV_PedidoContato: Int,
    val TotalM3: String,
    val TotalICMS: String,
    val TotalIPI: String,
    val TotalST: String,
    val TotalPIS: String,
    val TotalCOFINS: String,
    val PDV_PedidoEnderecos: List<PedidoAddress>,
    val PedidoObservacao: List<PedidoObservation>,
    val PedidoProdutos: List<Product>
)

data class PedidoObservation(
    val PDV_PedidoObsCodigo: Int,
    val PDV_PedidoObsDescricao: String
)

data class Product(
    val PDV_PedidoItemSeq: Int,
    val PDV_PedidoItemTabela: Int,
    val PDV_PedidoItemProduto: String,
    val PDV_PedidoItemProdutoNome: String,
    val PDV_PedidoItemProdutoNomeManual: String,
    val PDV_PedidoItemProdutoUnidade: String,
    val PDV_PedidoItemProdutoUnidadeMan: String,
    val PDV_PedidoItemQtdPedida: String,
    val PDV_PedidoItemDespesasRateado: String,
    val PDV_PedidoItemSeguroRateado: String,
    val PDV_PedidoItemDescontoRateado: String,
    val PDV_PedidoItemAcrescimoRateado: String,
    val PDV_PedidoItemDescontoPercentual: Double,
    val PDV_PedidoItemDescPercCalculado: Double,
    val PDV_PedidoItemAcrescimoPercentual: Double,
    val PDV_PedidoItemValorUnitario: String,
    val PDV_PedidoItemValorTotal: String,
    val PDV_PedidoItemValorOriginal: String,
    val PDV_PedidoItemValorTabela: String,
    val PDV_PedidoItemGrade: String,
    val PDV_PedidoItemGradeDescricao: String,
    val PDV_PedidoItemCST: String,
    val PDV_PedidoItemAliquota: Int,
    val PDV_PedidoItemBaseCalculo: String,
    val PDV_PedidoItemRegra: Int,
    val PDV_PedidoItemCFOP: String,
    val PDV_PedidoItemPesoBruto: String,
    val PDV_PedidoItemPesoLiquido: String,
    val PDV_PedidoItemVolumes: String,
    val PDV_PedidoItemProdutoPesoBruto: String,
    val PDV_PedidoItemProdutoPesoLiquido: String,
    val PDV_PedidoItemProdutoVolume: String,
    val PDV_PedidoItemDescontoValor: String,
    val PDV_PedidoItemDataEntrega: String,
    val PDV_PedidoItemAprovacao: String,
    val PDV_PedidoItemAprovacaoNome: String,
    val PDV_PedidoItemSituacao: String,
    val PDV_PedidoItemFreteRateado: String,
    val PDV_PedidoItemLinhaProduto: Int,
    val PDV_PedidoItemLinhaProdutoDesc: String,
    val Pdv_PedidoItemDesc: List<Any>, // Dependendo da estrutura real, pode ser outra classe de dados
    val PedidoItemComposto: List<Any>, // Dependendo da estrutura real, pode ser outra classe de dados
    val PedidoItemEst: List<Any>, // Dependendo da estrutura real, pode ser outra classe de dados
    val PedidoItemImposto: List<ProductTax>
)

data class ProductTax(
    val PDV_PedidoItemIImposto: Int,
    val PDV_PedidoItemIImpostoFiscal: Int,
    val PDV_PedidoItemIImpostoFiscalDescricao: String,
    val PDV_PedidoItemIRegra: Int,
    val PDV_PedidoItemIFormula: Int,
    val PDV_PedidoItemICFOP: String,
    val PDV_PedidoItemICST: String,
    val PDV_PedidoItemIBCalculo: String,
    val PDV_PedidoItemIAliquota: Double,
    val PDV_PedidoItemIValor: String,
    val PDV_PedidoItemIIsentas: String,
    val PDV_PedidoItemIOutras: String,
    val PDV_PedidoItemIClasseNF: String,
    val PDV_PedidoItemIClasseFN: String,
    val PDV_PedidoItemIFatura: String,
    val PDV_PedidoItemIParcela: Int,
    val PDV_PedidoItemIMVA: Double,
    val PDV_PedidoItemIValorUnidade: String
)

data class PedidoAddress(
    val EMP_EnderecoTipo: String,
    val EMP_EnderecoTipoDescricao: String,
    val EMP_EnderecoLogradouro: String,
    val EMP_EnderecoNumero: Int,
    val EMP_EnderecoComplemento: String,
    val EMP_EnderecoBairro: String,
    val CID_PaisCodigo: Int,
    val CID_PaisDescricao: String,
    val CID_Codigo: Int,
    val CID_Descricao: String,
    val CID_EstadoCodigo: String,
    val CID_EstadoDescricao: String,
    val CID_LogradouroCEP: Int,
    val CID_CidadeCodIBGE: Int,
    val CID_PaisIBGE: Int,
    val EMP_EnderecoInscEstadual: String
)

data class Filtros(
    val PDV_PedidoFilial: String,
    val PDV_PedidoCodigo: String,
    val DataEmissaoInicial: String,
    val DataEmissaoFinal: String,
    val PDV_PedidoDataHoraAlteracao: String,
    val PDV_PedidoUsuarioAlteracao: String,
    val CodigoCRM: Int,
    val CodigoCliente: String,
    val FantasiaCliente: String,
    val RazaoSocialCliente: String,
    val CPF_CNPJ: String,
    val CodigoRepresentante: String,
    val CodTransportador: String,
    val CodTransportadorRedespacho: String,
    val Situacao: String,
    val Tipo: Int,
    val Pagina: Int,
    val qtdRegistros: Int
)

data class Mensagem(
    val codigo: String,
    val mensagem: String
)