📄 🧾 EXPLICAÇÃO PARA O RELATÓRIO (WiFi Direct Lab)
📌 1. Objetivo do Trabalho

O objetivo deste laboratório foi implementar uma aplicação Android capaz de descobrir dispositivos próximos utilizando a tecnologia WiFi Direct (WiFi P2P).
A aplicação permite a deteção de peers (dispositivos disponíveis na rede local) sem necessidade de um ponto de acesso (router).

📌 2. Tecnologia Utilizada

Foi utilizada a API nativa do Android:

WifiP2pManager
WifiP2pManager.Channel
BroadcastReceiver
WifiP2pDeviceList

Esta API permite comunicação direta entre dispositivos Android através de WiFi, formando uma rede peer-to-peer.

📌 3. Funcionamento da Aplicação

A aplicação possui três funcionalidades principais:

🔘 WiFi ON / OFF

Estes botões servem apenas como interface informativa, pois o controlo do WiFi é gerido automaticamente pelo sistema operativo Android.

🔍 In Range (Descoberta de Dispositivos)

Quando o utilizador clica no botão “In Range”, a aplicação executa o método:

manager.discoverPeers(channel, actionListener);

Este método inicia o processo de descoberta de dispositivos WiFi Direct próximos.

📡 4. Receção dos Dispositivos

Quando dispositivos são encontrados, o sistema Android envia um evento para o BroadcastReceiver, que reage ao seguinte:

WIFI_P2P_PEERS_CHANGED_ACTION

Neste momento, a aplicação chama:

manager.requestPeers(channel, PeerListListener);
📋 5. Exibição dos Resultados

Os dispositivos encontrados são recebidos através do método:

onPeersAvailable(WifiP2pDeviceList peerList)

A aplicação percorre a lista de dispositivos e apresenta:

Nome do dispositivo
Endereço MAC

Os resultados são exibidos numa janela (AlertDialog).

📱 6. Testes Realizados

O sistema foi testado em dois dispositivos Android físicos.

Condições necessárias:
WiFi ativado em ambos os dispositivos
Localização ativada (obrigatório no Android moderno)
Aplicação instalada nos dois dispositivos
Resultado:
Um dispositivo conseguiu detetar o outro com sucesso através do WiFi Direct.
⚠️ 7. Problemas Encontrados

Durante o desenvolvimento foram encontrados os seguintes problemas:

Uso de código antigo baseado em Termite (substituído por API moderna Android)
Erros de compatibilidade com versões recentes do Android
Falta de permissões de localização
Erros de layout e recursos XML (IDs inexistentes)
🔄 8. Melhorias Futuras
Implementar conexão direta entre dispositivos (Group Owner)
Troca de mensagens via sockets
Interface mais moderna com ConstraintLayout
Gestão dinâmica de permissões em tempo de execução
📌 9. Conclusão

O laboratório permitiu compreender o funcionamento do WiFi Direct no Android, incluindo descoberta de dispositivos e uso de BroadcastReceivers para eventos do sistema.
A aplicação demonstra comunicação peer-to-peer sem necessidade de infraestrutura de rede tradicional.