📘 MINI DOCUMENTAÇÃO DA API UTILIZADA
📡 1. WifiP2pManager

Classe principal que controla WiFi Direct.

Funções principais:
🔹 discoverPeers()
manager.discoverPeers(channel, actionListener);

✔ Inicia procura de dispositivos próximos
✔ Requer WiFi ativo e permissões
✔ Executa descoberta assíncrona

🔹 requestPeers()
manager.requestPeers(channel, PeerListListener);

✔ Obtém lista atual de dispositivos encontrados
✔ Usado após evento PEERS_CHANGED

🔹 initialize()
channel = manager.initialize(context, looper, null);

✔ Cria canal de comunicação com sistema WiFi Direct
✔ Necessário para todas as operações

📡 2. WifiP2pDevice

Representa um dispositivo encontrado.

Campos principais:
deviceName → nome do dispositivo
deviceAddress → MAC address
📡 3. WifiP2pDeviceList

Lista de dispositivos encontrados.

Método principal:
getDeviceList()

✔ Retorna todos os peers disponíveis

📡 4. BroadcastReceiver

Responsável por receber eventos do sistema.

Eventos principais:
WiFi Direct ativado/desativado
peers atualizados
conexão alterada
estado do dispositivo
📡 5. ActionListener

Usado em operações assíncronas:

Métodos:
onSuccess()
onFailure(int reason)

✔ Indica resultado da operação

🚀 RESULTADO FINAL

✔ Aplicação funcional
✔ Descoberta de dispositivos WiFi Direct
✔ Uso da API oficial Android
✔ Substituição correta do Termite
✔ Testável em dispositivos reais