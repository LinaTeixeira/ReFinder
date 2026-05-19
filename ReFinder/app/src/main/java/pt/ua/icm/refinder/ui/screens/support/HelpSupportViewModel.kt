package pt.ua.icm.refinder.ui.screens.support

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.launch

class HelpSupportViewModel : ViewModel() {

    var messages by mutableStateOf(
        listOf(
            SupportMessage(
                text = "Olá! Sou o assistente do ReFinder. Posso ajudar-te com itens perdidos, itens achados, cacifos inteligentes, PINs e QR Codes.",
                isUser = false
            )
        )
    )
        private set

    var inputText by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    private val model = Firebase.ai(
        backend = GenerativeBackend.googleAI()
    ).generativeModel(
        modelName = "gemini-2.5-flash"
    )

    fun onInputChange(value: String) {
        inputText = value
    }

    fun sendMessage() {
        val userText = inputText.trim()
        if (userText.isBlank() || isLoading) return

        messages = messages + SupportMessage(userText, isUser = true)
        inputText = ""
        isLoading = true

        viewModelScope.launch {
            try {
                val prompt = """
                    És o assistente de suporte da app ReFinder, uma aplicação Android de perdidos e achados com Smart Lockers.
                    
                    Responde em português de Portugal, de forma curta, clara e simpática.
                    
                    A app permite:
                    - registar itens perdidos ou achados;
                    - adicionar fotos;
                    - pesquisar por filtros;
                    - ver itens no mapa;
                    - depositar itens achados em Smart Lockers;
                    - gerar PIN e QR Code para levantamento.
                    
                    Pergunta do utilizador:
                    $userText
                """.trimIndent()

                val response = model.generateContent(prompt)
                val answer = response.text ?: "Não consegui gerar uma resposta agora."

                messages = messages + SupportMessage(answer, isUser = false)
            } catch (e: Exception) {
                messages = messages + SupportMessage(
                    text = "ERRO REAL: ${e.message}",
                    isUser = false
                )
            } finally {
                isLoading = false
            }
        }
    }
}
