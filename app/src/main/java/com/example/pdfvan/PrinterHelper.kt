package com.example.pdfvan

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

object PrinterHelper {

    fun printPages(context: Context, uri: Uri, page: Int? = null) {
        val pairedPrinters = BluetoothPrintersConnections().list
        if (pairedPrinters.isNullOrEmpty()) {
            Toast.makeText(context, "Nenhuma impressora Bluetooth encontrada.", Toast.LENGTH_SHORT).show()
            return
        }

        val printerNames = pairedPrinters.map {
            val name = it.device.name ?: "Sem nome"
            val mac = it.device.address ?: "Sem endereço"
            "$name\n$mac"
        }.toTypedArray()

        AlertDialog.Builder(context)
            .setTitle("Selecione uma impressora")
            .setItems(printerNames) { _, which ->
                val connection: DeviceConnection = pairedPrinters[which]

                try {
                    context.contentResolver.openInputStream(uri).use { inputStream ->
                        val document = PDDocument.load(inputStream)
                        val stripper = PDFTextStripper()

                        if (page != null) {
                            stripper.startPage = page + 1
                            stripper.endPage = page + 1
                        }

                        val text = stripper.getText(document)
                        document.close()
                        Log.d("PrinterHelper", "Texto original do PDF:\n$text")

                        val lines = text.lines().filter { it.isNotBlank() }

                        // Mapeamento fixo atualizado
                        val empresa = lines.getOrNull(9) ?: ""
                        val nome = lines.getOrNull(1) ?: ""
                        val fantasia = lines.getOrNull(2) ?: ""
                        val endereco = lines.getOrNull(3) ?: ""
                        val bairro = lines.getOrNull(16) ?: ""
                        val cep = lines.getOrNull(14) ?: ""
                        val cidade = lines.getOrNull(4) ?: ""
                        val cnpj = lines.getOrNull(5) ?: ""
                        val fone = lines.getOrNull(6) ?: ""
                        val email = lines.getOrNull(13) ?: ""

                        val banco = lines.getOrNull(7) ?: ""
                        val pagamento = lines.getOrNull(8) ?: ""
                        val data = lines.getOrNull(10) ?: ""
                        val representante = lines.getOrNull(12) ?: ""

                        val pedido = "${lines.getOrNull(42)}"

                        // Encontrar a linha com "Total dos Itens"
                        val indexTotalItens = lines.indexOfFirst { it.contains("Total dos Itens") }
                        val indexFimProdutos = if (indexTotalItens >= 3) indexTotalItens - 3 else 44

                        // Produtos formatados
                        val produtosBrutos = lines.drop(44).take(indexFimProdutos - 44)
                        val produtos = produtosBrutos.mapNotNull { formatarProduto(it) }.joinToString("\n")

                        val totalBruto = lines.getOrNull(indexTotalItens - 3) ?: ""
                        val descontoGeral = lines.getOrNull(indexTotalItens - 1) ?: ""
                        val ipiLinha = lines.getOrNull(indexTotalItens) ?: ""
                        val ipi = ipiLinha.substringAfter("Total dos Itens").trim().ifEmpty { "0,00" }
                        val totalLiquido = lines.getOrNull(indexTotalItens + 1)?.drop(12) ?: ""

                        val linhaICMS_Qtd = lines.getOrNull(indexTotalItens + 3) ?: ""
                        val posVirgula = linhaICMS_Qtd.indexOf(',')

                        val icms = if (posVirgula != -1 && linhaICMS_Qtd.length > posVirgula + 2) {
                            linhaICMS_Qtd.substring(0, posVirgula + 3).trim()
                        } else "0,00"

                        val qtdTotal = if (posVirgula != -1 && linhaICMS_Qtd.length > posVirgula + 3) {
                            linhaICMS_Qtd.substring(posVirgula + 3).trim()
                        } else ""

                        val linhaDifalST = lines.getOrNull(indexTotalItens + 5) ?: ""
                        val numerosVirgula = Regex("(\\d+,\\d{2})").findAll(linhaDifalST).map { it.value }.toList()

                        val difal = numerosVirgula.getOrNull(1) ?: "0,00"
                        val st = numerosVirgula.getOrNull(2) ?: "0,00"

                        Log.d("PrinterHelper", "IPI: $ipi")
                        Log.d("PrinterHelper", "ICMS: $icms")
                        Log.d("PrinterHelper", "Difal: $difal")
                        Log.d("PrinterHelper", "ST: $st")
                        val impostos = somarImpostos(ipi, icms, difal, st)

                        val finalText = """
[L]PEDIDO: $pedido    $data
[L]Nome: $nome
                        
[L]CODIGO     QTD     VL. TOTAL
[L]DESCRICAO

$produtos
                        
[C]----------------------------
[C]<b>TOTAIS:</b>
[L]Qtd Total: $qtdTotal
[L]Total Bruto: $totalBruto
[L]Desconto: $descontoGeral
[L]Impostos: $impostos
[L]Valor Total: $totalLiquido
                        
[C]<b>FORMAS DE PAGAMENTO:</b>
$pagamento
                        
[C]_____________________________
[C]Assinatura do Cliente
""".trimIndent()

                        Log.d("PrinterHelper", "Texto formatado para impressão:\n$finalText")

                        val printer = EscPosPrinter(connection, 203, 48f, 32)
                        //printer.printFormattedText(finalText)

                        Toast.makeText(context, "Impressão concluída com sucesso", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(context, "Erro ao imprimir: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("PrinterHelper", "Erro ao imprimir: ${e.message}", e)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun formatarProduto(linha: String): String? {
        return try {
            val partes = linha.split(" ")
            val qtd = partes.getOrNull(0) ?: return null
            val unit = partes.getOrNull(1) ?: return null
            val total = partes.getOrNull(2) ?: return null

            // Captura o IPI usando regex
            val ipiRegex = Regex("\\d+,\\d{2}")
            val ipiMatch = ipiRegex.find(linha, startIndex = total.length + unit.length + qtd.length)
            val ipi = ipiMatch?.value ?: "0,00"

            // Captura o código do produto logo após o IPI
            val depoisDoIPI = linha.substringAfter(ipi).trim()
            val codigoRegex = Regex("(\\d{8,})\\s*-")
            val codigoMatch = codigoRegex.find(depoisDoIPI)
            val codigo = codigoMatch?.groups?.get(1)?.value?.takeLast(8)?.padStart(8, '0') ?: "????????"

            // Extrai o nome do produto entre o traço e os próximos valores numéricos
            val nome = depoisDoIPI.substringAfter("-").trim()
                .split(Regex("\\d+,\\d{2}")).firstOrNull()?.trim() ?: "Sem Nome"

            "$codigo      $qtd      $total\n$nome"
        } catch (e: Exception) {
            Log.e("PrinterHelper", "Erro ao formatar produto: ${e.message}", e)
            null
        }
    }

    private fun somarImpostos(ipi: String?, icms: String?, difal: String?, st: String?): String {
        fun parse(valor: String?) = valor?.replace(",", ".")?.toFloatOrNull() ?: 0f
        val total = parse(ipi) + parse(icms) + parse(difal) + parse(st)
        return "%.2f".format(total).replace(".", ",")
    }
}
