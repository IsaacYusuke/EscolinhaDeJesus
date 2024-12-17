package com.example.escolinhadejesus

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EscolinhaApp()
        }
    }
}

@Composable
fun EscolinhaApp() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "tela1") {
        composable("tela1") {
            Tela1(onIniciarClick = { navController.navigate("tela2") })
        }
        composable("tela2") {
            // Obtém os títulos dos gráficos do arquivo Data.kt
            Tela2(
                listaDeBotoes = graficos.map { it.titulo },
                onBotaoClick = { index -> navController.navigate("grafico/${index - 1}") }
            )
        }
        composable("grafico/{index}") { backStackEntry ->
            val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: 0
            // Obtém os dados do gráfico correspondente de Data.kt
            val grafico = graficos.getOrNull(index)
            TelaGraficoI(
                estadoJson = grafico?.arquivoJson ?: "arquivo_padrao.json",
                imagem2 = R.drawable.imagem2, // Mantém a imagem existente
                textoRolavel = grafico?.texto ?: "Texto padrão"
            )
        }
    }
}


@Composable
fun Tela1(onIniciarClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = rememberImagePainter(R.drawable.imagem1),
            contentDescription = "Fundo",
            modifier = Modifier.fillMaxSize()
        )
        Button(
            onClick = onIniciarClick,
            shape = RectangleShape, // Botão quadrado
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2E7D32), // Cor verde
                contentColor = Color.White // Cor do texto
            ),
            border = BorderStroke(2.dp, Color.Black), // Contorno preto
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = "Iniciar",
                fontSize = 30.sp // Tamanho da fonte
            )
        }
    }
}

@Composable
fun Tela2(listaDeBotoes: List<String>, onBotaoClick: (Int) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Imagem de fundo
        Image(
            painter = rememberImagePainter(R.drawable.imagem1),
            contentDescription = "Fundo",
            modifier = Modifier.fillMaxSize()
        )

        // Centralização da LazyColumn
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center // Centraliza o LazyColumn no meio da tela
        ) {
            LazyColumn(
                modifier = Modifier
                    .width(200.dp), // Define largura fixa para os botões
                verticalArrangement = Arrangement.spacedBy(8.dp) // Espaçamento vertical
            ) {
                itemsIndexed(listaDeBotoes) { index, texto ->
                    Button(
                        onClick = { onBotaoClick(index + 1) },
                        shape = RectangleShape, // Botões com formato quadrado
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32), // Cor verde
                            contentColor = Color.White // Cor do texto
                        ),
                        border = BorderStroke(2.dp, Color.Black), // Contorno preto
                        modifier = Modifier
                            .fillMaxWidth() // Ocupa a largura definida pelo LazyColumn
                            .padding(horizontal = 8.dp) // Padding horizontal
                    ) {
                        Text(
                            text = texto,
                            fontSize = 18.sp // Tamanho da fonte
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TelaGraficoI(estadoJson: String, imagem2: Int, textoRolavel: String) {
    val context = LocalContext.current

    // Inicializar o servidor local
    val server = remember { LocalWebServer(context) }
    DisposableEffect(Unit) {
        server.start()
        onDispose {
            server.stop() // Garante que o servidor será encerrado
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // WebView para exibir o gráfico
        AndroidView(factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                webViewClient = WebViewClient()
                layoutParams = android.widget.LinearLayout.LayoutParams( //ERA ISSO QUE FALTAVA (?)
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT
                ) // Força largura/altura adequadas  //ERA ISSO QUE FALTAVA (?)
                loadUrl("http://localhost:12346/grafico.html?json=$estadoJson")
            }
        }, modifier = Modifier
            .weight(0.75f)
            .fillMaxHeight())  // Garantia explícita da altura


        //loadUrl("http://localhost:12346/grafico.html?json=$estadoJson")

        // Cor sólida como fundo e texto sobreposto
        Column(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize() // A Box vai ocupar todo o espaço disponível
                    .background(color = androidx.compose.ui.graphics.Color(0xFF006400)) // Cor verde escuro (lousa)) // Cor sólida de fundo
                    .border(width = 2.dp, color = androidx.compose.ui.graphics.Color.Black) // Borda preta ao redor
            ) {
                // O texto vai ser colocado sobre a cor sólida
                Box(
                    modifier = Modifier
                        .fillMaxSize()  // Preenche toda a Box
                        .align(Alignment.Center)  // Centraliza o texto
                ) {
                    Text(
                        text = textoRolavel,
                        color = androidx.compose.ui.graphics.Color.White,  // Texto com cor preta
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 18.sp  // Tamanho do texto em sp
                        ),
                        modifier = Modifier.align(Alignment.Center)  // Garantir centralização
                    )
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewTela1() {
    Tela1(onIniciarClick = {})
}

@Preview(showBackground = true)
@Composable
fun PreviewTela2() {
    val listaDeBotoes = listOf("Gráfico 1", "Gráfico 2", "Gráfico 3")
    Tela2(listaDeBotoes = listaDeBotoes, onBotaoClick = {})
}

@Preview(showBackground = true)
@Composable
fun PreviewTelaGraficoI() {
    TelaGraficoI(
        estadoJson = "estado_1.json",
        imagem2 = R.drawable.imagem2,
        textoRolavel = "Texto de exemplo para o gráfico"
    )
}
