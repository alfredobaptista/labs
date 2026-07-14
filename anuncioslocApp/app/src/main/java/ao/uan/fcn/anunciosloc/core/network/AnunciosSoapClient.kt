package ao.uan.fc.anuncioslock.core.network


import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ao.uan.fc.anunciosloc.core.utils.Constants
import ao.uan.fc.anunciosloc.data.model.*
import java.util.concurrent.TimeUnit

class AnunciosSoapClient(
    private val baseUrl: String = Constants.BASE_URL
) {
    companion object {
        private const val TAG = "SOAP_DEBUG"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(SoapInterceptor())
        .build()

    private suspend fun callSoap(method: String, params: String): String =
        withContext(Dispatchers.IO) {
            val envelope = """
                <soapenv:Envelope
                    xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                    xmlns:ser="${Constants.NAMESPACE}">
                    <soapenv:Header/>
                    <soapenv:Body>
                        <ser:$method>$params</ser:$method>
                    </soapenv:Body>
                </soapenv:Envelope>
            """.trimIndent()

            Log.d(TAG, "=== ENVELOPE $method ===\n$envelope")

            val request = Request.Builder()
                .url(baseUrl)
                .addHeader("Content-Type", "text/xml; charset=utf-8")
                .addHeader("SOAPAction", "")
                .post(envelope.toRequestBody("text/xml; charset=utf-8".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: throw Exception("Resposta vazia")
                Log.d(TAG, "=== RESPOSTA (${response.code}) ===\n$body")
                if (!response.isSuccessful) throw Exception("HTTP ${response.code}: $body")
                body
            }
        }

    private fun extractTag(xml: String, tag: String): String {
        val regex = Regex("<(?:[^:>]+:)?$tag(?:\\s[^>]*)?>([\\s\\S]*?)</(?:[^:>]+:)?$tag>", RegexOption.DOT_MATCHES_ALL)
        return regex.find(xml)?.groupValues?.get(1)?.trim() ?: ""
    }

    private fun extractAllTags(xml: String, tag: String): List<String> {
        val regex = Regex("<(?:[^:>]+:)?$tag(?:\\s[^>]*)?>([\\s\\S]*?)</(?:[^:>]+:)?$tag>", RegexOption.DOT_MATCHES_ALL)
        return regex.findAll(xml).map { it.groupValues[1].trim() }.toList()
    }

    // --- Autenticação ---
    suspend fun login(username: String, password: String): Result<String> = runCatching {
        val xml = callSoap("login", """
            <nomeUtilizador>$username</nomeUtilizador>
            <palavraPasse>$password</palavraPasse>
        """.trimIndent())
        extractTag(xml, "return").takeIf { it.isNotEmpty() } ?: throw Exception("Login falhou")
    }

    suspend fun logout(token: String): Result<Boolean> = runCatching {
        callSoap("logout", "<idSessao>$token</idSessao>")
        true
    }

    suspend fun ativarUtilizador(nome: String, email: String, senha: String): Result<String> = runCatching {
        val xml = callSoap("ativarUtilizador", """
            <nomeUtilizador>$nome</nomeUtilizador>
            <email>$email</email>
            <palavraPasse>$senha</palavraPasse>
        """.trimIndent())
        extractTag(xml, "return").ifEmpty { "Sucesso" }
    }

    // --- Perfil ---
    suspend fun enviarPerfil(token: String, pares: List<String>): Result<String> = runCatching {
        val paresXml = pares.joinToString("") { "<pares>$it</pares>" }
        val xml = callSoap("enviarPerfil", """
            <idSessao>$token</idSessao>
            $paresXml
        """.trimIndent())
        extractTag(xml, "return").ifEmpty { "OK" }
    }

    suspend fun listarChavesPerfis(): Result<List<String>> = runCatching {
        val xml = callSoap("listarChavesPerfis", "")
        extractAllTags(xml, "return")
    }

    // --- Saldo ---
    suspend fun consultarSaldo(token: String): Result<Int> = runCatching {
        val xml = callSoap("consultarSaldo", "<idSessao>$token</idSessao>")
        extractTag(xml, "return").toIntOrNull() ?: throw Exception("Saldo inválido")
    }

    // --- Infraestruturas ---
    suspend fun listarInfraestruturas(lat: Double, lon: Double, k: Int): Result<List<Infraestrutura>> = runCatching {
        val xml = callSoap("listarInfraestruturas", """
            <latitude>$lat</latitude>
            <longitude>$lon</longitude>
            <k>$k</k>
        """.trimIndent())
        val blocks = extractAllTags(xml, "return")
        blocks.map { block ->
            Infraestrutura(
                nome = extractTag(block, "nome"),
                latitude = extractTag(block, "latitude").toDoubleOrNull() ?: 0.0,
                longitude = extractTag(block, "longitude").toDoubleOrNull() ?: 0.0,
                raio = extractTag(block, "raio").toIntOrNull() ?: 0,
                capacidade = extractTag(block, "capacidade").toIntOrNull() ?: 0,
                conexoesDisponiveis = extractTag(block, "conexoesDisponiveis").toIntOrNull() ?: 0,
                totalAnuncios = extractTag(block, "totalAnuncios").toIntOrNull() ?: 0,
                totalEntregas = extractTag(block, "totalEntregas").toIntOrNull() ?: 0,
                url = extractTag(block, "url")
            )
        }
    }

    suspend fun criarLocal(token: String, infra: String, nome: String, lat: Double, lon: Double, raio: Int): Result<String> = runCatching {
        val xml = callSoap("criarLocal", """
            <idSessao>$token</idSessao>
            <nomeInfraestrutura>$infra</nomeInfraestrutura>
            <nomeLocal>$nome</nomeLocal>
            <latitude>$lat</latitude>
            <longitude>$lon</longitude>
            <raio>$raio</raio>
        """.trimIndent())
        extractTag(xml, "return").ifEmpty { "Criado" }
    }

    // --- Anúncios ---
    suspend fun postarMensagem(
        token: String,
        local: String,
        infra: String,
        texto: String,
        filtros: List<String> = emptyList(),
        tipo: String = "WHITELIST",
        dataExpiracao: String? = null
    ): Result<String> = runCatching {
        val politicaXml = if (filtros.isNotEmpty() || tipo != "WHITELIST" || dataExpiracao != null) {
            """
            <politica>
                <tipo>$tipo</tipo>
                ${filtros.joinToString("") { "<filtros>$it</filtros>" }}
                ${if (dataExpiracao != null) "<dataExpiracao>$dataExpiracao</dataExpiracao>" else ""}
            </politica>
            """.trimIndent()
        } else ""
        val xml = callSoap("postarMensagem", """
            <idSessao>$token</idSessao>
            <nomeLocal>$local</nomeLocal>
            <nomeInfraestrutura>$infra</nomeInfraestrutura>
            <texto>$texto</texto>
            $politicaXml
        """.trimIndent())
        extractTag(xml, "return")
    }

    suspend fun receberMensagem(token: String, infra: String): Result<List<Anuncio>> = runCatching {
        val xml = callSoap("receberMensagem", """
            <idSessao>$token</idSessao>
            <nomeInfraestrutura>$infra</nomeInfraestrutura>
        """.trimIndent())
        val blocks = extractAllTags(xml, "return")
        blocks.map { block ->
            val politicaBlock = extractTag(block, "politica")
            Anuncio(
                id = extractTag(block, "id"),
                autor = extractTag(block, "emailRemetente"),
                nomeLocal = extractTag(block, "nomeLocal"),
                nomeInfraestrutura = extractTag(block, "nomeInfraestrutura"),
                conteudo = extractTag(block, "texto"),
                dataPublicacao = extractTag(block, "dataPublicacao"),
                politica = if (politicaBlock.isNotEmpty()) Politica(
                    tipo = extractTag(politicaBlock, "tipo"),
                    filtros = extractAllTags(politicaBlock, "filtros"),
                    dataExpiracao = extractTag(politicaBlock, "dataExpiracao").ifEmpty { null }
                ) else null
            )
        }
    }

    suspend fun removerMensagem(token: String, id: String): Result<Boolean> = runCatching {
        callSoap("removerMensagem", """
            <idSessao>$token</idSessao>
            <id>$id</id>
        """.trimIndent())
        true
    }
}