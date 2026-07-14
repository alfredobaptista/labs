package ao.uan.fc.anunciosloc

import android.app.Application
import ao.uan.fc.anunciosloc.core.cache.CacheManager
import ao.uan.fc.anunciosloc.core.cache.SessionManager
import ao.uan.fc.anunciosloc.core.network.AnunciosSoapClient
import ao.uan.fc.anunciosloc.data.repository.*
import ao.uan.fc.anunciosloc.transport.centralized.CentralizedDeliveryManager
import ao.uan.fc.anunciosloc.transport.decentralized.discovery.PeerDiscoveryManager
import ao.uan.fc.anunciosloc.transport.decentralized.protocol.ProtocolHandler
import ao.uan.fc.anunciosloc.transport.decentralized.routing.MuleRouter
import ao.uan.fc.anunciosloc.transport.decentralized.wifidirect.WifiDirectManager

class AnunciosLocApp : Application() {

    lateinit var soapClient: AnunciosSoapClient
    lateinit var cacheManager: CacheManager
    lateinit var sessionManager: SessionManager

    lateinit var authRepository: AuthRepository
    lateinit var anuncioRepository: AnuncioRepository
    lateinit var localRepository: LocalRepository
    lateinit var perfilRepository: PerfilRepository

    lateinit var centralizedDeliveryManager: CentralizedDeliveryManager
    lateinit var wifiDirectManager: WifiDirectManager
    lateinit var peerDiscoveryManager: PeerDiscoveryManager
    lateinit var protocolHandler: ProtocolHandler
    lateinit var muleRouter: MuleRouter

    override fun onCreate() {
        super.onCreate()

        soapClient = AnunciosSoapClient()
        cacheManager = CacheManager(this)
        sessionManager = SessionManager(this)

        authRepository = AuthRepository(soapClient, sessionManager)
        anuncioRepository = AnuncioRepository(soapClient, cacheManager)
        localRepository = LocalRepository(soapClient, cacheManager)
        perfilRepository = PerfilRepository(soapClient, cacheManager)

        centralizedDeliveryManager = CentralizedDeliveryManager(soapClient, cacheManager)
        wifiDirectManager = WifiDirectManager(this).apply { inicializar() }
        peerDiscoveryManager = PeerDiscoveryManager()
        protocolHandler = ProtocolHandler()
        muleRouter = MuleRouter()
    }
}