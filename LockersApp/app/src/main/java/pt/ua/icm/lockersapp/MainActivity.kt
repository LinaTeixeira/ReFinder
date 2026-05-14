package pt.ua.icm.lockersapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import pt.ua.icm.lockersapp.ui.theme.LockersAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LockersAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LockerScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LockerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Estado do Locker: true = Bloqueado, false = Aberto
    var isLocked by remember { mutableStateOf(true) }

    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val scannedText = result.contents

        if (scannedText == null) {
            Toast.makeText(context, "Leitura cancelada", Toast.LENGTH_SHORT).show()
        } else {
            // Verifica se o código começa com "ReFinder"
            if (scannedText.startsWith("REFINDER_PICKUP:")) {
                val pin = scannedText.removePrefix("REFINDER_PICKUP:")
                isLocked = false
                Toast.makeText(context, "Acesso Concedido! PIN: $pin", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "QR Code Inválido para ReFinder!", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = if (isLocked) "Bloqueado" else "Aberto",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = if (isLocked) Color.Red else Color.Green
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val options = ScanOptions().apply {
                    setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    setPrompt("Aponte para o QR Code do Locker")
                    setCameraId(0)
                    setBeepEnabled(true)
                    setOrientationLocked(false)
                }
                barcodeLauncher.launch(options)
            },
            enabled = isLocked
        ) {
            Text(text = "Scanear QR Code", fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LockerScreenPreview() {
    LockersAppTheme {
        LockerScreen()
    }
}