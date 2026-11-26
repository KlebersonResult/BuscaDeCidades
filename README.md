# Busca de Cidades

Programa desktop em Java que consulta a API REST **GeoDB Cities** (RapidAPI) e mostra dados de cidades.

## O que ele faz

| Ação na tela | Chamada à API |
|---|---|
| **Buscar** (início do nome da cidade) | `GET /v1/geo/cities?namePrefix=...` |
| **Ver detalhes** (cidade selecionada) | `GET /v1/geo/cities/{id}` |

## Antes de começar

- JDK 17 ou superior
- Apache NetBeans (ou Maven no terminal)
- Conta no [RapidAPI](https://rapidapi.com) com a API **GeoDB Cities** assinada (plano gratuito)

## Como rodar

**1. Clone o projeto**

```
git clone https://github.com/KlebersonResult/BuscaDeCidades.git
```

**2. Coloque sua chave da API**

Copie `config.exemplo.properties` para `config.properties` e preencha:

```
chave.api=SUA_CHAVE
```

A chave fica em *My Apps → seu app → Security* no RapidAPI. Esse arquivo não vai para o Git.

**3. Execute**

- **NetBeans:** *File → Open Project*, escolha a pasta e clique em *Run Project* (F6).
- **Terminal:**
  ```
  mvn clean package
  java -jar target/ClienteGeoDB.jar
  ```

## Teste rápido

1. Digite `Poços de Caldas` e aperte Enter.
2. Selecione a primeira linha e clique em **Ver detalhes**.
3. O painel da direita mostra ID, coordenadas e população.

## Estrutura

| Arquivo | Função |
|---|---|
| `Principal` | Inicia o programa |
| `TelaPrincipal` | Tela (Swing) |
| `ClienteGeoDB` | Faz as chamadas GET à API |
| `Configuracao` | Lê a chave do `config.properties` |
| `Cidade` | Dados de uma cidade |
| `ErroApi` | Erros da API com mensagem em português |

Mais detalhes (URLs, autenticação e exemplos de JSON) em [`Documentacao.pdf`](Documentacao.pdf).
