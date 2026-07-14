📄 📡 RELATÓRIO — LAB 5 WIFI DIRECT (ANDROID)
📌 1. Introdução

Este laboratório teve como objetivo a implementação de uma aplicação Android capaz de realizar descoberta de dispositivos próximos utilizando tecnologia WiFi Direct (WiFi P2P).

A aplicação permite identificar peers disponíveis sem necessidade de um router ou infraestrutura de rede tradicional, utilizando comunicação direta entre dispositivos.

📌 2. Objetivo do Trabalho
Implementar descoberta de dispositivos via WiFi Direct
Utilizar a API oficial do Android (WifiP2pManager)
Processar eventos de rede através de BroadcastReceiver
Testar comunicação entre dispositivos físicos Android
📌 3. Tecnologia Utilizada

Foi utilizada a API oficial do Android fornecida pela Google:

WifiP2pManager
WifiP2pManager.Channel
WifiP2pDevice
WifiP2pDeviceList
BroadcastReceiver

Esta API substitui a biblioteca académica Termite (SimWifiP2p), que não é compatível com versões modernas do Android.

📌 4. Arquitetura da Aplicação

A aplicação é composta por:

Activity principal (PeerScannerActivity)
Interface do utilizador
Botões de controlo
WifiP2pManager
Gestão do WiFi Direct
BroadcastReceiver
Receção de eventos do sistema
PeerListListener
Receção da lista de dispositivos encontrados
📌 5. Funcionamento da Aplicação
🔘 Botão “In Range”

Quando o utilizador clica no botão, é iniciado o processo de descoberta:

manager.discoverPeers(channel, actionListener);

✔ Inicia a procura de dispositivos WiFi Direct próximos

📡 Callback de sucesso
onSuccess()

✔ Indica que o processo de discovery foi iniciado corretamente
✔ Mostra mensagem: “Scanning peers...”

❌ Callback de erro
onFailure(int reason)

✔ Indica falha na descoberta
✔ Retorna código de erro do sistema

📡 Receção de peers

Quando dispositivos são encontrados:

onPeersAvailable(WifiP2pDeviceList peerList)

✔ Percorre lista de dispositivos
✔ Obtém nome e endereço MAC
✔ Exibe resultados em AlertDialog

📌 6. BroadcastReceiver (Eventos do Sistema)

O BroadcastReceiver trata eventos do WiFi Direct:

📡 Estado do WiFi Direct
WIFI_P2P_STATE_CHANGED_ACTION

✔ Indica se WiFi Direct está ativo ou não

🔍 Mudança na lista de peers
WIFI_P2P_PEERS_CHANGED_ACTION

✔ Atualiza lista de dispositivos
✔ Pode chamar requestPeers()

🔗 Alteração de conexão
WIFI_P2P_CONNECTION_CHANGED_ACTION

✔ Indica ligação ou desconexão entre dispositivos

📱 Alteração do dispositivo local
WIFI_P2P_THIS_DEVICE_CHANGED_ACTION

✔ Mostra informações do próprio dispositivo

📌 7. Testes Realizados

Os testes foram realizados em dispositivos Android físicos.

Condições necessárias:lab5
WiFi ativado
Localização ativada
Permissões concedidas
Aplicação aberta em ambos dispositivos
Resultado:
Inicialização do WiFi Direct bem-sucedida
Descoberta de peers funcional quando há mais de um dispositivo
Exibição correta de mensagens de estado
📌 8. Problemas Encontrados
Necessidade de permissões de localização no Android moderno
Diferenças entre Termite (antigo) e API atual Android
Falta de peers quando apenas um dispositivo está disponível
Bloqueios do sistema ao iniciar discovery sem permissões
📌 9. Conclusão

A aplicação implementada cumpre o objetivo de demonstrar comunicação peer-to-peer utilizando WiFi Direct no Android.

Apesar da substituição da biblioteca Termite pela API moderna do Android, foi possível manter a lógica do laboratório, garantindo descoberta de dispositivos e interação entre peers.
















































📄 RELATÓRIO — LABORATÓRIO #5
WiFi Direct – Descoberta de Dispositivos em Redes P2P

Disciplina: Desenvolvimento de Aplicações Móveis
Instituição: Universidade Agostinho Neto (UAN)
Curso: Ciências da Computação
Aluno: [Teu Nome]
Data: [Inserir data]

1. INTRODUÇÃO

O presente laboratório tem como objetivo o estudo e implementação de comunicação em redes WiFi Direct (Peer-to-Peer), permitindo que dispositivos Android descubram outros dispositivos próximos sem necessidade de um ponto de acesso (router).

Tradicionalmente, o laboratório utiliza o simulador Termite WiFi Direct, que permite emular dispositivos virtuais. No entanto, neste trabalho foi utilizada a API nativa do Android (WifiP2pManager), mantendo os mesmos conceitos fundamentais do sistema Termite.

2. OBJETIVOS
Compreender o funcionamento do WiFi Direct.
Implementar descoberta de dispositivos próximos (peer discovery).
Utilizar BroadcastReceiver para eventos de rede.
Interpretar estados de conexão e mudanças na rede.
Substituir a API Termite pela API real do Android.
3. FUNDAMENTAÇÃO TEÓRICA
3.1 WiFi Direct

WiFi Direct é uma tecnologia que permite comunicação direta entre dispositivos sem necessidade de roteador. Um dispositivo assume o papel de Group Owner (GO) e os restantes conectam-se a ele.

3.2 WifiP2pManager (Android API)

A classe principal utilizada é:

WifiP2pManager → controla operações WiFi Direct
Channel → canal de comunicação com o sistema
PeerListListener → recebe lista de dispositivos encontrados
3.3 BroadcastReceiver

O BroadcastReceiver é responsável por capturar eventos do sistema:

Estado do WiFi Direct (ligado/desligado)
Mudança na lista de peers
Mudança de conexão
Informação do próprio dispositivo
3.4 Substituição do Termite

O Termite fornece uma API simulada:

SimWifiP2pManager
SimWifiP2pDevice

Neste trabalho foi substituído por:

WifiP2pManager (API oficial Android)
4. ARQUITETURA DA APLICAÇÃO

A aplicação é composta por:

PeerScannerActivity
Controla interface e descoberta de dispositivos
WifiDirectBroadcastReceiver
Captura eventos do sistema
WifiP2pManager
Realiza a descoberta de peers
5. IMPLEMENTAÇÃO
5.1 Inicialização do WiFi Direct

A inicialização ocorre na Activity principal:

Obtenção do serviço WiFi Direct
Criação do canal de comunicação
Registo do BroadcastReceiver
5.2 Descoberta de dispositivos

A descoberta é feita através do método:

manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
onSuccess() → indica que o scan iniciou
onFailure() → indica erro na descoberta
5.3 Receção de peers

Quando dispositivos são encontrados:

O método onPeersAvailable() é chamado
Os dispositivos são listados e exibidos ao utilizador
5.4 BroadcastReceiver

O receiver trata eventos como:

WiFi Direct ativado/desativado
Mudança de peers
Conexão entre dispositivos
Estado do dispositivo
6. MANIFESTO (CONFIGURAÇÃO)

Foram utilizadas permissões essenciais:

INTERNET
ACCESS_WIFI_STATE
CHANGE_WIFI_STATE
ACCESS_NETWORK_STATE
ACCESS_FINE_LOCATION
NEARBY_WIFI_DEVICES

Estas permissões permitem descoberta e comunicação entre dispositivos WiFi Direct.

7. TESTES REALIZADOS
7.1 Cenário de teste
2 dispositivos Android
WiFi Direct ativado em ambos
Aplicação instalada nos dois dispositivos
7.2 Procedimento
Abrir aplicação
Clicar em “WiFi ON”
Clicar em “In Range”
Observar dispositivos encontrados
7.3 Resultados
Mensagem “Scanning peers...” exibida corretamente
Dispositivos próximos foram detectados
Lista exibida em dialog

Quando nenhum dispositivo está próximo:

Lista aparece vazia (“No devices found”)
8. DISCUSSÃO

O uso da API Android substitui com sucesso o simulador Termite, permitindo testes reais em dispositivos físicos.

Diferenças principais:

Termite	Android WiFi Direct
Simulação de rede	Rede real
SimWifiP2pManager	WifiP2pManager
Nós virtuais (A, B)	Dispositivos reais
Comandos CLI	Interface Android
9. CONCLUSÃO

O laboratório permitiu compreender o funcionamento de redes WiFi Direct e a descoberta de dispositivos próximos.

A implementação com API Android demonstrou o mesmo comportamento conceptual do Termite, mas em ambiente real, oferecendo maior precisão e aplicabilidade prática.

10. TRABALHO FUTURO
Implementação de grupos WiFi Direct (Group Owner)
Troca de mensagens entre dispositivos
Comunicação bidirecional (chat P2P)
Integração com backend (FastAPI ou Spring Boot)