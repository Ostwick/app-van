package com.example.pdfvan

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.example.pdfvan.model.PedidoResponse
import com.example.pdfvan.model.Pedido
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object PrinterHelper {

    private const val TAG = "PrinterHelper"

    private fun String.removeAccents(): String {
        val normalized = java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
        // This regex removes all characters in the "Combining Diacritical Marks" block
        return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(normalized, "")
    }

    fun printPedido(context: Context, device: android.bluetooth.BluetoothDevice, pedidoResponse: PedidoResponse) {
        val connection = BluetoothConnection(device)
        var totalDescontos = 0f

        // Criar um formatador de números para duas casas decimais com vírgula
        val decimalFormatSymbols = DecimalFormatSymbols(Locale("pt", "BR"))
        decimalFormatSymbols.decimalSeparator = ','
        decimalFormatSymbols.groupingSeparator = '.'
        val formatter = DecimalFormat("#,##0.00", decimalFormatSymbols)

        try {
            connection.connect()

            val pedido = pedidoResponse.Pedidos.firstOrNull() ?: run {
                Toast.makeText(context, "Pedido inválido ou não encontrado", Toast.LENGTH_SHORT).show()
                return
            }

            val endereco = pedido.PDV_PedidoEnderecos.firstOrNull()
            val produtos = pedido.PedidoProdutos
            val descontoPedido = pedido.PDV_PedidoDescontoValor.replace(",", ".").toFloatOrNull() ?: 0f
            totalDescontos += descontoPedido

            val produtosFormatados = produtos.joinToString("\n") { produto ->
                val codigo = produto.PDV_PedidoItemProduto.padStart(8, '0')
                val nome = produto.PDV_PedidoItemProdutoNome
                val qtd = (produto.PDV_PedidoItemQtdPedida.toFloatOrNull() ?: 0f).let { formatter.format(it) }
                val unit = (produto.PDV_PedidoItemValorUnitario.toFloatOrNull() ?: 0f).let { formatter.format(it) }
                val descontoValor = produto.PDV_PedidoItemDescontoValor.replace(",", ".").toFloatOrNull() ?: 0f
                val desconto = formatter.format(descontoValor)
                totalDescontos += descontoValor


                // Calcular IPI e ST para este produto
                var ipiProduto = 0f
                var stProduto = 0f
                for (tax in produto.PedidoItemImposto) {
                    val valor = tax.PDV_PedidoItemIValor.replace(",", ".").toFloatOrNull() ?: 0f
                    when (tax.PDV_PedidoItemIImpostoFiscalDescricao.uppercase()) {
                        "IPI" -> ipiProduto += valor
                        "ICMS-ST" -> stProduto += valor // Usamos "ICMS-ST" conforme o JSON
                    }
                }

                // Somar IPI e ST ao valor total do item
                val valorTotalItemOriginal = produto.PDV_PedidoItemValorTotal.replace(",", ".").toFloatOrNull() ?: 0f
                val valorTotalComImpostos = valorTotalItemOriginal + ipiProduto + stProduto
                val totalFormatado = formatter.format(valorTotalComImpostos)


                "$codigo - $nome  \nQtd: $qtd  Unit: $unit  Desc: $desconto  Total: $totalFormatado\n................................................"
            }

            val impostosCalculados = calcularImpostosTotais(pedido, formatter) // Passa o formatador
            val stTotal = impostosCalculados.st
            val demaisImpostosTotal = impostosCalculados.demaisImpostos

            val finalText = """<b>
[L]PEDIDO: ${pedido.PDV_PedidoCodigo}               ${pedido.PDV_PedidoDataEmissao}
[L]Empresa: ${pedido.PDV_PedidoEmpCodigo} - ${pedido.PDV_PedidoEmpDescricao}
[L]Fantasia: ${pedido.PDV_PedidoEmpFantasia}
[L]Endereco: ${endereco?.EMP_EnderecoLogradouro ?: ""}, ${endereco?.EMP_EnderecoNumero ?: ""}
[L]Bairro: ${endereco?.EMP_EnderecoBairro ?: ""} - CEP: ${endereco?.CID_LogradouroCEP?.toString() ?: ""}
[L]Cidade: ${endereco?.CID_Descricao ?: ""} - ${endereco?.CID_EstadoCodigo ?: ""}
[C]************************************************
[L]CODIGO     DESCRICAO
[L]QTD     VL. UNIT   DESC   VL. TOTAL

$produtosFormatados

[C]************************************************
[C]TOTAIS:
[L]Total: ${formatter.format(pedido.PDV_PedidoValorTotal.toFloatOrNull() ?: 0f)}
[L]Frete: ${formatter.format(pedido.PDV_PedidoFreteValor.toFloatOrNull() ?: 0f)}
[L]Descontos: ${formatter.format(totalDescontos)}
[L]ST: $stTotal
[L]Demais Impostos: $demaisImpostosTotal
[L]Pagamento: ${pedido.PDV_PedidoCondicaoPgtoDescricao}
[L]Obs: ${pedido.PDV_PedidoObs}

[C]________________________________________________
[C]Assinatura do Cliente</b>
            """.trimIndent()

            Log.d(TAG, "Texto formatado para impressão:\n$finalText")

            val printer = EscPosPrinter(connection, 203, 48f, 32)
            printer.printFormattedText(finalText.removeAccents())

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

    data class ImpostosTotais(
        val st: String,
        val demaisImpostos: String
    )

    // A função agora recebe um objeto Pedido e o formatador de números
    private fun calcularImpostosTotais(pedido: Pedido, formatter: DecimalFormat): ImpostosTotais {
        val totalIPI = pedido.TotalIPI.replace(",", ".").toFloatOrNull() ?: 0f
        val totalPIS = pedido.TotalPIS.replace(",", ".").toFloatOrNull() ?: 0f
        val totalCOFINS = pedido.TotalCOFINS.replace(",", ".").toFloatOrNull() ?: 0f
        val totalICMS = pedido.TotalICMS.replace(",", ".").toFloatOrNull() ?: 0f
        val totalST = pedido.TotalST.replace(",", ".").toFloatOrNull() ?: 0f

        // ST é separado
        val stFormatado = formatter.format(totalST)

        // Demais impostos somados (IPI + PIS + COFINS + ICMS)
        val demaisImpostos = totalIPI
        val demaisImpostosFormatado = formatter.format(demaisImpostos)

        return ImpostosTotais(stFormatado, demaisImpostosFormatado)
    }
}