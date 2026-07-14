package ao.uan.fcn.anunciosloc.ui.tabs

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ao.uan.fcn.anunciosloc.data.model.Anuncio
import ao.uan.fcn.anunciosloc.data.repository.AnuncioRepository
import ao.uan.fcn.anunciosloc.data.repository.LocalRepository
import kotlinx.coroutines.launch

@Composable
fun InicioTab(
    token: String,
    localRepository: LocalRepository,
    anuncioRepository: AnuncioRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var infraList by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedInfra by remember { mutableStateOf("") }
    var anuncios by remember { mutableStateOf<List<Anuncio>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        localRepository.obterInfraestruturasCache().collect { list ->
            infraList = list.map { it.nome }
            if (selectedInfra.isEmpty() && infraList.isNotEmpty()) {
                selectedInfra = infraList.first()
                carregarAnuncios()
            }
        }
    }

    fun carregarAnuncios() {
        if (selectedInfra.isBlank()) return
        scope.launch {
            isLoading = true
            anuncioRepository.receberMensagem(token, selectedInfra)
                .onSuccess { anuncios = it }
                .onFailure { Toast.makeText(context, "Erro: ${it.message}", Toast.LENGTH_SHORT).show() }
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Anúncios", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedInfra,
                onValueChange = {},
                readOnly = true,
                label = { Text("Infraestrutura") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                infraList.forEach { infra ->
                    DropdownMenuItem(
                        text = { Text(infra) },
                        onClick = {
                            selectedInfra = infra
                            expanded = false
                            localRepository.salvarUltimaInfra(infra)
                            carregarAnuncios()
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (anuncios.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum anúncio disponível.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(anuncios) { anuncio ->
                    AnuncioCard(anuncio)
                }
            }
        }
    }
}

@Composable
fun AnuncioCard(anuncio: Anuncio) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(anuncio.nomeLocal, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(anuncio.conteudo, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text("👤 ${anuncio.autor}", fontSize = 12.sp, color = Color.Gray)
            Text("📅 ${anuncio.dataPublicacao.take(10)}", fontSize = 12.sp, color = Color.Gray)
            if (anuncio.politica != null) {
                Text("🔒 ${anuncio.politica.tipo}", fontSize = 12.sp, color = Color(0xFF2196F3))
            }
        }
    }
}