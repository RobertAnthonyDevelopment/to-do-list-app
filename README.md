
# To-Do List App

This is a simple **To-Do List** application built using **Kotlin** and **Jetpack Compose** for Android. It allows users to manage their tasks by adding tasks with deadlines, priorities, and recurring options, along with reminders. This project demonstrates the use of modern Android development practices with Compose, SharedPreferences for local storage, and AlarmManager for reminders.

## Features

- **Add Tasks**: Enter tasks with a description, priority, deadline, and set recurring options.
- **Task Priorities**: Set priority for each task as High, Medium, or Low.
- **Deadlines**: Add a specific date and time for task deadlines.
- **Recurring Tasks**: Set tasks to repeat daily or weekly with control over which days of the week they recur.
- **Reminders**: Set reminders for tasks based on the deadline and time.
- **Task Completion Progress**: See the percentage of tasks completed using a progress bar.
- **Sort Tasks**: Sort tasks by priority or deadline.
- **Persistent Storage**: Tasks are saved locally using SharedPreferences.
- **Notifications**: Get reminders for tasks via the system notification system.
- **Edit/Delete Tasks**: Easily modify or remove tasks from the list.

## Project Structure

- **MainActivity**: Entry point for the app, which sets up the main `ToDoApp` composable.
- **ToDoApp**: The main composable function that holds the UI and logic for managing tasks.
- **TaskItem**: Composable for rendering each task in the list.
- **DatePicker & TimePicker**: Custom date and time pickers for selecting task deadlines.
- **ReminderReceiver**: Broadcast receiver that triggers reminders for tasks.
- **SharedPreferences**: Used to store and load tasks locally on the device.

## How to Run

1. Clone the repository:
   ```bash
   git clone git@github.com:RobertAnthonyDevelopment/to-do-list-app.git
   ```

2. Open the project in **Android Studio**.

3. Build and run the project on an Android device or emulator.

## Key Components

### Task Data Model

```kotlin
data class Task(
    val description: String,
    val priority: String,
    val deadline: String,
    val isRecurring: String,
    val daysOfWeek: List<String> = emptyList(),
    var isCompleted: Boolean
)
```

### Main Features

- **Task Management**: Users can add tasks with a description, priority (High, Medium, Low), and set deadlines.
- **Recurring Options**: Tasks can be marked as recurring either daily or weekly with selected days of the week.
- **Reminders**: The app uses the Android AlarmManager to trigger notifications for tasks based on their deadline and time.
- **Persistent Data**: Tasks are stored in SharedPreferences as JSON.

### Persistent Storage Example

- **Saving Tasks**:
  ```kotlin
  fun saveTasks(preferences: SharedPreferences, gson: Gson, tasks: List<Task>) {
      val jsonString = gson.toJson(tasks)
      preferences.edit().putString("tasks", jsonString).apply()
  }
  ```

- **Loading Tasks**:
  ```kotlin
  fun loadTasks(preferences: SharedPreferences, gson: Gson): List<Task> {
      val jsonString = preferences.getString("tasks", null)
      return if (jsonString != null) {
          val type = object : TypeToken<List<Task>>() {}.type
          gson.fromJson(jsonString, type)
      } else {
          emptyList()
      }
  }
  ```

## Contributing

Feel free to contribute to this project by submitting pull requests or issues.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE.txt) file for details.
