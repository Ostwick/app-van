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

                        val pedido = "Pedido: ${lines.getOrNull(42)} : ${lines.getOrNull(43)}"

                        // Encontrar a linha com "Total dos Itens"
                        val indexTotalItens = lines.indexOfFirst { it.contains("Total dos Itens") }
                        val indexFimProdutos = if (indexTotalItens >= 3) indexTotalItens - 3 else 44

                        // Produtos formatados
                        val produtosBrutos = lines.drop(44).take(indexFimProdutos - 44)
                        val produtos = produtosBrutos.mapNotNull { formatarProduto(it) }.joinToString("\n")

                        val totalBruto = lines.getOrNull(indexTotalItens) ?: ""
                        val descontoGeral = lines.getOrNull(indexTotalItens + 2) ?: ""
                        val ipi = lines.getOrNull(indexTotalItens + 3)?.drop(16) ?: ""
                        val totalLiquido = lines.getOrNull(indexTotalItens + 4)?.drop(12) ?: ""

                        val linha55 = lines.getOrNull(indexTotalItens + 6) ?: ""
                        val valores55 = linha55.split(",")
                        val difal = valores55.getOrNull(0)?.trim() ?: ""
                        val qtdTotal = valores55.getOrNull(1)?.substring(2)?.trim() ?: ""
                        val icms = valores55.getOrNull(0) + "," + valores55.getOrNull(1)?.take(2)

                        val linha57 = lines.getOrNull(indexTotalItens + 8) ?: ""
                        val st = linha57.split(",").getOrNull(2)?.substring(2)?.trim() ?: ""

                        val finalText = """
[C]<b>STEELBRAS PEDIDO</b>
[L]Empresa: $empresa
[L]Nome: $nome
[L]Fantasia: $fantasia
[L]Endereço: $endereco
[L]Bairro: $bairro
[L]CEP: $cep
[L]Cidade: $cidade
[L]CNPJ: $cnpj
[L]Fone: $fone
[L]E-mail: $email

[L]Banco: $banco
[L]Pagamento: $pagamento
[L]Data: $data
[L]Representante: $representante

[C]----------------------------
[L]$pedido
[L]Produtos:
$produtos
[C]----------------------------
[L]Qtd Total: $qtdTotal
[L]Total Bruto: $totalBruto
[L]Desconto: $descontoGeral
[L]IPI: $ipi
[L]ICMS: $icms
[L]Difal: $difal
[L]ST: $st
[L]Valor Total: $totalLiquido

[C]_____________________________
[C]Assinatura do Cliente
                        """.trimIndent()

                        Log.d("PrinterHelper", "Texto formatado para impressão:\n$finalText")

                        val printer = EscPosPrinter(connection, 203, 48f, 32)
                        printer.printFormattedText(finalText)

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
        if (!linha.contains("-")) return null

        return try {
            val partes = linha.split(" ")
            val qtd = partes[0]
            val unit = partes[1]
            val total = partes[2]
            val ipiRaw = partes[3]

            val ipi = ipiRaw.replace(",", ".").toFloatOrNull()?.let {
                String.format("%.2f", it).replace(".", ",")
            } ?: ipiRaw

            val depoisDoIPI = linha.substringAfter(ipiRaw).trim()

            val regexCodigo = Regex("(\\d{8,})\\s*-")
            val match = regexCodigo.find(depoisDoIPI)
            val codigo = match?.groups?.get(1)?.value?.takeLast(8)?.padStart(8, '0') ?: "????????"

            val nome = depoisDoIPI.substringAfter("-").trim()
                .split(Regex("\\d+,\\d{2}")).firstOrNull()?.trim() ?: "Sem Nome"

            val valores = Regex("(\\d+,\\d{2})").findAll(depoisDoIPI).map { it.value }.toList()

            val icms = valores.getOrNull(0) ?: "0,00"
            val desconto = valores.getOrNull(1) ?: "0,00"
            val difal = valores.getOrNull(2) ?: "0,00"
            val st = valores.getOrNull(3) ?: "0,00"

            "$codigo - $nome Qtd: $qtd Unit: $unit Desconto: $desconto Total: $total Difal: $difal ICMS: $icms IPI: $ipi ST: $st"
        } catch (e: Exception) {
            null
        }
    }
}
