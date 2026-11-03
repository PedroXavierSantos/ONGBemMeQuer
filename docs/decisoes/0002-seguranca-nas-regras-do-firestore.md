# 0002. Segurança nas regras do Firestore, testadas no emulador

- **Status:** Aceita
- **Data:** 2026-10-25

## Contexto
O app roda no celular das pessoas e pode ser modificado; qualquer um pode falar com o banco sem usar o app.
O cadastro grava `tipo: "paciente"` pelo próprio app, então, sem proteção no servidor, alguém poderia gravar
`tipo: "funcionario"` e ganhar acesso de funcionário. O app guarda dados de saúde de crianças (dados sensíveis
pela LGPD).

## Decisão
- Toda permissão é decidida nas regras do Firestore (`firebase/firestore.rules`). O que não é liberado é negado.
- As regras validam quem pode ler e gravar e também o formato dos dados (campos permitidos, tamanhos, tipos).
- Cada regra tem testes automáticos em `firebase/tests/`, que rodam no emulador local e no CI.
- Regras e testes entram no mesmo Pull Request da funcionalidade que grava os dados.

## Consequências
- A proteção vale mesmo que o app seja adulterado.
- Cada coleção nova exige escrever regras e testes antes de funcionar, o que torna cada entrega um pouco maior.
- Consultas da família precisam filtrar pelo próprio UID; sem o filtro, o servidor nega a consulta inteira.

## Alternativas consideradas
- **Validar só no app:** não protege contra um app modificado.
- **Servidor próprio (API) entre o app e o banco:** mais flexível, mas exige hospedagem, custo e manutenção
  que o projeto não tem hoje.
