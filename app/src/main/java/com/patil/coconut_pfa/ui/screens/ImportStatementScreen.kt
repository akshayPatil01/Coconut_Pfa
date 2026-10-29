package com.patil.coconut_pfa.ui.screens

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.patil.coconut_pfa.data.local.dao.AccountDao
import com.patil.coconut_pfa.data.local.dao.SmsItemDao
import com.patil.coconut_pfa.data.local.db.CoconutDatabase
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.DbSmsSource
import com.patil.coconut_pfa.data.local.entity.DbSmsStatus
import com.patil.coconut_pfa.data.local.entity.SmsItemEntity
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.components.CoconutExposedDropdownMenu
import com.patil.coconut_pfa.ui.components.CoconutSnackbarHost
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.domain.sms.SmsProcessingEngine
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.abs

enum class StatementFileType {
    CSV, XLSX, PDF, UNKNOWN
}

enum class AmountMode {
    DEBIT_CREDIT_COLUMNS, SIGNED_AMOUNT
}

@JsonClass(generateAdapter = true)
data class StatementMapping(
    val dateColIndex: Int,
    val payeeColIndex: Int,
    val amountMode: String,
    val debitColIndex: Int,
    val creditColIndex: Int,
    val signedAmountColIndex: Int,
    val dateFormat: String
)

data class ImportedRowData(
    val dateStr: String,
    val dateMillis: Long,
    val payee: String,
    val amountPaise: Long,
    val isDebit: Boolean,
    val rawLine: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportStatementScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val database = remember { CoconutDatabase.getInstance(context) }
    val accountDao = remember { database.accountDao() }
    val smsItemDao = remember { database.smsItemDao() }

    var accounts by remember { mutableStateOf<List<AccountEntity>>(emptyList()) }
    var selectedAccount by remember { mutableStateOf<AccountEntity?>(null) }

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var fileType by remember { mutableStateOf(StatementFileType.UNKNOWN) }

    var rawHeaders by remember { mutableStateOf<List<String>>(emptyList()) }
    var rawDataRows by remember { mutableStateOf<List<List<String>>>(emptyList()) }
    var rawPdfLines by remember { mutableStateOf<List<String>>(emptyList()) }

    var isProcessing by remember { mutableStateOf(false) }

    // Mapping state
    var dateColIndex by remember { mutableStateOf(0) }
    var payeeColIndex by remember { mutableStateOf(1) }
    var amountMode by remember { mutableStateOf(AmountMode.DEBIT_CREDIT_COLUMNS) }
    var debitColIndex by remember { mutableStateOf(2) }
    var creditColIndex by remember { mutableStateOf(3) }
    var signedAmountColIndex by remember { mutableStateOf(2) }
    var dateFormat by remember { mutableStateOf("dd/MM/yyyy") }

    val dateFormats = listOf("dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd", "dd-MMM-yyyy", "dd.MM.yyyy")

    // Load accounts
    LaunchedEffect(Unit) {
        val list = accountDao.getActiveAccountsList()
        accounts = list
        if (list.isNotEmpty() && selectedAccount == null) {
            selectedAccount = list.first()
        }
    }

    // Load saved mapping when selected account changes
    LaunchedEffect(selectedAccount) {
        selectedAccount?.let { acc ->
            loadMappingForAccount(context, acc.name)?.let { mapping ->
                dateColIndex = mapping.dateColIndex
                payeeColIndex = mapping.payeeColIndex
                amountMode = try { AmountMode.valueOf(mapping.amountMode) } catch (e: Exception) { AmountMode.DEBIT_CREDIT_COLUMNS }
                debitColIndex = mapping.debitColIndex
                creditColIndex = mapping.creditColIndex
                signedAmountColIndex = mapping.signedAmountColIndex
                dateFormat = mapping.dateFormat
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            val name = getFileName(context, uri) ?: "statement_file"
            selectedFileName = name
            val lower = name.lowercase()
            fileType = when {
                lower.endsWith(".csv") || lower.endsWith(".txt") -> StatementFileType.CSV
                lower.endsWith(".xlsx") || lower.endsWith(".xls") -> StatementFileType.XLSX
                lower.endsWith(".pdf") -> StatementFileType.PDF
                else -> StatementFileType.CSV
            }

            isProcessing = true
            scope.launch(Dispatchers.IO) {
                if (fileType == StatementFileType.PDF) {
                    processPdfUri(context, uri) { lines, err ->
                        rawPdfLines = lines
                        rawHeaders = listOf("Column A (Date)", "Column B (Description)", "Column C (Amount)")
                        rawDataRows = parsePdfLinesToRows(lines)
                        isProcessing = false
                        if (err != null) {
                            scope.launch { snackbarHostState.showSnackbar("PDF warning: $err. Please review extracted rows carefully.") }
                        }
                    }
                } else {
                    // CSV or text/xlsx fallback
                    try {
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            val pair = processCsvInputStream(inputStream)
                            rawHeaders = pair.first
                            rawDataRows = pair.second
                            isProcessing = false
                        } ?: run {
                            isProcessing = false
                            scope.launch { snackbarHostState.showSnackbar("Failed to open file stream") }
                        }
                    } catch (e: Exception) {
                        isProcessing = false
                        scope.launch { snackbarHostState.showSnackbar("Error reading file: ${e.localizedMessage}") }
                    }
                }
            }
        }
    }

    // Compute preview rows based on current mapping
    val previewRows = remember(rawDataRows, rawPdfLines, fileType, dateColIndex, payeeColIndex, amountMode, debitColIndex, creditColIndex, signedAmountColIndex, dateFormat) {
        if (fileType == StatementFileType.PDF) {
            rawDataRows.mapNotNull { row ->
                if (row.size >= 3) {
                    val dateStr = row.getOrElse(0) { "" }
                    val payee = row.getOrElse(1) { "Statement Transaction" }
                    val amtStr = row.getOrElse(2) { "0" }
                    val dateMillis = parseDateToMillis(dateStr, dateFormat)
                    val pair = parseAmountToPaise(amtStr)
                    ImportedRowData(dateStr, dateMillis, payee, pair.first, pair.second, row.joinToString(" "))
                } else null
            }.take(20)
        } else {
            rawDataRows.mapNotNull { cols ->
                if (cols.isNotEmpty()) {
                    val dateStr = cols.getOrElse(dateColIndex) { "" }
                    val payee = cols.getOrElse(payeeColIndex) { "Statement Transaction" }
                    val (amountPaise, isDebit) = if (amountMode == AmountMode.SIGNED_AMOUNT) {
                        parseAmountToPaise(cols.getOrElse(signedAmountColIndex) { "0" })
                    } else {
                        val drStr = cols.getOrElse(debitColIndex) { "0" }
                        val crStr = cols.getOrElse(creditColIndex) { "0" }
                        val drPaise = parseAmountToPaise(drStr).first
                        val crPaise = parseAmountToPaise(crStr).first
                        if (drPaise > 0) Pair(drPaise, true) else Pair(crPaise, false)
                    }
                    val dateMillis = parseDateToMillis(dateStr, dateFormat)
                    ImportedRowData(dateStr, dateMillis, payee, amountPaise, isDebit, cols.joinToString(", "))
                } else null
            }.take(20)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { CoconutSnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigate(Screen.Settings) }) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Import Bank Statement",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "CSV, XLSX & PDF Statement Parser",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Step 1: Account Selection & File Picker
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Step 1: Select Account & Statement File",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Account Selector Dropdown
                            CoconutExposedDropdownMenu(
                                value = selectedAccount?.name ?: "Select Target Account",
                                options = accounts,
                                onOptionSelected = { selectedAccount = it },
                                optionToString = { it.name },
                                label = "Target Account",
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = {
                                    filePickerLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/pdf", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel", "*/*"))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Rounded.FileOpen, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(selectedFileName ?: "Pick CSV, XLSX, or PDF File")
                            }

                            if (isProcessing) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                Text(
                                    text = "Parsing statement (PDF OCR / CSV)...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (fileType == StatementFileType.PDF) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "⚠️ Warning: PDF results use on-device OCR and may require careful review of extracted rows.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Step 2: Column Mapping & Date Format
                if (selectedFileName != null && fileType != StatementFileType.PDF) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Step 2: Column Mapping & Date Format",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Date Format Picker
                                CoconutExposedDropdownMenu(
                                    value = dateFormat,
                                    options = dateFormats,
                                    onOptionSelected = { dateFormat = it },
                                    label = "Date Format",
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Date Column Dropdown
                                ColumnDropdown(
                                    label = "Date Column",
                                    selectedIndex = dateColIndex,
                                    headers = rawHeaders,
                                    onSelected = { dateColIndex = it }
                                )

                                // Payee Column Dropdown
                                ColumnDropdown(
                                    label = "Payee / Description Column",
                                    selectedIndex = payeeColIndex,
                                    headers = rawHeaders,
                                    onSelected = { payeeColIndex = it }
                                )

                                // Amount Mode Dropdown
                                CoconutExposedDropdownMenu(
                                    value = if (amountMode == AmountMode.DEBIT_CREDIT_COLUMNS) "Separate Debit & Credit Columns" else "Single Signed Amount Column",
                                    options = AmountMode.entries,
                                    onOptionSelected = { amountMode = it },
                                    optionToString = { mode ->
                                        if (mode == AmountMode.DEBIT_CREDIT_COLUMNS) "Separate Debit & Credit Columns" else "Single Signed Amount Column"
                                    },
                                    label = "Amount Structure",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                )

                                if (amountMode == AmountMode.DEBIT_CREDIT_COLUMNS) {
                                    ColumnDropdown(
                                        label = "Debit Column",
                                        selectedIndex = debitColIndex,
                                        headers = rawHeaders,
                                        onSelected = { debitColIndex = it }
                                    )
                                    ColumnDropdown(
                                        label = "Credit Column",
                                        selectedIndex = creditColIndex,
                                        headers = rawHeaders,
                                        onSelected = { creditColIndex = it }
                                    )
                                } else {
                                    ColumnDropdown(
                                        label = "Signed Amount Column",
                                        selectedIndex = signedAmountColIndex,
                                        headers = rawHeaders,
                                        onSelected = { signedAmountColIndex = it }
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedButton(
                                    onClick = {
                                        selectedAccount?.let { acc ->
                                            val mapping = StatementMapping(
                                                dateColIndex = dateColIndex,
                                                payeeColIndex = payeeColIndex,
                                                amountMode = amountMode.name,
                                                debitColIndex = debitColIndex,
                                                creditColIndex = creditColIndex,
                                                signedAmountColIndex = signedAmountColIndex,
                                                dateFormat = dateFormat
                                            )
                                            saveMappingForAccount(context, acc.name, mapping)
                                            scope.launch { snackbarHostState.showSnackbar("Mapping saved for ${acc.name}!") }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Rounded.Save, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Save Mapping for this Bank/Account")
                                }
                            }
                        }
                    }
                }

                // Step 3: Preview Rows & Send to Review Inbox
                if (selectedFileName != null && previewRows.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Step 3: Preview Parsed Rows (${previewRows.size} shown)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                previewRows.forEach { row ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = row.payee, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text(text = "${row.dateStr} • ${if (row.isDebit) "Debit" else "Credit"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                        }

                                        AmountText(
                                            amountInPaise = row.amountPaise,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!row.isDebit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                        )
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        scope.launch(Dispatchers.IO) {
                                            val targetAcc = selectedAccount
                                            var importedCount = 0
                                            var duplicateCount = 0

                                            val rowsToImport = if (fileType == StatementFileType.PDF) {
                                                rawDataRows.mapNotNull { row ->
                                                    if (row.size >= 3) {
                                                        val dateStr = row.getOrElse(0) { "" }
                                                        val payee = row.getOrElse(1) { "Statement Transaction" }
                                                        val amtStr = row.getOrElse(2) { "0" }
                                                        val dateMillis = parseDateToMillis(dateStr, dateFormat)
                                                        val pair = parseAmountToPaise(amtStr)
                                                        ImportedRowData(dateStr, dateMillis, payee, pair.first, pair.second, row.joinToString(" "))
                                                    } else null
                                                }
                                            } else {
                                                rawDataRows.mapNotNull { cols ->
                                                    if (cols.isNotEmpty()) {
                                                        val dateStr = cols.getOrElse(dateColIndex) { "" }
                                                        val payee = cols.getOrElse(payeeColIndex) { "Statement Transaction" }
                                                        val (amountPaise, isDebit) = if (amountMode == AmountMode.SIGNED_AMOUNT) {
                                                            parseAmountToPaise(cols.getOrElse(signedAmountColIndex) { "0" })
                                                        } else {
                                                            val drStr = cols.getOrElse(debitColIndex) { "0" }
                                                            val crStr = cols.getOrElse(creditColIndex) { "0" }
                                                            val drPaise = parseAmountToPaise(drStr).first
                                                            val crPaise = parseAmountToPaise(crStr).first
                                                            if (drPaise > 0) Pair(drPaise, true) else Pair(crPaise, false)
                                                        }
                                                        val dateMillis = parseDateToMillis(dateStr, dateFormat)
                                                        ImportedRowData(dateStr, dateMillis, payee, amountPaise, isDebit, cols.joinToString(", "))
                                                    } else null
                                                }
                                            }

                                            for (row in rowsToImport) {
                                                val body = "Imported Statement: ${row.payee} on ${row.dateStr} - ${if (row.isDebit) "Dr" else "Cr"} ${row.amountPaise / 100.0}"
                                                val bodyHash = SmsProcessingEngine.hashBody(body)
                                                val existing = smsItemDao.getSmsByHash(bodyHash)
                                                if (existing != null) {
                                                    duplicateCount++
                                                    continue
                                                }

                                                val smsEntity = SmsItemEntity(
                                                    id = UUID.randomUUID().toString(),
                                                    sender = targetAcc?.name ?: "Statement Import",
                                                    receivedAt = row.dateMillis,
                                                    body = body,
                                                    bodyHash = bodyHash,
                                                    parsedType = if (row.isDebit) "DEBIT" else "CREDIT",
                                                    amountPaise = row.amountPaise,
                                                    merchant = row.payee,
                                                    matchedAccountId = targetAcc?.id,
                                                    status = DbSmsStatus.PENDING,
                                                    source = DbSmsSource.IMPORT
                                                )
                                                smsItemDao.insert(smsEntity)
                                                importedCount++
                                            }

                                            withContext(Dispatchers.Main) {
                                                snackbarHostState.showSnackbar("Imported $importedCount transactions ($duplicateCount duplicates skipped).")
                                                onNavigate(Screen.ReviewInbox)
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Send All Rows to Review Inbox")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnDropdown(
    label: String,
    selectedIndex: Int,
    headers: List<String>,
    onSelected: (Int) -> Unit
) {
    val currentText = if (selectedIndex in headers.indices) headers[selectedIndex] else "Column $selectedIndex"
    CoconutExposedDropdownMenu(
        value = currentText,
        options = headers.indices.toList(),
        onOptionSelected = { onSelected(it) },
        optionToString = { idx -> if (idx in headers.indices) headers[idx] else "Column $idx" },
        label = label,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

private fun getFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }
        }
    }
    if (name == null) {
        name = uri.path?.let { path ->
            val cut = path.lastIndexOf('/')
            if (cut != -1) path.substring(cut + 1) else path
        }
    }
    return name
}

private fun processCsvInputStream(
    inputStream: java.io.InputStream
): Pair<List<String>, List<List<String>>> {
    val reader = BufferedReader(InputStreamReader(inputStream))
    val lines = reader.readLines().map { it.trim() }.filter { it.isNotBlank() }
    val csvRows = lines.map { parseCsvLine(it) }
    val headers = if (csvRows.isNotEmpty()) csvRows.first() else emptyList()
    val dataRows = if (csvRows.size > 1) csvRows.drop(1) else emptyList()

    val actualHeaders = if (headers.isEmpty() || headers.all { it.matches(Regex("\\d+")) }) {
        if (csvRows.isNotEmpty()) (0 until csvRows.first().size).map { "Column ${Char('A'.code + it)}" } else emptyList()
    } else {
        headers
    }
    return Pair(actualHeaders, if (actualHeaders == headers) dataRows else csvRows)
}

private fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    var cur = StringBuilder()
    var inQuotes = false
    for (char in line) {
        when {
            char == '"' -> inQuotes = !inQuotes
            char == ',' && !inQuotes -> {
                result.add(cur.toString().trim())
                cur = StringBuilder()
            }
            else -> cur.append(char)
        }
    }
    result.add(cur.toString().trim())
    return result
}

private fun processPdfUri(
    context: Context,
    uri: Uri,
    onResult: (List<String>, String?) -> Unit
) {
    try {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: run {
            onResult(emptyList(), "Failed to open PDF")
            return
        }
        val renderer = PdfRenderer(pfd)
        val allLines = mutableListOf<String>()
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val pageCount = renderer.pageCount
        var completed = 0

        for (i in 0 until pageCount) {
            val page = renderer.openPage(i)
            val bitmap = android.graphics.Bitmap.createBitmap(page.width * 2, page.height * 2, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
            page.close()

            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    allLines.addAll(visionText.text.lines().map { it.trim() }.filter { it.isNotBlank() })
                    completed++
                    if (completed == pageCount) {
                        try {
                            renderer.close()
                            pfd.close()
                        } catch (e: Exception) {}
                        onResult(allLines, null)
                    }
                }
                .addOnFailureListener { e ->
                    completed++
                    if (completed == pageCount) {
                        try {
                            renderer.close()
                            pfd.close()
                        } catch (e: Exception) {}
                        onResult(allLines, e.localizedMessage)
                    }
                }
        }
        if (pageCount == 0) {
            renderer.close()
            pfd.close()
            onResult(emptyList(), "PDF has 0 pages")
        }
    } catch (e: Exception) {
        onResult(emptyList(), e.localizedMessage)
    }
}

private fun parsePdfLinesToRows(lines: List<String>): List<List<String>> {
    val dateRegex = Regex("\\b\\d{1,2}[/\\-]\\d{1,2}[/\\-]\\d{2,4}\\b|\\b\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{2,4}\\b")
    val amountRegex = Regex("\\b(?:[₹$€Rs.]\\s*)?(?:\\d{1,3}(?:,\\d{2,3})+|\\d+)(?:\\.\\d{1,2})?\\b")

    val rows = mutableListOf<List<String>>()
    for (line in lines) {
        val hasDate = dateRegex.containsMatchIn(line)
        val hasAmount = amountRegex.containsMatchIn(line)
        if (hasDate && hasAmount) {
            val dateMatch = dateRegex.find(line)?.value ?: "01/01/2024"
            val amountMatch = amountRegex.findAll(line).lastOrNull()?.value ?: "0.00"
            val payee = line.replace(dateMatch, "").replace(amountMatch, "").trim(' ', '-', ':', '|')
            rows.add(listOf(dateStrMatch(dateMatch), payee.ifBlank { "PDF Statement Txn" }, amountMatch))
        }
    }
    return rows
}

private fun dateStrMatch(str: String): String = str

private fun parseDateToMillis(dateStr: String, formatPattern: String): Long {
    try {
        val formatter = DateTimeFormatter.ofPattern(formatPattern, java.util.Locale.ENGLISH)
        val date = LocalDate.parse(dateStr, formatter)
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    } catch (e: Exception) {
        try {
            val date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e2: Exception) {
            return System.currentTimeMillis()
        }
    }
}

private fun parseAmountToPaise(amountStr: String): Pair<Long, Boolean> {
    val cleaned = amountStr.replace(Regex("[₹$€Rs.,\\s]"), "").trim()
    val isNegative = amountStr.contains("-") || amountStr.contains("Dr", ignoreCase = true)
    val numStr = amountStr.replace(Regex("[^0-9.\\-]"), "").trim()
    val value = numStr.toDoubleOrNull() ?: 0.0
    val paise = (abs(value) * 100).toLong()
    val isDebit = isNegative || value < 0 || amountStr.contains("Dr", ignoreCase = true)
    return Pair(paise, isDebit)
}

private fun saveMappingForAccount(context: Context, accountName: String, mapping: StatementMapping) {
    try {
        val prefs = context.getSharedPreferences("statement_mappings", Context.MODE_PRIVATE)
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val json = moshi.adapter(StatementMapping::class.java).toJson(mapping)
        prefs.edit().putString(accountName, json).apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun loadMappingForAccount(context: Context, accountName: String): StatementMapping? {
    try {
        val prefs = context.getSharedPreferences("statement_mappings", Context.MODE_PRIVATE)
        val json = prefs.getString(accountName, null) ?: return null
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        return moshi.adapter(StatementMapping::class.java).fromJson(json)
    } catch (e: Exception) {
        return null
    }
}

@Preview(showBackground = true)
@Composable
private fun ImportStatementScreenPreview() {
    Coconut_PfaTheme {
        ImportStatementScreen(onNavigate = {})
    }
}
