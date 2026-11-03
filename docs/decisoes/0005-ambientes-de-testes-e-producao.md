# 0005. Dois projetos Firebase: testes e produção

- **Status:** Aceita
- **Data:** 2026-11-02

## Contexto
Com a branch `dev` como ambiente de testes (ADR 0006), testar no mesmo Firebase usado pelas famílias criaria
contas e dados falsos no meio dos reais, e publicar regras em teste mudaria a segurança de todos.

## Decisão
- Projeto de testes `bemmequer-dev` e projeto real `bemmequer-9ea2a571`, com a mesma configuração (login por
  e-mail e senha, Firestore em `southamerica-east1`).
- O build debug usa `app/src/debug/google-services.json` (testes); o release usa `app/google-services.json`
  (produção). Os dois ficam fora do Git.
- A Firebase CLI usa o projeto de testes por padrão; publicar em produção exige `npm run deploy:regras:producao`.
- Regras novas são publicadas primeiro em testes e só vão para produção quando a `dev` vai para a `main`.

## Consequências
- O ▶️ Run nunca toca os dados reais.
- Contas de teste (inclusive funcionários) precisam ser criadas no projeto de testes.
- Cada configuração feita num projeto precisa ser repetida no outro.

## Alternativas consideradas
- **Um projeto só, com dados de teste marcados:** risco de misturar dados e de publicar regras não testadas.
- **Só emuladores locais para testes:** não testam a configuração real do console nem permitem testar no celular
  sem o computador.
