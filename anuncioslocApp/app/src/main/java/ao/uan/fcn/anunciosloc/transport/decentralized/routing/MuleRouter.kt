package ao.uan.fcn.anunciosloc.transport.decentralized.routing


import ao.uan.fc.anunciosloc.core.utils.Constants
import ao.uan.fc.anunciosloc.transport.common.TransportMessage
import ao.uan.fc.dam.anuncioslock.transport.common.TransportMessage
import ao.uan.fcn.anunciosloc.core.utils.Constants
import java.util.concurrent.ConcurrentHashMap

class MuleRouter(
    private val capacidadeMaxima: Int = Constants.DEFAULT_MULE_CAPACITY
) {
    private val cacheMensagens = ConcurrentHashMap<String, TransportMessage>()

    fun podeAceitarMensagem(): Boolean {
        return cacheMensagens.size < capacidadeMaxima
    }

    fun armazenarMensagem(mensagem: TransportMessage): Boolean {
        if (!podeAceitarMensagem()) return false
        if (mensagem.saltos >= Constants.MAX_MULE_HOPS) return false
        cacheMensagens[mensagem.id] = mensagem
        return true
    }

    fun entregarMensagem(id: String): TransportMessage? {
        return cacheMensagens.remove(id)
    }

    fun listarMensagens(): List<TransportMessage> {
        return cacheMensagens.values.toList()
    }
}