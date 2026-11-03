# 0003. Abas do paciente como Fragments numa única Activity

- **Status:** Aceita
- **Data:** 2026-10-29

## Contexto
O protótipo tem uma barra inferior com cinco abas para as famílias: Início, Agenda, Notícias, Doações e Perfil.
A troca entre abas precisa ser rápida e o botão voltar precisa se comportar como nos apps conhecidos.

## Decisão
- `mainPacienteActivity` é uma moldura com a `BottomNavigationView` e um `frame_container`.
- Cada aba é um Fragment trocado dentro do `frame_container`.
- Abas ainda não implementadas usam o `EmBreveFragment`, que recebe título e ícone como argumentos.
- O botão voltar leva ao Início a partir de qualquer aba e fecha o app quando já está no Início.

## Consequências
- Uma aba nova é só um Fragment novo ligado a um item do menu.
- O estado da aba aberta sobrevive à rotação da tela (o Android restaura o Fragment).
- Fragments exigem cuidado com respostas assíncronas: conferir `isAdded()` antes de mexer na tela.

## Alternativas consideradas
- **Uma Activity por aba:** cada troca abriria uma janela nova, mais lenta, e empilharia telas no voltar.
- **Navigation Component:** padrão recomendado para apps maiores; pode ser adotado quando a navegação crescer.
