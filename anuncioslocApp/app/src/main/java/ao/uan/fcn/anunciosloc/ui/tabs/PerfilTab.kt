package ao.uan.fcn.anunciosloc.ui.tabs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ao.uan.fcn.anunciosloc.data.repository.PerfilRepository
import kotlinx.coroutines.launch

@Composable
fun PerfilTab(
    token: String,
    username: String,
    email: String,
    perfilRepository: PerfilRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var saldo by remember { mutableStateOf<Int?>(null) }
    var chaves by remember { mutableStateOf<List<String>>(emptyList()) }
    var paresPerfil by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var novaChave by remember { mutableStateOf("") }
    var novoValor by remember { mutableStateOf("") }
    var isAdding by remember { mutableStateOf(false) }
    var mostrarValores by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        perfilRepository.obterPerfilLocal().collect { map ->
            paresPerfil = map
        }
        carregarDados()
    }

    fun carregarDados() {
        scope.launch {
            perfilRepository.consultarSaldo(token)
                .onSuccess { saldo = it }
                .onFailure { /* ignora */ }

            perfilRepository.listarChavesPerfis()
                .onSuccess { chaves = it }
                .onFailure { /* ignora */ }
        }
    }

    fun adicionarPerfil() {
        if (novaChave.isBlank() || novoValor.isBlank()) {
            Toast.makeText(context, "Preencha chave e valor", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            isAdding = true
            val par = "${novaChave.trim()}=${novoValor.trim()}"
            perfilRepository.enviarPerfil(token, listOf(par))
                .onSuccess {
                    val novosPares = paresPerfil.toMutableMap()
                    novosPares[novaChave.trim()] = novoValor.trim()
                    paresPerfil = novosPares
                    perfilRepository.salvarPerfilLocal(novosPares)
                    Toast.makeText(context, "Perfil atualizado", Toast.LENGTH_SHORT).show()
                    novaChave = ""
                    novoValor = ""
                    carregarDados()
                }.onFailure {
                    Toast.makeText(context, "Erro: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            isAdding = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF2196F3)),
            contentAlignment = Alignment.Center
        ) {
            Text(username.firstOrNull()?.uppercase() ?: "U", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text(username, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(email, fontSize = 14.sp, color = Color.Gray)

        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Saldo", fontSize = 14.sp)
                Text("${saldo ?: 0} pontos", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Atributos do Perfil", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.fillMaxWidth())

        if (paresPerfil.isEmpty()) {
            Text("Nenhum atributo definido.", color = Color.Gray, fontSize = 14.sp)
        } else {
            paresPerfil.forEach { (chave, valor) ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(chave, fontWeight = FontWeight.Medium)
                        Text(if (mostrarValores) valor else "••••••", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        TextButton(onClick = { mostrarValores = !mostrarValores }) {
            Text(if (mostrarValores) "Ocultar valores" else "Mostrar valores")
        }

        Spacer(Modifier.height(16.dp))
        Text("Adicionar par", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = novaChave,
            onValueChange = { novaChave = it },
            label = { Text("Chave") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = novoValor,
            onValueChange = { novoValor = it },
            label = { Text("Valor") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { adicionarPerfil() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isAdding
        ) {
            if (isAdding) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            else Text("Adicionar par")
        }

        Spacer(Modifier.height(16.dp))
        Text("Chaves públicas disponíveis", fontWeight = FontWeight.SemiBold)
        if (chaves.isEmpty()) {
            Text("Nenhuma chave pública disponível.", color = Color.Gray, fontSize = 13.sp)
        } else {
            chaves.forEach { chave ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Text(chave, modifier = Modifier.padding(8.dp), fontSize = 13.sp)
                }
            }
        }
    }
}