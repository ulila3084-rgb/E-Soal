package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BankSoalViewModel
import com.example.ui.components.ArabicInputAccessoryBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuestionScreen(
    viewModel: BankSoalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetNames by viewModel.sheetNames.collectAsState()
    val allQuestions by viewModel.allQuestions.collectAsState()
    val inputJilid by viewModel.inputJilid.collectAsState()
    val isCustomJilid by viewModel.isCustomJilid.collectAsState()
    val newJilidName by viewModel.newJilidName.collectAsState()
    val inputJenis by viewModel.inputJenis.collectAsState()
    val inputSoal by viewModel.inputSoal.collectAsState()
    val inputPilA by viewModel.inputPilA.collectAsState()
    val inputPilB by viewModel.inputPilB.collectAsState()
    val inputPilC by viewModel.inputPilC.collectAsState()
    val inputJawabanPilihan by viewModel.inputJawabanPilihan.collectAsState()
    val inputJawabanLain by viewModel.inputJawabanLain.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var jilidMenuExpanded by remember { mutableStateOf(false) }
    var activeField by remember { mutableStateOf("soal") }

    val allAvailableSheets = (listOf("Jilid 1", "Jilid 2", "Jilid 3", "Jilid 4", "Jilid 5") + sheetNames + allQuestions.map { it.sheetName }).distinct().sorted()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("add_question_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form Title Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Tambah Soal Baru",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tersimpan otomatis ke database & tersinkron ke Google Sheets",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. Pilih Jilid
                    Text(
                        text = "1. Pilih Jilid / Sheet Tujuan",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allAvailableSheets.forEach { sheet ->
                            FilterChip(
                                selected = !isCustomJilid && inputJilid == sheet,
                                onClick = {
                                    viewModel.setInputJilid(sheet)
                                },
                                label = { Text(sheet) }
                            )
                        }

                        FilterChip(
                            selected = isCustomJilid,
                            onClick = { viewModel.setIsCustomJilid(true) },
                            label = { Text("+ Jilid Baru") }
                        )
                    }

                    if (isCustomJilid) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newJilidName,
                            onValueChange = { viewModel.setNewJilidName(it) },
                            label = { Text("Nama Jilid / Sheet Baru") },
                            placeholder = { Text("Contoh: Jilid 6, Nahwu, Shorof...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2. Jenis Soal
                    Text(
                        text = "2. Jenis Soal",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val jenisOptions = listOf(
                        "pilihan" to "Pilihan",
                        "meneruskan" to "Meneruskan",
                        "melengkapi" to "Melengkapi",
                        "mengisi" to "Mengisi"
                    )

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        jenisOptions.forEachIndexed { index, (key, label) ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = jenisOptions.size),
                                onClick = { viewModel.setInputJenis(key) },
                                selected = inputJenis == key
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = if (inputJenis == key) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }
        }

        // Virtual Arabic Keyboard Accessory Bar
        item {
            ArabicInputAccessoryBar(
                onCharClicked = { char ->
                    viewModel.activeInputTarget = activeField
                    viewModel.appendArabicChar(char)
                }
            )
        }

        // Dynamic Form Fields Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    val soalLabel = when (inputJenis) {
                        "meneruskan" -> "SOAL / KALIMAT AWAL (MENERUSKAN)"
                        "melengkapi" -> "SOAL (Gunakan ______ untuk rumpang)"
                        "mengisi" -> "SOAL ISIAN / PERTANYAAN SINGKAT"
                        else -> "SOAL (Teks Arab / Pegon)"
                    }

                    val soalPlaceholder = when (inputJenis) {
                        "meneruskan" -> "إدخال بداية الجملة..."
                        "melengkapi" -> "ذهب الطالب إلى ______"
                        "mengisi" -> "ما هو ... ؟ / اذكر ... !"
                        else -> "إدخال السؤال هنا..."
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = soalLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Quick Arabic Stems Chip Bar
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "ما هو ... ؟",
                            "اذكر ... !",
                            "بيّن ... !",
                            "أكمل ما يأتي ...",
                            "واصل: ...",
                            "ذهب الطالب إلى ______"
                        ).forEach { stem ->
                            OutlinedButton(
                                onClick = {
                                    activeField = "soal"
                                    viewModel.setInputSoal(stem)
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(stem, fontSize = 11.sp, fontFamily = FontFamily.Serif)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        OutlinedTextField(
                            value = inputSoal,
                            onValueChange = {
                                activeField = "soal"
                                viewModel.setInputSoal(it)
                            },
                            placeholder = { Text(soalPlaceholder, style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 18.sp, textAlign = TextAlign.Right)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_soal_field"),
                            minLines = 3,
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontSize = 21.sp,
                                textAlign = TextAlign.Right,
                                lineHeight = 30.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Pilihan Ganda Specific Fields
                    if (inputJenis == "pilihan") {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "OPSI PILIHAN JAWABAN:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Opsi A
                                Text(
                                    text = "PILIHAN A (أ)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                    OutlinedTextField(
                                        value = inputPilA,
                                        onValueChange = {
                                            activeField = "pilA"
                                            viewModel.setInputPilA(it)
                                        },
                                        placeholder = { Text("الخيار أ...", style = TextStyle(fontFamily = FontFamily.Serif, textAlign = TextAlign.Right)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 19.sp,
                                            textAlign = TextAlign.Right
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Opsi B
                                Text(
                                    text = "PILIHAN B (ب)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                    OutlinedTextField(
                                        value = inputPilB,
                                        onValueChange = {
                                            activeField = "pilB"
                                            viewModel.setInputPilB(it)
                                        },
                                        placeholder = { Text("الخيار ب...", style = TextStyle(fontFamily = FontFamily.Serif, textAlign = TextAlign.Right)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 19.sp,
                                            textAlign = TextAlign.Right
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Opsi C
                                Text(
                                    text = "PILIHAN C (ج)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                    OutlinedTextField(
                                        value = inputPilC,
                                        onValueChange = {
                                            activeField = "pilC"
                                            viewModel.setInputPilC(it)
                                        },
                                        placeholder = { Text("الخيار ج...", style = TextStyle(fontFamily = FontFamily.Serif, textAlign = TextAlign.Right)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 19.sp,
                                            textAlign = TextAlign.Right
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Jawaban Benar Selector
                                Text(
                                    text = "PILIH KUNCI JAWABAN BENAR:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("أ", "ب", "ج").forEach { choice ->
                                        FilterChip(
                                            selected = inputJawabanPilihan == choice,
                                            onClick = { viewModel.setInputJawabanPilihan(choice) },
                                            label = {
                                                Text(
                                                    text = "Jawaban $choice",
                                                    fontSize = 15.sp,
                                                    fontWeight = if (inputJawabanPilihan == choice) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Meneruskan / Melengkapi / Mengisi Jawaban
                        val answerLabel = when (inputJenis) {
                            "meneruskan" -> "JAWABAN / KELANJUTAN"
                            "melengkapi" -> "JAWABAN KATA RUMPANG"
                            else -> "KUNCI JAWABAN ISIAN / MODEL JAWABAN"
                        }
                        val answerPlaceholder = when (inputJenis) {
                            "meneruskan" -> "إدخال التكملة..."
                            "melengkapi" -> "المدرسة..."
                            else -> "إدخال الإجابة النموذجية..."
                        }

                        Text(
                            text = answerLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            OutlinedTextField(
                                value = inputJawabanLain,
                                onValueChange = {
                                    activeField = "jawabanLain"
                                    viewModel.setInputJawabanLain(it)
                                },
                                placeholder = { Text(answerPlaceholder, style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 18.sp, textAlign = TextAlign.Right)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_jawaban_lain_field"),
                                minLines = 2,
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Right,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit & Cancel Buttons
                    Button(
                        onClick = {
                            viewModel.saveQuestion(
                                onSuccess = { no, jilid ->
                                    Toast.makeText(context, "Soal No. $no berhasil ditambahkan ke $jilid!", Toast.LENGTH_LONG).show()
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_simpan_soal"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Menyimpan Soal...")
                        } else {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SIMPAN SOAL KE DATABASE", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { viewModel.resetForm(keepSettings = false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kosongkan Formulir")
                    }
                }
            }
        }
    }
}
