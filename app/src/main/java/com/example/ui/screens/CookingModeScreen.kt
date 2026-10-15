package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Ingredient
import com.example.data.model.Recipe
import com.example.ui.SavorViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Data holder for a single step's interactive timer
 */
data class StepTimerState(
    val stepIndex: Int,
    var totalSeconds: Int,
    var remainingSeconds: Int,
    var isRunning: Boolean = false,
    var isFinished: Boolean = false
)

/**
 * Parse timer seconds from step description or recipe metadata
 */
fun parseStepTimerSeconds(stepText: String, metadataMinutes: Int?): Int {
    if (metadataMinutes != null && metadataMinutes > 0) {
        return metadataMinutes * 60
    }
    val minMatch = Regex("""(\d+)\s*(?:minute|min|minutes|mins)""", RegexOption.IGNORE_CASE).find(stepText)
    if (minMatch != null) {
        val mins = minMatch.groupValues[1].toIntOrNull() ?: 3
        return mins * 60
    }
    val secMatch = Regex("""(\d+)\s*(?:second|sec|seconds|secs)""", RegexOption.IGNORE_CASE).find(stepText)
    if (secMatch != null) {
        return secMatch.groupValues[1].toIntOrNull() ?: 45
    }
    val hourMatch = Regex("""(\d+)\s*(?:hour|hr|hours|hrs)""", RegexOption.IGNORE_CASE).find(stepText)
    if (hourMatch != null) {
        val hours = hourMatch.groupValues[1].toIntOrNull() ?: 1
        return hours * 3600
    }
    return 180 // default 3 minutes
}

fun formatTimerDisplay(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

@Composable
fun CookingModeScreen(
    recipeId: String,
    initialStepIndex: Int = 0,
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val recipes by viewModel.recipes.collectAsState()
    val recipe = recipes.find { it.id == recipeId }

    if (recipe == null) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text("Recipe not found.")
        }
        return
    }

    val context = LocalContext.current
    var keepScreenAwake by remember { mutableStateOf(true) }

    // Keep screen awake while in cooking mode
    DisposableEffect(keepScreenAwake) {
        val window = (context as? Activity)?.window
        if (keepScreenAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val totalSteps = recipe.steps.size
    var currentStepIndex by remember {
        mutableStateOf(initialStepIndex.coerceIn(0, (totalSteps - 1).coerceAtLeast(0)))
    }

    // View mode: Focus on single step vs. All steps with timers overview
    var isOverviewMode by remember { mutableStateOf(false) }

    // Ingredients drawer toggle
    var showIngredientsDrawer by remember { mutableStateOf(false) }

    // Step completion tracking
    val completedSteps = remember { mutableStateMapOf<Int, Boolean>() }

    // Step Timers map for all steps in the recipe
    val stepTimers = remember(recipe) {
        val map = mutableStateMapOf<Int, StepTimerState>()
        recipe.steps.forEachIndexed { index, stepText ->
            val metaMin = recipe.stepTimersMinutes.getOrNull(index)
            val initialSec = parseStepTimerSeconds(stepText, metaMin)
            map[index] = StepTimerState(
                stepIndex = index,
                totalSeconds = initialSec,
                remainingSeconds = initialSec,
                isRunning = false,
                isFinished = false
            )
        }
        map
    }

    // Active finished timer alert state
    var finishedTimerAlertStep by remember { mutableStateOf<Int?>(null) }

    // Vibrate trigger helper
    fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 350, 150, 350, 150, 500), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 350, 150, 350, 150, 500), -1)
            }
        } catch (_: Exception) {}
    }

    // Master timer ticker for all active step timers
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            stepTimers.keys.forEach { index ->
                val timer = stepTimers[index]
                if (timer != null && timer.isRunning && timer.remainingSeconds > 0) {
                    val newRemaining = timer.remainingSeconds - 1
                    val isNowFinished = newRemaining == 0
                    stepTimers[index] = timer.copy(
                        remainingSeconds = newRemaining,
                        isRunning = !isNowFinished,
                        isFinished = isNowFinished
                    )
                    if (isNowFinished) {
                        finishedTimerAlertStep = index
                        triggerVibration()
                    }
                }
            }
        }
    }

    // Background running timers count
    val runningTimersCount = stepTimers.values.count { it.isRunning }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 3.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.navigateBack() },
                            modifier = Modifier.testTag("cooking_mode_exit_button")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Cooking Mode")
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.OutdoorGrill,
                                    contentDescription = null,
                                    tint = SavorAccentOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "COOKING MODE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = SavorAccentOrange
                                )
                            }
                            Text(
                                text = recipe.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // View Switcher (Focus vs All Steps Overview)
                            IconButton(
                                onClick = { isOverviewMode = !isOverviewMode },
                                modifier = Modifier.testTag("cooking_mode_view_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isOverviewMode) Icons.Default.ViewAgenda else Icons.Default.FormatListNumbered,
                                    contentDescription = if (isOverviewMode) "Focus Mode" else "All Steps View",
                                    tint = SavorAccentOrange
                                )
                            }

                            // Awake Mode Toggle (Keeps display active)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (keepScreenAwake) SavorGoldenYellow.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { keepScreenAwake = !keepScreenAwake }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (keepScreenAwake) Icons.Default.WbSunny else Icons.Default.NightlightRound,
                                        contentDescription = "Awake Toggle",
                                        tint = if (keepScreenAwake) SavorGoldenYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (keepScreenAwake) "Awake" else "Dim",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (keepScreenAwake) SavorGoldenYellow else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Horizontal Step Tabs Carousel
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(recipe.steps) { idx, _ ->
                            val isSelected = currentStepIndex == idx && !isOverviewMode
                            val isDone = completedSteps[idx] == true
                            val timer = stepTimers[idx]
                            val isRunning = timer?.isRunning == true
                            val isFinished = timer?.isFinished == true

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = when {
                                    isSelected -> SavorAccentOrange
                                    isFinished -> SavorCrimson.copy(alpha = 0.15f)
                                    isRunning -> SavorGoldenYellow.copy(alpha = 0.25f)
                                    isDone -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isSelected -> SavorAccentOrange
                                        isFinished -> SavorCrimson
                                        isRunning -> SavorGoldenYellow
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    }
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        currentStepIndex = idx
                                        isOverviewMode = false
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    if (isDone) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else SavorGoldenYellow,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    } else if (isRunning) {
                                        Icon(
                                            imageVector = Icons.Default.HourglassTop,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else SavorGoldenYellow,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }

                                    Text(
                                        text = "Step ${idx + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )

                                    if (timer != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = if (timer.isRunning || timer.remainingSeconds != timer.totalSeconds) {
                                                    formatTimerDisplay(timer.remainingSeconds)
                                                } else {
                                                    "${timer.totalSeconds / 60}m"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = if (isSelected) Color.White else if (timer.isRunning) SavorAccentOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Step Progress Indicator
                    LinearProgressIndicator(
                        progress = { (currentStepIndex + 1).toFloat() / totalSteps.toFloat() },
                        color = SavorAccentOrange,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentStepIndex > 0) {
                                currentStepIndex--
                                isOverviewMode = false
                            }
                        },
                        enabled = currentStepIndex > 0,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prev Step")
                    }

                    // Ingredients Quick Drawer Button
                    IconButton(
                        onClick = { showIngredientsDrawer = !showIngredientsDrawer },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (showIngredientsDrawer) SavorGoldenYellow else MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ListAlt,
                            contentDescription = "View Ingredients",
                            tint = if (showIngredientsDrawer) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            completedSteps[currentStepIndex] = true
                            if (currentStepIndex < totalSteps - 1) {
                                currentStepIndex++
                                isOverviewMode = false
                            } else {
                                viewModel.showToast("🎉 Recipe completed! You mastered this dish!")
                                viewModel.navigateBack()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("cooking_mode_next_button")
                    ) {
                        Text(
                            text = if (currentStepIndex == totalSteps - 1) "Finish Recipe 🎉" else "Done & Next",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        },
        modifier = modifier.testTag("cooking_mode_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isOverviewMode) {
                // ==================== OVERVIEW MODE ====================
                // Shows all steps in the recipe with clickable interactive timers for each
                AllStepsOverviewView(
                    recipe = recipe,
                    currentStepIndex = currentStepIndex,
                    stepTimers = stepTimers,
                    completedSteps = completedSteps,
                    onSelectStep = { idx ->
                        currentStepIndex = idx
                        isOverviewMode = false
                    },
                    onToggleStepDone = { idx ->
                        completedSteps[idx] = !(completedSteps[idx] ?: false)
                    },
                    onToggleTimer = { idx ->
                        val timer = stepTimers[idx]
                        if (timer != null) {
                            stepTimers[idx] = timer.copy(
                                isRunning = !timer.isRunning,
                                isFinished = false
                            )
                        }
                    },
                    onResetTimer = { idx ->
                        val timer = stepTimers[idx]
                        if (timer != null) {
                            stepTimers[idx] = timer.copy(
                                remainingSeconds = timer.totalSeconds,
                                isRunning = false,
                                isFinished = false
                            )
                        }
                    },
                    onAdjustTimer = { idx, addSeconds ->
                        val timer = stepTimers[idx]
                        if (timer != null) {
                            val newTotal = (timer.totalSeconds + addSeconds).coerceAtLeast(30)
                            val newRemaining = (timer.remainingSeconds + addSeconds).coerceAtLeast(1)
                            stepTimers[idx] = timer.copy(
                                totalSeconds = newTotal,
                                remainingSeconds = newRemaining,
                                isFinished = false
                            )
                        }
                    }
                )
            } else {
                // ==================== FOCUS MODE ====================
                // Prominent single-step view with hero interactive clickable timer widget
                FocusStepView(
                    recipe = recipe,
                    stepIndex = currentStepIndex,
                    stepTimer = stepTimers[currentStepIndex],
                    isStepDone = completedSteps[currentStepIndex] ?: false,
                    runningTimersCount = runningTimersCount,
                    stepTimers = stepTimers,
                    onToggleStepDone = {
                        completedSteps[currentStepIndex] = !(completedSteps[currentStepIndex] ?: false)
                    },
                    onToggleTimer = {
                        val timer = stepTimers[currentStepIndex]
                        if (timer != null) {
                            stepTimers[currentStepIndex] = timer.copy(
                                isRunning = !timer.isRunning,
                                isFinished = false
                            )
                        }
                    },
                    onResetTimer = {
                        val timer = stepTimers[currentStepIndex]
                        if (timer != null) {
                            stepTimers[currentStepIndex] = timer.copy(
                                remainingSeconds = timer.totalSeconds,
                                isRunning = false,
                                isFinished = false
                            )
                        }
                    },
                    onAdjustTimer = { addSeconds ->
                        val timer = stepTimers[currentStepIndex]
                        if (timer != null) {
                            val newTotal = (timer.totalSeconds + addSeconds).coerceAtLeast(30)
                            val newRemaining = (timer.remainingSeconds + addSeconds).coerceAtLeast(1)
                            stepTimers[currentStepIndex] = timer.copy(
                                totalSeconds = newTotal,
                                remainingSeconds = newRemaining,
                                isFinished = false
                            )
                        }
                    },
                    onJumpToStep = { idx ->
                        currentStepIndex = idx
                    }
                )
            }

            // Slide-up Ingredients Drawer
            AnimatedVisibility(
                visible = showIngredientsDrawer,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 16.dp,
                    shadowElevation = 16.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Kitchen,
                                    contentDescription = null,
                                    tint = SavorAccentOrange
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Recipe Ingredients Checklist",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            IconButton(onClick = { showIngredientsDrawer = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Drawer")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            itemsIndexed(recipe.ingredients) { _, ing ->
                                var isChecked by remember { mutableStateOf(false) }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isChecked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable { isChecked = !isChecked }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { isChecked = it },
                                            colors = CheckboxDefaults.colors(checkedColor = SavorAccentOrange)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = ing.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${ing.amount} ${ing.unit}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = SavorAccentOrange
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Timer Finished Alert Dialog / Celebration
            finishedTimerAlertStep?.let { stepIdx ->
                AlertDialog(
                    onDismissRequest = { finishedTimerAlertStep = null },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = SavorAccentOrange,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "⏰ Step ${stepIdx + 1} Timer Complete!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Your timer for Step ${stepIdx + 1} has reached 00:00. Time to check on the dish!",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = recipe.steps.getOrElse(stepIdx) { "" },
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                completedSteps[stepIdx] = true
                                finishedTimerAlertStep = null
                                if (stepIdx < totalSteps - 1) {
                                    currentStepIndex = stepIdx + 1
                                    isOverviewMode = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange)
                        ) {
                            Text("Mark Done & Next Step")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                // Add 2 more minutes
                                val timer = stepTimers[stepIdx]
                                if (timer != null) {
                                    stepTimers[stepIdx] = timer.copy(
                                        totalSeconds = 120,
                                        remainingSeconds = 120,
                                        isRunning = true,
                                        isFinished = false
                                    )
                                }
                                finishedTimerAlertStep = null
                            }
                        ) {
                            Text("+2 Mins", color = SavorAccentOrange)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Focus View: Single step centered, giant typography, large circular/linear interactive timer widget
 */
@Composable
fun FocusStepView(
    recipe: Recipe,
    stepIndex: Int,
    stepTimer: StepTimerState?,
    isStepDone: Boolean,
    runningTimersCount: Int,
    stepTimers: Map<Int, StepTimerState>,
    onToggleStepDone: () -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onAdjustTimer: (Int) -> Unit,
    onJumpToStep: (Int) -> Unit
) {
    val totalSteps = recipe.steps.size
    val stepText = recipe.steps.getOrElse(stepIndex) { "Follow preparation instructions." }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        // Floating Background Timers Banner (if other step timers are running)
        val otherRunningTimers = stepTimers.filter { it.key != stepIndex && it.value.isRunning }
        if (otherRunningTimers.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SavorGoldenYellow.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, SavorGoldenYellow.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = null,
                                tint = SavorGoldenYellow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${otherRunningTimers.size} timer${if (otherRunningTimers.size > 1) "s" else ""} running on other steps",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // First other timer pill
                        otherRunningTimers.entries.firstOrNull()?.let { (otherIdx, timerState) ->
                            TextButton(
                                onClick = { onJumpToStep(otherIdx) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Step ${otherIdx + 1}: ${formatTimerDisplay(timerState.remainingSeconds)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SavorAccentOrange
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hero Step Instructions Card
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isStepDone) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(
                    1.dp,
                    if (isStepDone) SavorGoldenYellow.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("step_instruction_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isStepDone) SavorGoldenYellow else SavorAccentOrange
                                    )
                            ) {
                                if (isStepDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${stepIndex + 1}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "STEP ${stepIndex + 1} OF $totalSteps",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = SavorAccentOrange
                                )
                                Text(
                                    text = if (isStepDone) "Completed" else "In Progress",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                    color = if (isStepDone) SavorGoldenYellow else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Mark step as completed checkbox button
                        OutlinedButton(
                            onClick = onToggleStepDone,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isStepDone) SavorGoldenYellow.copy(alpha = 0.15f) else Color.Transparent
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isStepDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isStepDone) SavorGoldenYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isStepDone) "Done" else "Mark Done",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Large, High-Legibility Instruction Text for Kitchen Distance
                    Text(
                        text = stepText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp,
                            lineHeight = 30.sp,
                            fontWeight = FontWeight.Normal,
                            textDecoration = if (isStepDone) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (isStepDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // ==================== HERO CLICKABLE TIMER WIDGET ====================
        item {
            Spacer(modifier = Modifier.height(18.dp))

            if (stepTimer != null) {
                val isRunning = stepTimer.isRunning
                val isFinished = stepTimer.isFinished
                val remaining = stepTimer.remainingSeconds
                val total = stepTimer.totalSeconds.coerceAtLeast(1)
                val progress = (remaining.toFloat() / total.toFloat()).coerceIn(0f, 1f)

                // Pulsing animation for running timer
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isRunning) 1.03f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "timer_pulse"
                )

                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isFinished -> SavorCrimson.copy(alpha = 0.08f)
                            isRunning -> SavorAccentOrange.copy(alpha = 0.06f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        2.dp,
                        when {
                            isFinished -> SavorCrimson
                            isRunning -> SavorAccentOrange
                            else -> SavorGoldenYellow.copy(alpha = 0.6f)
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isRunning) 6.dp else 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(pulseScale)
                        .testTag("interactive_step_timer_widget")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(22.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isFinished -> SavorCrimson
                                                isRunning -> SavorAccentOrange
                                                else -> SavorGoldenYellow
                                            }
                                        )
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isFinished -> Icons.Default.NotificationsActive
                                            isRunning -> Icons.Default.Timer
                                            else -> Icons.Default.Timer
                                        },
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Step ${stepIndex + 1} Interactive Timer",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            // Status badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    isFinished -> SavorCrimson.copy(alpha = 0.2f)
                                    isRunning -> SavorAccentOrange.copy(alpha = 0.2f)
                                    remaining < total -> SavorGoldenYellow.copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Text(
                                    text = when {
                                        isFinished -> "FINISHED 🔔"
                                        isRunning -> "RUNNING ⏱️"
                                        remaining < total -> "PAUSED ⏸️"
                                        else -> "READY TO COOK"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when {
                                        isFinished -> SavorCrimson
                                        isRunning -> SavorAccentOrange
                                        remaining < total -> SavorGoldenYellow
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Clickable Countdown Display (Tap to Start/Pause)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    when {
                                        isFinished -> SavorCrimson.copy(alpha = 0.12f)
                                        isRunning -> SavorAccentOrange.copy(alpha = 0.1f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    }
                                )
                                .clickable { onToggleTimer() }
                                .padding(vertical = 18.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formatTimerDisplay(remaining),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontSize = 54.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp
                                    ),
                                    color = when {
                                        isFinished -> SavorCrimson
                                        remaining <= 10 && isRunning -> SavorCrimson
                                        isRunning -> SavorAccentOrange
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )

                                Text(
                                    text = if (isRunning) "Tap to pause" else "Tap to ${if (remaining < total) "resume" else "start"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Real-time Progress Bar
                        LinearProgressIndicator(
                            progress = { progress },
                            color = when {
                                isFinished -> SavorCrimson
                                remaining <= 10 && isRunning -> SavorCrimson
                                else -> SavorAccentOrange
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Main Control Action Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = onToggleTimer,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRunning) SavorCrimson else SavorAccentOrange
                                ),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(52.dp)
                                    .testTag("step_timer_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isRunning) "Pause Timer" else if (remaining < total) "Resume Timer" else "Start Timer",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            OutlinedButton(
                                onClick = onResetTimer,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("step_timer_reset_button")
                            ) {
                                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset")
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Time Adjuster Presets (-1m, +30s, +1m, +3m, +5m)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                -60 to "-1m",
                                30 to "+30s",
                                60 to "+1m",
                                180 to "+3m",
                                300 to "+5m"
                            ).forEach { (seconds, label) ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onAdjustTimer(seconds) }
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Kitchen Tips & Chef Advice
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp)) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = SavorGoldenYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Chef Cooking Tip",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Keep your timers running even if you switch steps to prep upcoming ingredients. Savor continues tracking cooking steps in the background.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

/**
 * All Steps Overview View: Shows all recipe steps at once, each featuring its own clickable timer control
 */
@Composable
fun AllStepsOverviewView(
    recipe: Recipe,
    currentStepIndex: Int,
    stepTimers: Map<Int, StepTimerState>,
    completedSteps: Map<Int, Boolean>,
    onSelectStep: (Int) -> Unit,
    onToggleStepDone: (Int) -> Unit,
    onToggleTimer: (Int) -> Unit,
    onResetTimer: (Int) -> Unit,
    onAdjustTimer: (Int, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "All Steps & Timers Overview",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${recipe.steps.size} steps",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Tap any timer to start tracking that step directly, or tap a card to enter Focus Mode.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        itemsIndexed(recipe.steps) { index, stepText ->
            val isDone = completedSteps[index] == true
            val timer = stepTimers[index]
            val isCurrent = currentStepIndex == index
            val isRunning = timer?.isRunning == true
            val isFinished = timer?.isFinished == true
            val remaining = timer?.remainingSeconds ?: 180

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isFinished -> SavorCrimson.copy(alpha = 0.08f)
                        isRunning -> SavorAccentOrange.copy(alpha = 0.08f)
                        isDone -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        isCurrent -> MaterialTheme.colorScheme.surface
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(
                    if (isRunning || isCurrent) 2.dp else 1.dp,
                    when {
                        isFinished -> SavorCrimson
                        isRunning -> SavorAccentOrange
                        isCurrent -> SavorGoldenYellow
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onSelectStep(index) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header: Step number, title, and Mark Done button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isDone -> SavorGoldenYellow
                                            isRunning -> SavorAccentOrange
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isRunning) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Step ${index + 1}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        IconButton(
                            onClick = { onToggleStepDone(index) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Toggle Done",
                                tint = if (isDone) SavorGoldenYellow else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stepText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Step Clickable Timer Bar
                    if (timer != null) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                isFinished -> SavorCrimson.copy(alpha = 0.15f)
                                isRunning -> SavorAccentOrange.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isFinished -> SavorCrimson
                                    isRunning -> SavorAccentOrange
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isRunning) Icons.Default.HourglassTop else Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = if (isRunning) SavorAccentOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = formatTimerDisplay(remaining),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = if (isRunning) SavorAccentOrange else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = when {
                                                isFinished -> "Complete 🔔"
                                                isRunning -> "Counting down..."
                                                else -> "Ready (${timer.totalSeconds / 60}m)"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // +1 Min button
                                    FilledTonalButton(
                                        onClick = { onAdjustTimer(index, 60) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("+1m", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    }

                                    // Play / Pause Button
                                    Button(
                                        onClick = { onToggleTimer(index) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isRunning) SavorCrimson else SavorAccentOrange
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("overview_step_timer_toggle_$index")
                                    ) {
                                        Icon(
                                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isRunning) "Pause" else "Start",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    // Reset button
                                    if (remaining != timer.totalSeconds || isFinished) {
                                        IconButton(
                                            onClick = { onResetTimer(index) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.RestartAlt,
                                                contentDescription = "Reset Timer",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
