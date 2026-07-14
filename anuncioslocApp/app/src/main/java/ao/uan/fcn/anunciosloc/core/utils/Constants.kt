package ao.uan.fcn.anunciosloc.core.utils

object Constants {
    // Servidor central
    const val BASE_URL = "http://192.168.1.187:8080/anunciosloc"
    const val NAMESPACE = "http://ws.anunciosloc.anunciosloc.fcn.aun.ao/"
    const val UDDI_URL = "http://localhost:9090/uddi"

    // WiFi Direct
    const val WIFI_DIRECT_PORT = 8988
    const val BUFFER_SIZE = 65507

    // Localização
    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val LOCATION_FASTEST_INTERVAL = 2000L

    // Cache
    const val CACHE_EXPIRATION_HOURS = 24

    // Mulas
    const val MAX_MULE_HOPS = 1
    const val DEFAULT_MULE_CAPACITY = 5

    // Centro de Luanda (para testes)
    const val DEFAULT_LATITUDE = -8.8383
    const val DEFAULT_LONGITUDE = 13.2344
}