# 0004. Relatórios diários imutáveis, lidos pelo `pacienteUid`

- **Status:** Aceita
- **Data:** 2026-10-30

## Contexto
O funcionário registra como foi o atendimento de um paciente, e só a família daquele paciente pode ler. Os
relatórios formam o histórico do acompanhamento e podem ser usados pela ONG para mostrar a evolução da criança.

## Decisão
- Coleção `relatorios`, um documento por relatório, com `pacienteUid` (UID da conta da família), autor, relato,
  `data` e local.
- Leitura: funcionários, ou a família cujo UID é o `pacienteUid`. A lista da família filtra por `pacienteUid` e
  ordena por `data` (índice composto).
- Criação: só funcionário, em nome próprio (`funcionarioUid` igual ao seu UID) e para um paciente existente.
- Edição e exclusão bloqueadas pelo app.

## Consequências
- O histórico é confiável: o que foi entregue à família não muda.
- Um relatório com erro não pode ser corrigido pelo app; a correção é um novo relatório ou uma ação da
  administração no console.
- Cada conta de família corresponde a um paciente. Famílias com mais de uma criança precisarão de uma
  coleção de pacientes no futuro.

## Alternativas consideradas
- **Permitir edição pelo autor:** mais prático, mas a família poderia ler uma versão e depois encontrar outra.
  Pode ser revisto com um histórico de versões.
- **Relatórios dentro do documento do usuário:** um documento cresceria sem limite e não permitiria ordenar e
  paginar a lista.
