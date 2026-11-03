# Fluxo de trabalho com Git e GitHub

Como o código do app Bem Me Quer vai do computador de quem programa até a versão usada pela ONG.

---

## 1. As três camadas

```
main      ●──────────────────────●────────  ← PRODUÇÃO: versão usada pela ONG
           \                    ↑ PR dev → main (em lotes, depois de testado)
dev         ●────●──────●───────●────────  ← TESTES: tudo junto, no Firebase de testes
                 ↑      ↑
feat/...    ●────┘      │                    ← DESENVOLVIMENTO: uma funcionalidade por branch
fix/...          ●──────┘
```

| Branch | Para quê | Como o código entra |
|---|---|---|
| `main` | Versão estável, entregue à ONG | **Só** por PR vindo da `dev` |
| `dev` | Juntar as funcionalidades prontas e testar tudo junto | **Só** por PR vindo de `feat/...` ou `fix/...` |
| `feat/...`, `fix/...`, `docs/...`, `chore/...` | Uma tarefa em construção | Commits livres de quem está trabalhando nela |

Cada camada tem o seu Firebase (veja [FIREBASE.md](FIREBASE.md)): `dev` e as branches de tarefa usam o
**`bemmequer-dev`** (build debug); a `main` vai para o **`bemmequer-9ea2a571`** (build release).

---

## 2. Passo a passo de uma tarefa

Exemplo: criar a Agenda.

### 2.1 Começar da `dev` atualizada
Toda tarefa nasce da `dev`, nunca da `main`.

```bash
git checkout dev
git pull
```

### 2.2 Criar a branch da tarefa
```bash
git checkout -b feat/agenda
```

Padrão de nome `tipo/descricao-curta`:

| Tipo | Quando usar | Exemplo |
|---|---|---|
| `feat/` | Funcionalidade nova | `feat/agenda` |
| `fix/` | Correção de erro | `fix/data-relatorio` |
| `docs/` | Só documentação | `docs/guia-firebase` |
| `chore/` | Configuração, ferramentas, organização | `chore/fluxo-dev` |

No Android Studio: nome da branch (no topo) → `dev` → **New Branch from 'dev'...**

### 2.3 Programar e testar
O ▶️ Run usa o Firebase de **testes**: pode criar contas e dados à vontade.

### 2.4 Commit (vários, pequenos)
```bash
git status
git add .
git commit -m "feat: adiciona a tela da agenda"
```

- Confira o `git status` antes do `add` para não incluir arquivos por engano.
- Cada commit deve **compilar sozinho** e tratar de **um assunto**.
- Mensagem no padrão [Conventional Commits](https://www.conventionalcommits.org/pt-br/): `tipo: o que mudou`,
  com os mesmos tipos da tabela acima (`feat`, `fix`, `docs`, `chore`, além de `build`, `ci`, `refactor`, `test`).

### 2.5 Push
```bash
git push -u origin feat/agenda
```

O `-u` só é necessário no primeiro push da branch. Depois, basta `git push`.

### 2.6 Abrir o Pull Request **para a `dev`**
No GitHub: botão **Compare & pull request** e conferir as setas no topo:

```
base: dev   ←   compare: feat/agenda
```

> ⚠️ O erro mais comum é deixar `base: main`. A `base` de uma tarefa é **sempre** `dev`.

Ou pelo terminal:

```bash
gh pr create --base dev --title "Adiciona a tela da agenda"
```

Na descrição: o que mudou, por quê e como foi testado (checklist).

### 2.7 CI, correções e merge
1. O GitHub Actions roda o build, os testes do app e os testes das regras do Firestore.
2. Deu ❌? Corrija **na mesma branch**, faça commit e push. O PR se atualiza sozinho.
3. Com ✅, clique em **Merge pull request** e depois em **Delete branch**.

### 2.8 Atualizar o computador
```bash
git checkout dev
git pull
git branch -d feat/agenda
```

---

## 3. Levar a `dev` para a produção (`main`)

Em lotes: por exemplo, antes de uma apresentação ou de uma entrega para a ONG.

1. Testar a `dev` inteira no app (build debug, Firebase de testes).
2. Abrir o PR `base: main ← compare: dev`.
3. Testar o **build release** (Firebase real).
4. Merge na `main`.
5. Se as regras mudaram, publicar em produção (pasta `firebase`): `npm run deploy:regras:producao`.

---

## 4. Proteção das branches (regras do GitHub)

As branches `main` e `dev` têm um **ruleset** no GitHub (Settings → Rules → Rulesets) que vale para todos,
inclusive administradores:

| Regra | Efeito |
|---|---|
| Exigir Pull Request | `git push` direto é recusado; tudo entra por PR |
| Exigir o CI ✅ | O merge fica bloqueado enquanto "Build e testes do app" ou "Testes das regras do Firestore" falharem |
| Bloquear force push | Ninguém reescreve o histórico da branch |
| Bloquear exclusão | Ninguém apaga a `main` ou a `dev` |

Não é exigida aprovação de outra pessoa, porque o GitHub não deixa o autor aprovar o próprio PR.
Quando o grupo tiver mais de uma pessoa revisando, ative "Require approvals" no ruleset.

Se aparecer `push declined due to repository rule violations`, é a proteção funcionando: crie uma branch e abra um PR.

---

## 5. Erros comuns

| Situação | O que fazer |
|---|---|
| Comecei a programar na `dev` (ou na `main`) sem criar branch | Antes de commitar: `git checkout -b feat/nome`. As mudanças vão junto |
| Abri o PR com `base: main` | No PR, clique em **Edit** (ao lado do título) e troque a base para `dev` |
| A `dev` andou enquanto eu trabalhava | Na sua branch: `git pull origin dev`, resolva conflitos se houver, faça commit e push |
| O CI falhou | Abra o PR → aba **Checks** → leia o passo com ❌. Corrija na mesma branch |
| Esqueci de baixar o `google-services.json` | O build falha ao processar o Google Services. Veja [FIREBASE.md](FIREBASE.md) |
