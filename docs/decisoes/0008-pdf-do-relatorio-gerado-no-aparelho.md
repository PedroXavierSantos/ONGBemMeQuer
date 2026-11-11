# 0008. PDF do relatório gerado no aparelho, sem arquivo guardado

- **Status:** Aceita
- **Data:** 2026-11-10

## Contexto
A família precisa receber o relatório diário como um documento organizado (com o logo da ONG, o nome do
paciente, a data e o profissional), que possa ser salvo, impresso ou enviado, e não como uma mensagem solta.
O projeto não pode ter nenhum custo. Guardar arquivos no Cloud Storage do Firebase exige o plano pago (Blaze).

## Decisão
- O relatório continua sendo **só texto** na coleção `relatorios` do Firestore
  ([ADR-0004](0004-relatorios-imutaveis.md)). Nada muda nos dados nem nas regras de segurança.
- O PDF é **montado na hora, no celular**, a partir do objeto `Relatorio`, com a classe
  `android.graphics.pdf.PdfDocument`, que já faz parte do Android (`util/RelatorioPdf`).
- O arquivo é gravado no **cache do app** e entregue a outros apps pela folha de compartilhamento, por meio de
  um `FileProvider` que libera apenas a pasta `relatorios_pdf` do cache, somente para leitura.

## Consequências
- Custo zero: sem Cloud Storage, sem servidor e sem biblioteca nova.
- O app não pede permissão de armazenamento: o cache é uma pasta particular do app.
- O PDF não é um documento "oficial" guardado pela ONG: ele é refeito a cada toque. Como o relatório é imutável,
  o conteúdo é sempre o mesmo; só a data do rodapé ("gerado em") muda.
- Mudar o visual do documento vale para todos os relatórios, inclusive os antigos, sem migrar nada.
- O `PdfDocument` é uma folha em branco: a quebra de linha e de página é feita pelo app (`util/QuebraDeTexto`,
  com testes unitários). O desenho do PDF depende do Android e é testado no emulador
  (`RelatorioPdfInstrumentedTest`).
- Os PDFs do cache com mais de um dia são apagados na geração seguinte.

## Alternativas consideradas
- **Anexar um PDF pronto e guardar no Cloud Storage:** exigiria o plano Blaze, novas regras de segurança para os
  arquivos e uma tela de envio para o funcionário.
- **Gerar o PDF num servidor (Cloud Functions):** também exige o plano Blaze.
- **Biblioteca de PDF (iText, PDFBox):** mais recursos, mas aumenta o app e algumas têm licença paga para uso
  comercial; o `PdfDocument` é suficiente para um documento de texto.
- **Salvar direto na pasta Downloads:** exigiria permissão de armazenamento em versões antigas do Android; a
  folha de compartilhamento já oferece salvar, imprimir ou enviar, conforme os apps instalados.
