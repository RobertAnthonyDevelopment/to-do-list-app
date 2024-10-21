package com.robertclemons.myfirstapp

import androidx.compose.ui.text.font.FontWeight
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import android.app.TimePickerDialog
import android.os.Looper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
        setContent {
            ToDoApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoApp() {
    var taskText by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedDeadline by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedRecurringOption by remember { mutableStateOf("None") }

    var priorityExpanded by remember { mutableStateOf(false) }
    var recurringExpanded by remember { mutableStateOf(false) }

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val selectedDays = remember { mutableStateMapOf<String, Boolean>().apply {
        daysOfWeek.forEach { put(it, false) }
    } }

    val context = LocalContext.current
    val preferences = context.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
    val gson = remember { Gson() }
    val tasks = remember { mutableStateListOf<Task>().apply {
        try {
            addAll(loadTasks(preferences, gson))
        } catch (e: Exception) {
            Log.e("ToDoApp", "Error loading tasks", e)
        }
    } }
    val priorities = listOf("High", "Medium", "Low")
    val recurringOptions = listOf("None", "Daily", "Weekly")

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                // Task input field
                TextField(
                    value = taskText,
                    onValueChange = { taskText = it },
                    label = { Text("Enter task") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Priority dropdown
                Box {
                    OutlinedButton(onClick = { priorityExpanded = true }) {
                        Text(text = "Priority: $selectedPriority")
                    }
                    DropdownMenu(expanded = priorityExpanded, onDismissRequest = { priorityExpanded = false }) {
                        priorities.forEach { priority ->
                            DropdownMenuItem(
                                text = { Text(priority) },
                                onClick = {
                                    selectedPriority = priority
                                    priorityExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date and time pickers
                DatePicker(selectedDate = selectedDeadline) { date -> selectedDeadline = date }
                Spacer(modifier = Modifier.height(16.dp))
                TimePicker(selectedTime = selectedTime) { time -> selectedTime = time }
                Spacer(modifier = Modifier.height(16.dp))

                // Recurring options dropdown
                Box {
                    OutlinedButton(onClick = { recurringExpanded = true }) {
                        Text(text = "Recurring: $selectedRecurringOption")
                    }
                    DropdownMenu(expanded = recurringExpanded, onDismissRequest = { recurringExpanded = false }) {
                        recurringOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedRecurringOption = option
                                    recurringExpanded = false
                                }
                            )
                        }
                    }
                }

                // Days selection for weekly recurring tasks
                if (selectedRecurringOption == "Weekly") {
                    Column {
                        Text("Select days:")
                        daysOfWeek.forEach { day ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedDays[day] == true,
                                    onCheckedChange = { selectedDays[day] = it }
                                )
                                Text(text = day)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add task button
                Button(
                    onClick = {
                        if (taskText.isNotEmpty()) {
                            val newTask = Task(
                                description = taskText,
                                priority = selectedPriority,
                                deadline = selectedDeadline,
                                isRecurring = selectedRecurringOption,
                                daysOfWeek = selectedDays.filter { it.value }.keys.toList(),
                                isCompleted = false
                            )
                            tasks.add(newTask)
                            saveTasks(preferences, gson, tasks)

                            taskText = ""
                            selectedDeadline = ""
                            selectedTime = ""
                            selectedRecurringOption = "None"
                            selectedDays.forEach { (day, _) -> selectedDays[day] = false }
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Add Task")
                }

                // Task completion progress
                val totalTasks = tasks.size
                val completedTasks = tasks.count { it.isCompleted }
                val completionPercentage = if (totalTasks > 0) (completedTasks / totalTasks.toFloat()) else 0f

                LinearProgressIndicator(
                    progress = completionPercentage,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Task Completion: ${(completionPercentage * 100).toInt()}%")
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Display tasks
            itemsIndexed(tasks) { index, task ->
                TaskItem(
                    task = task,
                    onTaskCheckedChange = { isChecked ->
                        tasks[index] = task.copy(isCompleted = isChecked)
                        saveTasks(preferences, gson, tasks)
                    },
                    onDeleteTask = {
                        tasks.removeAt(index)
                        saveTasks(preferences, gson, tasks)
                    },
                    onEditTask = {
                        taskText = task.description
                        selectedPriority = task.priority
                        selectedDeadline = task.deadline
                        selectedRecurringOption = task.isRecurring
                        task.daysOfWeek.forEach { day -> selectedDays[day] = true }
                        tasks.removeAt(index)
                    },
                    onSetReminder = {
                        setTaskReminder(context, task.description, task.deadline, selectedTime)
                    }
                )
            }
        }
    }
}

// Task item component
@Composable
fun TaskItem(
    task: Task,
    onTaskCheckedChange: (Boolean) -> Unit,
    onDeleteTask: () -> Unit,
    onEditTask: () -> Unit,
    onSetReminder: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = task.isCompleted, onCheckedChange = onTaskCheckedChange)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = task.description, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = "Priority: ${task.priority}", fontSize = 14.sp)
            Text(text = "Deadline: ${task.deadline}", fontSize = 14.sp)
            if (task.isRecurring != "None") {
                Text(text = "Recurring: ${task.isRecurring} (${task.daysOfWeek.joinToString()})", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        IconButton(onClick = onEditTask) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Task")
        }
        IconButton(onClick = onSetReminder) {
            Icon(Icons.Default.Notifications, contentDescription = "Set Reminder")
        }
        IconButton(onClick = onDeleteTask) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Task")
        }
    }
}

// Date picker composable
@Composable
fun DatePicker(selectedDate: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    Button(onClick = {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            onDateSelected(formatter.format(selectedCalendar.time))
        }, year, month, day).show()
    }) {
        Text(if (selectedDate.isEmpty()) "Pick Deadline" else selectedDate)
    }
}

// Time picker composable
@Composable
fun TimePicker(selectedTime: String, onTimeSelected: (String) -> Unit) {
    val context = LocalContext.current
    Button(onClick = {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(context, { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            onTimeSelected(timeFormat.format(calendar.time))
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
        timePickerDialog.show()
    }) {
        Text(if (selectedTime.isEmpty()) "Pick Time" else selectedTime)
    }
}

// Function to set a reminder
fun setTaskReminder(context: Context, taskDescription: String, deadline: String, time: String) {
    if (deadline.isNotEmpty() && time.isNotEmpty()) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("task_description", taskDescription)
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val calendar = Calendar.getInstance()
            val date = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault()).parse("$deadline $time")
            if (date != null) {
                calendar.time = date
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e("ToDoApp", "Error setting reminder", e)
        }
    }
}

// BroadcastReceiver to handle reminders
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskDescription = intent.getStringExtra("task_description")
        Toast.makeText(context, "Reminder: $taskDescription", Toast.LENGTH_LONG).show()
    }
}

// Function to save tasks to SharedPreferences
fun saveTasks(preferences: SharedPreferences, gson: Gson, tasks: List<Task>) {
    try {
        val jsonString = gson.toJson(tasks)
        preferences.edit().putString("tasks", jsonString).apply()
    } catch (e: Exception) {
        Log.e("ToDoApp", "Error saving tasks", e)
    }
}

// Function to load tasks from SharedPreferences
fun loadTasks(preferences: SharedPreferences, gson: Gson): List<Task> {
    return try {
        val jsonString = preferences.getString("tasks", null)
        if (jsonString != null) {
            val type = object : TypeToken<List<Task>>() {}.type
            gson.fromJson(jsonString, type)
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("ToDoApp", "Error loading tasks", e)
        emptyList()
    }
}

// Data class for tasks
data class Task(
    val description: String,
    val priority: String,
    val deadline: String,
    val isRecurring: String,
    val daysOfWeek: List<String> = emptyList(),
    var isCompleted: Boolean
)

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ToDoApp()
}
