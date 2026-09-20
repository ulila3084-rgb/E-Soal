package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BankSoalViewModel
import com.example.ui.screens.AddQuestionScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MasterSoalScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.SyncScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BankSoalApp()
            }
        }
    }
}

@Composable
fun BankSoalApp(viewModel: BankSoalViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (currentTab == 0) {
                FloatingActionButton(
                    onClick = { viewModel.setCurrentTab(1) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_question")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Soal")
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                val navItems = listOf(
                    Triple(0, "Bank Soal", Icons.Default.MenuBook to Icons.Outlined.MenuBook),
                    Triple(1, "+ Tambah", Icons.Default.Add to Icons.Default.Add),
                    Triple(2, "Master F4", Icons.Default.Print to Icons.Outlined.Print),
                    Triple(3, "Latihan", Icons.Default.Quiz to Icons.Outlined.Quiz),
                    Triple(4, "Sinkron", Icons.Default.CloudSync to Icons.Outlined.CloudSync)
                )

                navItems.forEach { (index, label, iconPair) ->
                    val selected = currentTab == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (index == 3 && currentTab != 3) {
                                viewModel.startQuiz("Semua")
                            }
                            viewModel.setCurrentTab(index)
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) iconPair.first else iconPair.second,
                                contentDescription = label
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            0 -> HomeScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
            1 -> AddQuestionScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
            2 -> MasterSoalScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
            3 -> QuizScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
            4 -> SyncScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}

