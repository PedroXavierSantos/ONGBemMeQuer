# 0001. Login único, com o papel da pessoa no Firestore

- **Status:** Aceita
- **Data:** 2026-10-25

## Contexto
O protótipo aprovado pela ONG tem duas telas de login: uma para famílias e outra para funcionários, com CPF.
O app precisa mostrar telas diferentes para cada grupo, e um funcionário não pode ser criado por qualquer pessoa.

## Decisão
- Uma única tela de login para todos, com e-mail e senha (Firebase Authentication).
- O papel fica no campo `tipo` do documento `usuarios/{uid}`: `paciente` ou `funcionario`.
- Depois do login, o app lê o `tipo` e abre a área correspondente.
- Famílias se cadastram pelo app, sempre como `paciente`. Contas de funcionário são criadas pela administração
  no console.
- O CPF não é pedido no login.

## Consequências
- Menos código: uma tela de login para manter e testar.
- O roteamento por tipo é só conveniência; a proteção real depende das regras do Firestore (ADR 0002).
- Criar um funcionário exige dois passos manuais no console (conta e documento).
- O papel é lido do Firestore a cada login, uma leitura a mais.

## Alternativas consideradas
- **Duas telas de login:** a tela não protege nada, porque a conta de uma família funciona em qualquer tela.
  Duplica código sem ganho de segurança.
- **CPF como segunda informação no login:** CPF não é segredo e aumentaria os dados pessoais guardados (LGPD).
- **Custom Claims do Firebase (papel no token):** mais robusto, mas exige servidor (Cloud Functions, plano pago).
  Pode substituir esta decisão no futuro.
