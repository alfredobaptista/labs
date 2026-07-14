package ao.uan.fcn.anunciosloc.ui.tabs

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ao.uan.fcn.anunciosloc.data.repository.AnuncioRepository
import ao.uan.fcn.anunciosloc.data.repository.LocalRepository
import kotlinx.coroutines.launch

@Composable
fun PostarTab(
    token: String,
    anuncioRepository: AnuncioRepository,
    localRepository: LocalRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var infraList by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedInfra by remember { mutableStateOf("") }
    var local by remember { mutableStateOf("") }
    var texto by remember { mutableStateOf("") }
    var filtros by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("WHITELIST") }
    var dataExp by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        localRepository.obterInfraestruturasCache().collect { list ->
            infraList = list.map { it.nome }
            if (selectedInfra.isEmpty() && infraList.isNotEmpty()) {
                selectedInfra = infraList.first()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Publicar Anúncio", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedInfra,
                onValueChange = {},
                readOnly = true,
                label = { Text("Infraestrutura *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
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
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = local,
            onValueChange = { local = it },
            label = { Text("Nome do Local *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it },
            label = { Text("Texto *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        OutlinedTextField(
            value = filtros,
            onValueChange = { filtros = it },
            label = { Text("Filtros (chave=valor, vírgula)") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = tipo,
                onValueChange = { tipo = it },
                label = { Text("Tipo") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = dataExp,
                onValueChange = { dataExp = it },
                label = { Text("Data exp. (opcional)") },
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                if (selectedInfra.isBlank() || local.isBlank() || texto.isBlank()) {
                    Toast.makeText(context, "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isPosting = true
                scope.launch {
                    val filtrosList = filtros.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    anuncioRepository.postarMensagem(
                        token, local, selectedInfra, texto,
                        filtrosList, tipo, dataExp.ifEmpty { null }
                    ).onSuccess {
                        Toast.makeText(context, "Publicado com sucesso!", Toast.LENGTH_SHORT).show()
                        local = ""
                        texto = ""
                        filtros = ""
                        dataExp = ""
                    }.onFailure {
                        Toast.makeText(context, "Erro: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    isPosting = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isPosting
        ) {
            if (isPosting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            else Text("Publicar")
        }
    }
}