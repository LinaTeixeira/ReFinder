package pt.ua.icm.refinder.ui.screens


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pt.ua.icm.refinder.ui.theme.ReFinderTheme

@Composable
fun SearchScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Search Screen")
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    ReFinderTheme{
        SearchScreen()
    }
}