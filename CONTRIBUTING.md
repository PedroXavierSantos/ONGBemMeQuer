# Como contribuir

## Branches

| Branch | Papel | Como o código entra |
| --- | --- | --- |
| `main` | Produção: a versão entregue à ONG | Só por Pull Request vindo da `dev` |
| `dev` | Testes: tudo junto, no Firebase de testes | Só por Pull Request vindo de uma branch de tarefa |
| `tipo/descricao` | Uma tarefa em andamento | Commits livres |

As branches `main` e `dev` são protegidas: `git push` direto é recusado, o merge exige o CI aprovado e não é
possível apagá-las nem reescrever o histórico.

## Passo a passo de uma tarefa

```bash
git checkout dev
git pull
git checkout -b feat/agenda
```

Programe e teste com ▶️ Run (o build debug usa o Firebase de **testes**). Depois:

```bash
git add <arquivos>
git commit -m "feat: adiciona a tela da agenda"
git push -u origin feat/agenda
gh pr create --base dev
```

O Pull Request vai sempre para a **`dev`**. Com o CI aprovado, faça o merge e apague a branch.

## Nomes

**Branches:** `feat/` (funcionalidade), `fix/` (correção), `docs/` (documentação), `chore/` (configuração).

**Commits:** [Conventional Commits](https://www.conventionalcommits.org/pt-br/), no formato `tipo: o que mudou`.
Além dos tipos acima: `build`, `ci`, `refactor` e `test`. Um assunto por commit, e cada commit compila sozinho.

## Antes de abrir o Pull Request

```bash
./gradlew testDebugUnitTest lintDebug
```

- Se a mudança grava dados no Firestore, as regras e os testes delas entram **no mesmo PR**
  (`firebase/firestore.rules` e `firebase/tests/`, com `npm run test:regras`).
- Se a mudança é uma decisão importante de arquitetura, registre uma ADR em [docs/decisoes](docs/decisoes/README.md).
- Atualize a documentação que a mudança deixar desatualizada.

## Levar a `dev` para a produção

Em lotes, quando a `dev` estiver estável:

1. Pull Request de `dev` para `main`.
2. Teste do build release (Firebase real).
3. Merge e, se as regras mudaram, `npm run deploy:regras:producao` na pasta `firebase`.
