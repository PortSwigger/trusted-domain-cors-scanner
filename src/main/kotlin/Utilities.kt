import burp.api.montoya.MontoyaApi
import burp.api.montoya.core.Marker
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.requests.HttpRequest
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadPoolExecutor

fun randSting(length: Int): String {
    val chars = "abcdefghijklmnopqrstucwxyz"
    return (1..length).map{ chars.random() }.joinToString("")
}

fun getMarkerFromResponse(requestResponse: HttpRequestResponse, match: String): Marker? {
    val start = requestResponse.response().toString().indexOf(match, 0)
    val end = start+match.length
    val marker = Marker.marker(start, end)
    return marker
}

fun getMarkerFromRequest(requestResponse: HttpRequestResponse, match: String): Marker? {
    val start = requestResponse.request().toString().indexOf(match, 0)
    val end = start+match.length
    val marker = Marker.marker(start, end)
    return marker
}

fun checkArbitraryOriginReflection(api: MontoyaApi, selectedRequest: HttpRequest, threadPool: ThreadPoolExecutor): Boolean {
    val attackerDomain = randSting(12) + ".com"
    //Check if we have arbitrary origin reflection. If we do, just give-up burp will handle this for us and we don't want to report all of these bypasses....
    val arbitraryOriginCheckRequest = selectedRequest.withHeader("Origin", attackerDomain)

    val future: Future<Boolean> = threadPool.submit(Callable {
        try {
            val arbitraryOrigincheckRequestResponse = api.http().sendRequest(arbitraryOriginCheckRequest)
            if (arbitraryOrigincheckRequestResponse.response()
                    .headerValue("Access-Control-Allow-Credentials") == "true" && arbitraryOrigincheckRequestResponse.response()
                    .headerValue("Access-Control-Allow-Origin") == attackerDomain
            ) {
                api.logging()
                    .logToOutput("Arbitrary Reflected Origin found, skipping because burp will handle this for us.")
                return@Callable true
            } else {
                return@Callable false
            }
        } catch (e: Exception) {
            api.logging().logToError("Error during request: ${e.message}")
            return@Callable false
        }
    })

    val result = future.get()
    return result
}

val defaultTrustedDomains = listOf<String>(
    "[::]",
    "[::1]",
    "[::ffff:7f00:1]",
    "[0000:0000:0000:0000:0000:0000:0000:0000]",
    "0.0.0.0",
    "127.0.0.1",
    "localhost"
)