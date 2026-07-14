package ao.uan.fcn.anunciosloc.ui.tabs

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AddLocalDialog(
    infraestruturas: List<String>,
    onDismiss: () -> Unit,
    onSave: (infra: String, nome: String, lat: Double, lon: Double, raio: Int) -> Unit
) {
    var selectedInfra by remember { mutableStateOf(infraestruturas.firstOrNull() ?: "") }
    var nomeLocal by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("-8.8383") }
    var lon by remember { mutableStateOf("13.2344") }
    var raio by remember { mutableStateOf("500") }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Criar novo local", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        infraestruturas.forEach { infra ->
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
                    value = nomeLocal,
                    onValueChange = { nomeLocal = it },
                    label = { Text("Nome do local") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lat,
                    onValueChange = { lat = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lon,
                    onValueChange = { lon = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = raio,
                    onValueChange = { raio = it },
                    label = { Text("Raio (metros)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val latD = lat.toDoubleOrNull()
                val lonD = lon.toDoubleOrNull()
                val raioI = raio.toIntOrNull()
                if (selectedInfra.isNotBlank() && nomeLocal.isNotBlank() && latD != null && lonD != null && raioI != null) {
                    onSave(selectedInfra, nomeLocal, latD, lonD, raioI)
                } else {
                    Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}