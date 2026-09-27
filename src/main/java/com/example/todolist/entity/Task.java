package com.example.todolist.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
@Table (name="tasks")
public class Task  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull (message = "Название не должно быть пустым")
    @Size(min = 5, max = 20, message = "Название должно иметь минимум 5 символов и не больше 20")
    private String title;
    @NotNull (message = "Описание не должно быть пустым")
    @Size(min = 5, max = 100, message = "Описание должно иметь хотя бы 5 букв")
    private String description;
    private String status;
    @Column(name = "completed", nullable = false, columnDefinition = "boolean default false")
    private Boolean completed;



    public Task() {
        this.completed = false;
    }

    public Task(Long id, String title, String description, String status, Boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.completed = completed != null && completed;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed != null && completed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", completed=" + completed +
                '}';
    }
}
