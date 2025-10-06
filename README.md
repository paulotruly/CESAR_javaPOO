# Gerenciador de notícias com Java SWT

Este projeto implementa uma aplicação desktop em Java que permite gerenciar notícias de diferentes categorias.  
A interface foi desenvolvida utilizando a biblioteca **Eclipse SWT (Standard Widget Toolkit)**.

---

## Funcionalidades

- Listagem de notícias com exibição da categoria e do conteúdo  
- Adição de novas notícias  
- Edição de notícias existentes  
- Exclusão de notícias (exceto as fixas)  
- Limpeza de todas as notícias não fixas  
- Busca por palavra-chave e filtro por categoria  
- Contador de total de notícias exibidas

---

## Interface

A interface foi construída com **GridLayout** e **GridData**, organizando os componentes em:

- Campo de busca com filtro por categoria  
- Lista rolável de notícias  
- Botões de ação (adicionar, editar, excluir, limpar)
- Contador total de registros

---

## Estrutura de classes

| Classe | Descrição |
|--------|------------|
| **TelaNoticias** | Interface principal da aplicação, responsável por inicializar e gerenciar os eventos. |
| **Noticia** | Modelo que representa uma notícia, com categoria, conteúdo e informação se é fixa. |
| **GerenciadorNoticias** | Controlador responsável por armazenar, adicionar, remover e filtrar as notícias. |

---

## Conceitos aplicados

- Programação orientada a objetos (encapsulamento, classes e métodos)  
- Uso de **SWT** para construção de GUIs nativas  
- Gerenciamento de listas com `ArrayList`
- Manipulação de eventos (listeners)  
- Estrutura modular para CRUD (create, read, update, delete)

---


