package com.example.todolist;

import com.example.todolist.Controllers.TaskController;
import com.example.todolist.entity.Task;
import com.example.todolist.servises.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private Task task(Long id, String title, boolean completed) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription("Description of " + title);
        task.setStatus("NEW");
        task.setCompleted(completed);
        return task;
    }

    @Test
    void getAllTasksReturnsEnvelopeWithTaskList() throws Exception {
        when(taskService.getAllTasks()).thenReturn(List.of(task(1L, "First task", false), task(2L, "Second task", true)));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tasks found"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("First task"))
                .andExpect(jsonPath("$.data[1].completed").value(true));
    }

    @Test
    void getTaskByIdReturnsTask() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(task(1L, "First task", false));

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("First task"))
                .andExpect(jsonPath("$.data.completed").value(false));
    }

    @Test
    void getTaskByIdReturns404WhenTaskDoesNotExist() throws Exception {
        when(taskService.getTaskById(99L))
                .thenThrow(new ResourceNotFoundException("Task with id 99 not found"));

        mockMvc.perform(get("/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Task with id 99 not found"));
    }

    @Test
    void createTaskReturns201WithSavedTask() throws Exception {
        when(taskService.saveTask(any(Task.class))).thenReturn(task(1L, "Created task", false));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Created task",
                                    "description": "Description of Created task",
                                    "status": "NEW",
                                    "completed": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Created task"));
    }

    @Test
    void createTaskReturns400WhenValidationFails() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Bad",
                                    "description": "no"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.description").exists());

        verify(taskService, org.mockito.Mockito.never()).saveTask(any(Task.class));
    }

    @Test
    void createTaskDefaultsCompletedToFalseWhenFieldIsMissing() throws Exception {
        when(taskService.saveTask(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Task without flag",
                                    "description": "Field completed is missing"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.completed").value(false));
    }

    @Test
    void updateTaskReturnsUpdatedTask() throws Exception {
        when(taskService.updateTask(eq(1L), any(Task.class))).thenReturn(task(1L, "Updated task", true));

        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Updated task",
                                    "description": "Description of Updated task",
                                    "status": "DONE",
                                    "completed": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task updated successfully"))
                .andExpect(jsonPath("$.data.title").value("Updated task"))
                .andExpect(jsonPath("$.data.completed").value(true));
    }

    @Test
    void deleteTaskReturns200AndDeletedId() throws Exception {
        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task deleted successfully"))
                .andExpect(jsonPath("$.data.deletedId").value(1));

        verify(taskService).deleteTask(1L);
    }

    @Test
    void deleteTaskReturns404WhenTaskDoesNotExist() throws Exception {
        doThrow(new ResourceNotFoundException("Task with id 99 not found")).when(taskService).deleteTask(99L);

        mockMvc.perform(delete("/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Task with id 99 not found"));
    }

    @Test
    void getTaskByIdReturns400WhenIdIsNotNumeric() throws Exception {
        mockMvc.perform(get("/tasks/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
