# Firebase

O app usa **Firebase Authentication** (contas com e-mail e senha) e **Cloud Firestore** (dados). A segurança é
garantida pelas regras em [`firebase/firestore.rules`](../firebase/firestore.rules), que rodam no servidor.

## Ambientes

| Ambiente | Projeto | Build | Branches |
| --- | --- | --- | --- |
| Testes | `bemmequer-dev` | debug (▶️ Run) | `dev` e branches de tarefa |
| Produção | `bemmequer-9ea2a571` | release (entrega à ONG) | `main` |

Cada build lê o seu `google-services.json` (fora do Git):

| Arquivo | Projeto |
| --- | --- |
| `app/src/debug/google-services.json` | Testes |
| `app/google-services.json` | Produção |

Para baixar: console do Firebase → ⚙️ Configurações do projeto → app Android → `google-services.json`.

## Comandos

Na pasta `firebase` (requer Node.js e Java):

| Comando | O que faz |
| --- | --- |
| `npm install` | Instala a Firebase CLI e as bibliotecas de teste |
| `npm run test:regras` | Roda os testes das regras no emulador |
| `npm run emuladores` | Sobe Authentication e Firestore locais (painel em http://127.0.0.1:4000) |
| `npm run deploy:regras` | Publica regras e índices no projeto de **testes** |
| `npm run deploy:regras:producao` | Publica no projeto **real** (só ao levar a `dev` para a `main`) |

O projeto padrão da CLI é o de testes (`firebase/.firebaserc`). Para usar o app com os emuladores:

```bash
./gradlew installDebug -PusarEmuladorFirebase=true
```

No Windows, rode `npm run emuladores` em um terminal separado do emulador do Android: ao ligar, ele derruba
processos do mesmo terminal.

## Coleções e regras

| Coleção | Conteúdo | Ler | Criar | Editar / apagar |
| --- | --- | --- | --- | --- |
| `usuarios/{uid}` | Perfil: `tipo` (`paciente` ou `funcionario`), nomes, telefone, e-mail | O próprio usuário ou funcionário | O próprio, sempre como `paciente` | O próprio, sem mudar `tipo`, `email`, `criadoEm` / ninguém |
| `relatorios/{id}` | Relatório diário: `pacienteUid`, relato, `data`, local, autor | Funcionário ou a família do `pacienteUid` | Só funcionário, em nome próprio | Ninguém |

Toda coleção nova começa bloqueada e ganha regras e testes no mesmo Pull Request que a usa. Consultas que
filtram por um campo e ordenam por outro precisam de índice em `firebase/firestore.indexes.json`.

**Planejadas:** `agendamentos`, `triagens`, `pontos/{uid}/registros`, `noticias` e `campanhas`. Fotos (selfie do
ponto, imagens de notícias) irão para o Cloud Storage, com o caminho do arquivo no Firestore.

## Contas de funcionário

Funcionários não se cadastram pelo app. Em cada projeto (testes ou produção):

1. **Authentication → Users → Adicionar usuário**, com e-mail e senha. Copie o **UID**.
2. **Firestore Database → `usuarios` → Adicionar documento**, com ID igual ao UID e os campos (string):
   `tipo` = `funcionario`, `email` e `nome`.

## Configurar um projeto do zero


1. **Authentication → Sign-in method:** ativar E-mail/senha.
2. **Firestore Database → Criar banco:** edição Standard, ID `(default)`, local `southamerica-east1`
   (não pode ser mudado depois), modo de produção.
3. Publicar as regras com `npm run deploy:regras` (ou `:producao`).
4. Registrar o app Android (pacote `com.app.bemmequer`) e baixar o `google-services.json`.
