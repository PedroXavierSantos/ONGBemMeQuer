# Decisões de arquitetura

Cada arquivo registra **por que** uma decisão importante foi tomada (ADR, *Architecture Decision Record*).
Uma ADR aceita não é reescrita: se a decisão mudar, crie uma nova que a substitua e marque a antiga como
"Substituída por ADR-XXXX".

| Nº | Decisão | Status | Data |
| --- | --- | --- | --- |
| [0001](0001-login-unico-com-papeis.md) | Login único, com o papel da pessoa no Firestore | Aceita | 2026-10-25 |
| [0002](0002-seguranca-nas-regras-do-firestore.md) | Segurança nas regras do Firestore, testadas no emulador | Aceita | 2026-10-25 |
| [0003](0003-abas-do-paciente-com-fragments.md) | Abas do paciente como Fragments numa única Activity | Aceita | 2026-10-29 |
| [0004](0004-relatorios-imutaveis.md) | Relatórios diários imutáveis, lidos pelo `pacienteUid` | Aceita | 2026-10-30 |
| [0005](0005-ambientes-de-testes-e-producao.md) | Dois projetos Firebase: testes e produção | Aceita | 2026-11-02 |
| [0006](0006-fluxo-main-dev-tarefas.md) | Fluxo de branches `main` → `dev` → tarefas, com proteção | Aceita | 2026-11-02 |
| [0007](0007-documentacao-enxuta.md) | Documentação enxuta no repositório | Aceita | 2026-11-02 |

## Como registrar uma decisão

Copie o modelo abaixo para `NNNN-titulo-curto.md` (próximo número), preencha e adicione a linha na tabela,
no mesmo Pull Request da mudança.

```markdown
# NNNN. Título da decisão

- **Status:** Proposta | Aceita | Substituída por ADR-XXXX
- **Data:** AAAA-MM-DD

## Contexto
O problema e as restrições que levaram à decisão.

## Decisão
O que foi decidido, em poucas frases.

## Consequências
O que melhora, o que piora e o que passa a ser obrigatório.

## Alternativas consideradas
As outras opções e por que não foram escolhidas.
```
