# 0006. Fluxo de branches `main` → `dev` → tarefas, com proteção

- **Status:** Aceita
- **Data:** 2026-11-02

## Contexto
Cada funcionalidade era juntada direto na `main`. Duas funcionalidades que funcionam sozinhas podem quebrar
juntas, e o problema só apareceria na versão entregue à ONG.

## Decisão
- `main`: produção. Recebe só Pull Requests vindos da `dev`, em lotes.
- `dev`: testes. Recebe Pull Requests das branches de tarefa (`feat/`, `fix/`, `docs/`, `chore/`).
- Um ruleset do GitHub protege `main` e `dev`: exige Pull Request, exige o CI aprovado ("Build e testes do app"
  e "Testes das regras do Firestore"), bloqueia force push e exclusão. Vale também para administradores.
- O CI roda em todo Pull Request e em cada push na `main` e na `dev`.

## Consequências
- Nenhum código chega às famílias sem passar por Pull Request, CI e testes na `dev`.
- Uma etapa a mais em cada entrega (o PR `dev` → `main`), feita em lotes para não pesar.
- Sem aprovação obrigatória de outra pessoa enquanto só uma pessoa revisa; ativar quando o grupo crescer.
- Se os nomes dos jobs do CI mudarem, o ruleset precisa ser atualizado junto.

## Alternativas consideradas
- **Só `main` com branches de tarefa:** mais simples, mas sem um lugar para testar as funcionalidades juntas.
- **Git Flow completo (com `release/` e `hotfix/`):** mais estrutura do que o tamanho do projeto pede hoje.
