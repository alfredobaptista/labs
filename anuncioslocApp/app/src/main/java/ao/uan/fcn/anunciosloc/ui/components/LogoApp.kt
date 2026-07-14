package ao.uan.fcn.anunciosloc.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ao.uan.fc.anunciosloc.R

@Composable
fun LogoApp(tamanho: Int = 200) {
    Box(
        modifier = Modifier.size(tamanho.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(tamanho.dp)
        )
    }
}