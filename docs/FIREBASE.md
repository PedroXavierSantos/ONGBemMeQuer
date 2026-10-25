# Firebase no app Bem Me Quer

Este documento explica como o app usa o Firebase, como configurar o projeto no console
e como testar tudo localmente sem mexer nos dados reais.

Projeto Firebase: **`bemmequer-9ea2a571`**

---

## 1. Como funciona

O app usa dois serviços do Firebase:

| Serviço | Para que serve no app |
|---|---|
| **Authentication** | Guarda as contas (e-mail e senha). Cada conta tem um **UID**, um identificador único. |
| **Cloud Firestore** | Banco de dados. Guarda o **perfil** de cada conta: tipo (paciente/funcionário), nomes, telefone etc. |

O Authentication só sabe "quem é" a pessoa. **O que ela pode fazer** depende do perfil no Firestore.

### Fluxo do cadastro
1. Tela de cadastro 1: valida nome, telefone, e-mail e senha.
2. Tela de cadastro 2: valida o nome do paciente e então:
   - cria a conta no **Authentication**;
   - grava o perfil em **`usuarios/{uid}`** no Firestore, com `tipo: "paciente"`;
   - se a gravação falhar, apaga a conta recém-criada para a pessoa poder tentar de novo.

### Fluxo do login
1. A pessoa entra com e-mail e senha (ou já está logada: entrada automática).
2. O app lê `usuarios/{uid}` e olha o campo `tipo`:
   - `"funcionario"` → tela do funcionário;
   - qualquer outro valor (ou sem perfil) → tela do paciente.

### Onde fica cada coisa no código
| Arquivo | O que faz |
|---|---|
| `app/.../auth/AuthManager.java` | Login, cadastro, logout e tradução dos erros do Authentication |
| `app/.../data/UsuarioRepository.java` | Leitura e gravação dos perfis em `usuarios/{uid}` |
| `app/.../BemMeQuerApp.java` | Liga o app aos emuladores locais (só em builds de teste) |
| `firebase/firestore.rules` | **Regras de segurança** do banco |
| `firebase/tests/` | Testes automáticos das regras |

---

## 2. Regras de segurança (o mais importante)

O app roda no celular das pessoas, então **qualquer um pode alterar o app e mandar o que quiser
para o banco**. Quem protege os dados de verdade são as **regras do Firestore**
(`firebase/firestore.rules`), que rodam nos servidores do Google.

Regras atuais da coleção `usuarios`:

| Ação | Quem pode |
|---|---|
| Ler um perfil | O próprio usuário, ou um funcionário |
| Criar um perfil | Só o próprio usuário, **sempre como `paciente`**, com o e-mail da própria conta e só com os campos previstos |
| Editar o perfil | Só o próprio usuário, e **nunca** os campos `tipo`, `email` e `criadoEm` |
| Excluir | Ninguém pelo app (só a administração, pelo console) |

Isso impede, por exemplo, que alguém se cadastre como funcionário ou leia os dados de outra família.
Qualquer coleção nova (relatórios, agenda, doações...) começa **bloqueada** até ganhar regras próprias.

> Toda vez que uma nova funcionalidade gravar algo no banco, crie as regras **e** os testes dela em
> `firebase/` no mesmo Pull Request.

---

## 3. Configuração no console do Firebase (fazer uma vez)

Acesse <https://console.firebase.google.com> com a conta dona do projeto.

### 3.1 Authentication
1. **Authentication → Sign-in method → Adicionar provedor → E-mail/senha** → ativar → Salvar.
2. (Recomendado) **Authentication → Settings → Política de senha**: exigir no mínimo 6 caracteres
   (o mesmo que o app já valida).
3. Confira se **Proteção contra enumeração de e-mail** está ativada (vem ativada por padrão).

### 3.2 Cloud Firestore
1. **Firestore Database → Criar banco de dados**.
2. Edição **Standard**, ID do banco **`(default)`**.
3. Local: **`southamerica-east1` (São Paulo)**. ⚠️ **Não dá para mudar depois.**
4. Começar no **modo de produção** (tudo bloqueado). As regras do projeto serão publicadas a seguir.

### 3.3 Publicar as regras de segurança
Pelo terminal (recomendado, publica exatamente o que está testado no repositório):

```bash
cd firebase
npm install
npx firebase login
npm run deploy:regras
```

Alternativa: copiar o conteúdo de `firebase/firestore.rules` para **Firestore Database → Regras** e clicar em **Publicar**.

### 3.4 Criar a conta de um funcionário
Funcionários **não** se cadastram pelo app. A administração cria a conta:

1. **Authentication → Users → Adicionar usuário**: e-mail e senha do funcionário. Copie o **UID** gerado.
2. **Firestore Database → Dados → coleção `usuarios` → Adicionar documento**:
   - ID do documento: **o UID copiado**
   - campos: `tipo` = `funcionario` (string), `email` = e-mail do funcionário (string), `nome` = nome (string)

### 3.5 Plano e custos
O plano gratuito (**Spark**) é suficiente para o app neste momento. Se um dia mudarem para o plano
**Blaze**, configurem um **alerta de orçamento** em *Uso e faturamento*.

---

## 4. Testar localmente com os emuladores (sem tocar nos dados reais)

O Firebase tem **emuladores** que rodam no seu computador: um Authentication e um Firestore "de mentira",
com as mesmas regras. Ideal para desenvolver e para testar cadastros à vontade.

Pré-requisitos: Node.js e Java instalados.

### 4.1 Testes automáticos das regras
```bash
cd firebase
npm install
npm run test:regras
```

### 4.2 Rodar o app ligado aos emuladores
1. Em um terminal, suba os emuladores (deixe aberto):
   ```bash
   cd firebase
   npm run emuladores
   ```
   Painel para ver contas e dados: <http://127.0.0.1:4000>
2. Em outro terminal, instale o app no emulador do Android apontando para eles:
   ```bash
   ./gradlew installDebug -PusarEmuladorFirebase=true
   ```
   Sem o `-PusarEmuladorFirebase=true` (ou no Run do Android Studio), o app usa o **Firebase real**.
3. Para criar um funcionário de teste, use o painel (<http://127.0.0.1:4000>): crie o usuário em
   *Authentication* e o documento `usuarios/{uid}` com `tipo: funcionario` em *Firestore*.

Os dados dos emuladores são apagados quando eles são fechados.

> **Windows:** se aparecer `port taken` ao subir os emuladores, um emulador anterior ficou aberto.
> Feche o processo `java` que usa a porta 8080 (Gerenciador de Tarefas) e tente de novo.

---

## 5. Modelo de dados planejado (próximas funcionalidades)

Proposta para as telas do protótipo. Cada coleção ganha regras e testes quando for implementada.

| Coleção | Conteúdo | Quem lê / escreve |
|---|---|---|
| `usuarios/{uid}` | Perfil (paciente ou funcionário) | ✅ já implementado |
| `relatorios/{id}` | Relatório diário: paciente, profissional, relato, data, local | Funcionário escreve; **só o responsável daquele paciente** lê |
| `agendamentos/{id}` | Serviço, profissional, data, horário, status | Paciente cria/cancela os seus; funcionário confirma |
| `triagens/{id}` | Pré-cadastro de quem quer entrar no projeto | Qualquer pessoa cria; só funcionário lê |
| `pontos/{uid}/registros/{id}` | Ponto do funcionário (entrada, almoço, intervalo, saída, localização, selfie) | Só o próprio funcionário |
| `noticias/{id}` | Notícias da ONG | Todos leem; só funcionário escreve |
| `campanhas/{id}` | Abas sazonais (Páscoa, Natal...) com data de início/fim | Todos leem; só funcionário escreve |

**Fotos** (selfie do ponto, imagens de notícias) devem ir para o **Cloud Storage**, guardando no
Firestore só o caminho do arquivo.

### LGPD
O app guarda dados de saúde de crianças, que são **dados sensíveis**. Por isso:
- só coletar o necessário;
- manter as regras restritivas (cada família vê só os próprios dados);
- ter uma **Política de Privacidade** e o aceite dos **Termos de Uso** no cadastro (já previsto no protótipo).
