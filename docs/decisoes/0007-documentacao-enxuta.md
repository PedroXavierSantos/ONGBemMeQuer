# 0007. Documentação enxuta no repositório

- **Status:** Aceita
- **Data:** 2026-11-02

## Contexto
A documentação cresceu com explicações longas, analogias e soluções de problemas misturadas a comandos e
referências. O repositório não tinha README. Documentação longa fica desatualizada e afasta quem só quer usar
o projeto.

## Decisão
- No repositório, só o necessário para usar, contribuir e operar o projeto:
  `README.md`, `CONTRIBUTING.md`, `.github/pull_request_template.md`, `docs/FIREBASE.md` e `docs/decisoes/`.
- Explicações para aprender ficam fora do repositório, num caderno de estudos da equipe.
- A documentação é atualizada no mesmo Pull Request que muda o que ela descreve, e o que ficar desatualizado
  é apagado.

Baseado em: [GitHub, About READMEs](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-readmes),
[Google, Documentation best practices](https://google.github.io/styleguide/docguide/best_practices.html) e o
método Diátaxis.

## Consequências
- Quem chega ao repositório encontra em minutos o que é o projeto e como rodar.
- O GitHub mostra o link do `CONTRIBUTING.md` e preenche a descrição dos PRs com o template.
- Explicações detalhadas exigem manter um segundo lugar (o caderno de estudos).

## Alternativas consideradas
- **Manter tudo em `docs/`:** misturava tutorial, referência e explicação, e crescia sem controle.
- **Wiki do GitHub:** fica fora do controle de versão do código e não passa por Pull Request.
