package com.example.todolist.servises;

import com.example.todolist.ResourceNotFoundException;
import com.example.todolist.repository.TaskRepository;
import com.example.todolist.entity.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TaskService {



    private final TaskRepository taskRepository;
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
    }

    public Task saveTask(Task task) {
       return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
       Task task = getTaskById(id);
       taskRepository.delete(task);
    }

    public Task updateTask(Long id, Task newTask) {
        Task existing = getTaskById(id);

        existing.setTitle(newTask.getTitle());
        existing.setDescription(newTask.getDescription());
        existing.setStatus(newTask.getStatus());
        existing.setCompleted(newTask.getCompleted());
        return taskRepository.save(existing);
    }



}
