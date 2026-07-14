# Laboratório #5 – WiFi Direct (MsgSender)

## 1. Objectivo do Exercício II

Implementar uma aplicação Android que:

- Utilize a API nativa `WifiP2pManager` (em vez do emulador Termite) para formar grupos WiFi Direct;
- Permita trocar mensagens entre dois dispositivos do grupo;
- Suporte **modo conversacional** (múltiplas mensagens na mesma ligação TCP), conforme modificação pedida pelo professor.

---

## 2. Implementação Realizada

Foi desenvolvida a aplicação **WifiDirectChat** (package `ao.uan.fcn.wifidirectchat`) com as seguintes componentes:

- `WifiP2pService` – serviço em segundo plano que gere o ciclo de vida do WiFi Direct (descoberta, conexão, grupo, eventos).
- `MsgSenderActivity` – Activity principal com os botões idênticos ao enunciado (WiFi On/Off, In Range, In Network, Connect, Send, Disconnect).
- AsyncTasks para comunicação por sockets:
  - `IncomingCommTask` – servidor TCP que fica à escuta e mantém a ligação aberta.
  - `OutgoingCommTask` – cliente que se liga ao IP do parceiro e espera mensagens.
  - `SendCommTask` – tarefa ligeira para enviar uma mensagem sem fechar o socket.
- Layout `activity_main.xml` copiado do `main.xml` original.

O modo conversacional é garantido porque os sockets não são fechados após cada mensagem – o `SendCommTask` apenas escreve no `PrintWriter` existente.

---

## 3. Diferenças entre o Termite e a API Nativa

| Aspecto | Termite (simulação) | API Nativa (dispositivos reais) |
|---------|---------------------|--------------------------------|
| Endereços IP | IPs virtuais (ex.: 192.168.0.2) configurados no `backends.conf` | IPs reais atribuídos pelo Android: GO = `192.168.49.1`, clientes = `192.168.49.x` |
| Formação de grupo | Comandos `creategroup A (B)` no console Termite | Automática via `WifiP2pManager.connect()` |
| Movimento dos nós | Comandos `move A (B)` para simular aproximação | Depende da distância física real entre dispositivos |
| Descoberta de peers | Lista gerida pelo Termite | Através de `discoverPeers()` e broadcast `WIFI_P2P_PEERS_CHANGED_ACTION` |

---

## 4. Como obter o IP do parceiro em dispositivos físicos

No ambiente real (sem Termite), o IP do **Group Owner (GO)** é fixo: `192.168.49.1`. Os clientes recebem IPs dinâmicos na mesma sub‑rede.

**Procedimento para saber qual IP digitar no campo "Connect":**

1. Em ambos os dispositivos, ligue o WiFi e a localização.
2. Abra a app e clique **"WiFi On"** nos dois.
3. Num dos dispositivos, clique **"In Range"**, selecione o nome do outro e confirme a ligação.
   - O sistema Android forma automaticamente um grupo; um dispositivo torna‑se GO e o outro cliente.
4. No dispositivo **cliente**, clique **"In Network"**.
   - Surgirá um diálogo com a informação do grupo, por exemplo:  
     *"You are client. IP do GO: 192.168.49.1"*
5. Digite esse IP no campo de texto da app cliente e pressione **"Connect"**.
6. A partir desse momento, é possível trocar múltiplas mensagens (modo conversacional).

> **Nota:** Se ambos os dispositivos forem GO (o que não acontece), ou se houver mais do que dois membros, cada membro tem um IP diferente. O diálogo "In Network" lista todos os membros e o respetivo IP.

---

## 5. Porque é necessário digitar o IP manualmente?

O professor optou por manter o mesmo comportamento da aplicação original do Termite, onde o utilizador **digita o IP virtual** do destino. Isto tem um objectivo pedagógico:

- Reforça a compreensão de que a comunicação por WiFi Direct usa uma **rede IP real** (TCP/IP).
- O aluno aprende que, para estabelecer um socket, é necessário conhecer o endereço de rede do parceiro.
- Na versão original com Termite, os IPs eram virtuais e também tinham de ser escritos à mão. A adaptação para dispositivos reais mantém a mesma interacção, apenas com endereços reais (`192.168.49.1`).

**Opcional (não pedido):**  
Seria possível tornar o processo automático obtendo `groupOwnerAddress` do `WifiP2pInfo` e ligar sem intervenção do utilizador. Contudo, isso fugiria ao enunciado, que exige o campo de texto e o botão "Connect" como no esqueleto original.

---

## 6. Fluxo de teste resumido

| Passo | Acção |
|-------|-------|
| 1 | Instalar a app em dois dispositivos Android 7+ (WiFi e localização ligados). |
| 2 | Em ambos: abrir a app e clicar **"WiFi On"**. |
| 3 | Num deles: **"In Range"** → seleccionar o outro → confirmar. |
| 4 | No **cliente**: **"In Network"** → anotar o IP do GO. |
| 5 | No cliente: escrever esse IP no campo de texto → **"Connect"**. |
| 6 | Escrever mensagens e enviar – o chat funciona de forma contínua. |
| 7 | Para terminar: **"Disconnect"** e depois **"WiFi Off"** em ambos. |

---

## 7. Conclusão

O laboratório foi realizado com sucesso, cumprindo os objectivos:

- ✅ Utilização da API nativa `WifiP2pManager` sem dependência do Termite.
- ✅ Formação de grupos WiFi Direct em dispositivos reais.
- ✅ Troca de mensagens em modo conversacional (múltiplas mensagens na mesma ligação).
- ✅ Manutenção da interface e do fluxo exigido pelo professor, incluindo a introdução manual do IP para fins didácticos.

A aplicação está pronta para ser demonstrada e o código fonte segue as boas práticas para Android 7 (API 24).