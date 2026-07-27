package pl.bochynski.kosmetyki.ui.sprawdzkod

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SprawdzKodWejscieRoute(
    naWstecz: () -> Unit,
    naSkanuj: () -> Unit,
    naSzukaj: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SprawdzKodWejscieScreen(naWstecz = naWstecz, naSkanuj = naSkanuj, naSzukaj = naSzukaj, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SprawdzKodWejscieScreen(
    naWstecz: () -> Unit,
    naSkanuj: () -> Unit,
    naSzukaj: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var wpisanyEan by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Sprawdź kod kreskowy") },
                navigationIcon = {
                    IconButton(onClick = naWstecz) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Wpisz kod EAN produktu albo zeskanuj go aparatem.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = wpisanyEan,
                    onValueChange = { wpisanyEan = it },
                    label = { Text("Kod EAN") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { if (wpisanyEan.isNotBlank()) naSzukaj(wpisanyEan) }
                    )
                )
                IconButton(onClick = naSkanuj) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Skanuj kod EAN")
                }
            }

            Button(
                onClick = { naSzukaj(wpisanyEan) },
                modifier = Modifier.fillMaxWidth(),
                enabled = wpisanyEan.isNotBlank()
            ) {
                Text("Sprawdź")
            }
        }
    }
}
