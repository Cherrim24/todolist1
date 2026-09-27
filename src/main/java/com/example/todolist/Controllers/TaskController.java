package com.example.todolist.Controllers;

import com.example.todolist.entity.Task;
import com.example.todolist.servises.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Tag(name = "get", description = "GET-методы Task API")
    @Operation(summary = "Получить данные о задаче по id", description = "В ответе возвращается объект Task c полями id, title, description, status, completed.")
    @ApiResponse(responseCode = "200", description = "Задача найдена")
    @ApiResponse(responseCode = "404", description = "Задача не найдена")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTaskById(@Parameter(
            description = "ID задачи, данные по которой запрашиваются",
            required = true) @PathVariable Long id) {
        logger.info("GET /tasks/{} - Getting task by id", id);
        return ok("Task found", taskService.getTaskById(id));
    }

    @Tag(name = "get", description = "GET-методы Task API")
    @Operation(summary = "Получить данные о всех задачах", description = "В ответе возвращается массив Task.")
    @ApiResponse(responseCode = "200", description = "Список задач получен")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTasks() {
        logger.info("GET /tasks - Getting all tasks");
        return ok("Tasks found", taskService.getAllTasks());
    }

    @Tag(name = "post", description = "POST-метод Task API")
    @Operation(summary = "Создать новую задачу", description = "В ответе возвращается созданный объект Task.")
    @ApiResponse(responseCode = "201", description = "Задача создана")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTask(@Valid @RequestBody Task task) {
        logger.info("POST /tasks - Creating new task");
        Task savedTask = taskService.saveTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(body("Task created successfully", savedTask));
    }

    @Tag(name = "put", description = "PUT-метод Task API")
    @Operation(summary = "Изменить данные о задаче", description = "В ответе возвращается измененный объект Task.")
    @ApiResponse(responseCode = "200", description = "Задача изменена")
    @ApiResponse(responseCode = "404", description = "Задача не найдена")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTask(
            @Parameter(
                    description = "ID задачи, данные по которой нужно изменить",
                    required = true) @PathVariable Long id,
            @Valid @RequestBody Task taskDetails) {
        logger.info("PUT /tasks/{} - Updating task", id);
        return ok("Task updated successfully", taskService.updateTask(id, taskDetails));
    }

    @Tag(name = "delete", description = "DELETE-метод Task API")
    @Operation(summary = "Удалить задачу", description = "В ответе возвращается сообщение о том, что задача удалена.")
    @ApiResponse(responseCode = "200", description = "Задача успешно удалена")
    @ApiResponse(responseCode = "404", description = "Задача не найдена")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTask(@Parameter(
            description = "ID задачи, которую нужно удалить",
            required = true) @PathVariable Long id) {
        logger.info("DELETE /tasks/{} - Deleting task", id);
        taskService.deleteTask(id);
        return ok("Task deleted successfully", Map.of("deletedId", id));
    }

    private ResponseEntity<Map<String, Object>> ok(String message, Object data) {
        return ResponseEntity.ok(body(message, data));
    }

    private Map<String, Object> body(String message, Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }
}
