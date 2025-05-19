package com.example.pdfvan

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.example.pdfvan.model.PedidoResponse

object PrinterHelper {

    private const val TAG = "PrinterHelper"

    fun printPedido(context: Context, device: android.bluetooth.BluetoothDevice, pedidoResponse: PedidoResponse) {
        val connection = BluetoothConnection(device)

        try {
            connection.connect()

            val pedido = pedidoResponse.Pedidos.firstOrNull() ?: run {
                Toast.makeText(context, "Pedido inválido ou não encontrado", Toast.LENGTH_SHORT).show()
                return
            }

            val endereco = pedido.PDV_PedidoEnderecos.firstOrNull()
            val produtos = pedido.PedidoProdutos

            val produtosFormatados = produtos.joinToString("\n") { produto ->
                val codigo = produto.PDV_PedidoItemProduto.padStart(8, '0')
                val nome = produto.PDV_PedidoItemProdutoNome
                val qtd = produto.PDV_PedidoItemQtdPedida
                val unit = produto.PDV_PedidoItemValorUnitario
                val desconto = produto.PDV_PedidoItemDescontoValor
                val total = produto.PDV_PedidoItemValorTotal

                val impostos = produto.PedidoItemImposto.associateBy(
                    { it.PDV_PedidoItemIImpostoFiscalDescricao.uppercase() },
                    { it.PDV_PedidoItemIValor }
                )
                val difal = impostos["DIFAL"] ?: "0,00"
                val icms = impostos["ICMS"] ?: "0,00"
                val ipi = impostos["IPI"] ?: "0,00"
                val st = impostos["ST"] ?: "0,00"

                "$codigo   Qtd: $qtd  Unit: $unit  Desc: $desconto  Total: $total\n$nome"
            }

            // Somar impostos totais de todos os produtos
            val impostosTotais = calcularImpostosTotais(pedido.PedidoProdutos)

            val finalText = """
                [L]PEDIDO: ${pedido.PDV_PedidoCodigo}    ${pedido.PDV_PedidoDataEmissao}
                [L]Empresa: ${pedido.PDV_PedidoEmpDescricao}
                [L]Fantasia: ${pedido.PDV_PedidoEmpFantasia}
                [L]Endereço: ${endereco?.EMP_EnderecoLogradouro ?: ""}, ${endereco?.EMP_EnderecoNumero ?: ""}
                [L]Bairro: ${endereco?.EMP_EnderecoBairro ?: ""} - CEP: ${endereco?.CID_LogradouroCEP ?: ""}
                [L]Cidade: ${endereco?.CID_Descricao ?: ""} - ${endereco?.CID_EstadoCodigo ?: ""}

                [L]CODIGO     QTD     VL. UNIT   DESC   VL. TOTAL
                [L]DESCRICAO

                $produtosFormatados

                [C]----------------------------
                [C]<b>TOTAIS:</b>
                [L]Total: ${pedido.PDV_PedidoValorTotal}
                [L]Frete: ${pedido.PDV_PedidoFreteValor}
                [L]Impostos: ${impostosTotais}
                [L]Pagamento: ${pedido.PDV_PedidoCondicaoPgtoDescricao}
                [L]Obs: ${pedido.PDV_PedidoObs}

                [C]_____________________________
                [C]Assinatura do Cliente
            """.trimIndent()

            Log.d(TAG, "Texto formatado para impressão:\n$finalText")

            val printer = EscPosPrinter(connection, 203, 48f, 32)
            printer.printFormattedText(finalText)

            Toast.makeText(context, "Impressão concluída com sucesso", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao imprimir: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Erro ao imprimir", e)
        } finally {
            try {
                connection.disconnect()
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao desconectar da impressora: ${e.message}")
            }
        }
    }

    private fun calcularImpostosTotais(produtos: List<com.example.pdfvan.model.Product>): String {
        var ipi = 0f
        var icms = 0f
        var difal = 0f
        var st = 0f

        for (produto in produtos) {
            for (tax in produto.PedidoItemImposto) {
                val valor = tax.PDV_PedidoItemIValor.replace(",", ".").toFloatOrNull() ?: 0f
                when (tax.PDV_PedidoItemIImpostoFiscalDescricao.uppercase()) {
                    "IPI" -> ipi += valor
                    "ICMS" -> icms += valor
                    "DIFAL" -> difal += valor
                    "ST" -> st += valor
                }
            }
        }

        val total = ipi + icms + difal + st
        return "%.2f".format(total).replace(".", ",")
    }
}
