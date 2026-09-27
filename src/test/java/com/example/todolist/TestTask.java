package com.example.todolist;
import com.example.todolist.entity.Task;
import com.example.todolist.repository.TaskRepository;
import com.example.todolist.servises.TaskService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestTask {
    @Mock
    private TaskRepository taskrepository;
    @InjectMocks
    private TaskService taskservice;

    @Test
    void testGetByIdFound() {
        // Given
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setCompleted(false);

        when(taskrepository.findById(1L)).thenReturn(Optional.of(task));

        // When
        Task result = taskservice.getTaskById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Task", result.getTitle());
        assertEquals("Test Description", result.getDescription());

        verify(taskrepository, times(1)).findById(1L);
    }



    @Test
    public void should_get_all_tasks() {

        List<Task> tasks = getTask();
        when(taskrepository.findAll()).thenReturn(tasks);
        List<Task> result = taskservice.getAllTasks();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
    }

    @Test
    public void should_save_task() {
        Task task = new Task();
        task.setId(3L);
        task.setTitle("title");
        task.setDescription("description");
        task.setCompleted(true);

        when(taskrepository.save(any(Task.class))).thenReturn(task);

        Task result = taskservice.saveTask(task);

        Assertions.assertNotNull(result);
        assertEquals("title", result.getTitle());
        verify(taskrepository, times(1)).save(task);
    }

    @Test
    public void should_throw_exception_when_task_not_found() {
        when(taskrepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, () -> taskservice.getTaskById(1L));

        assertEquals("Task with id 1 not found", exception.getMessage());
    }
    @Test
    public void should_delete_task() {
        Long taskId = 1L;
        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setTitle("Task to Delete");

        when(taskrepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // When
        taskservice.deleteTask(taskId);

        // Then
        verify(taskrepository, times(1)).findById(taskId);
        verify(taskrepository, times(1)).delete(existingTask);
    }

    @Test
    void testUpdate() {
        // Given
        Long taskId = 1L;

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setTitle("Original Title");
        existingTask.setDescription("Original Description");
        existingTask.setCompleted(false);


        Task updateData = new Task();
        updateData.setTitle("Updated Title");
        updateData.setDescription("Updated Description");
        updateData.setCompleted(true);

        Task updatedTask = new Task();
        updatedTask.setId(taskId);
        updatedTask.setTitle("Updated Title");
        updatedTask.setDescription("Updated Description");
        updatedTask.setCompleted(true);


        when(taskrepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskrepository.save(any(Task.class))).thenReturn(updatedTask);

        // When
        Task result = taskservice.updateTask(updatedTask.getId(), updateData);

        // Then
        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertTrue(updatedTask.getCompleted());
        verify(taskrepository, times(1)).findById(taskId);
        verify(taskrepository, times(1)).save(existingTask);
    }

    private List<Task> getTask(){

        Task task2 = new Task();
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("title");
        task1.setDescription("description");
        task1.setCompleted(true);

        task2.setId(2L);
        task2.setTitle("title2");
        task2.setDescription("description2");
        task2.setCompleted(false);

        return List.of(task1,task2);
    }
}
