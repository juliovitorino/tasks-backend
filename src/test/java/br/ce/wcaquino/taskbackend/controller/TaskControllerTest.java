package br.ce.wcaquino.taskbackend.controller;

import br.ce.wcaquino.taskbackend.model.Task;
import br.ce.wcaquino.taskbackend.repo.TaskRepo;
import br.ce.wcaquino.taskbackend.utils.ValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

public class TaskControllerTest {

    public static final String FAIL = "Nao deveria ter chegado neste ponto";

    @Mock
    private TaskRepo taskRepo;
    @InjectMocks
    private TaskController taskController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void naoDeveSalvarTarefaSemDescricao() {
        String expect = "Fill the task description";
        Task task = new Task();
        task.setDueDate(LocalDate.now());
        TaskController taskController = new TaskController();
        try {
            taskController.save(task);
            Assert.fail(FAIL);
        } catch (ValidationException e) {
            Assert.assertEquals(expect, e.getMessage());
        }

    }

    @Test
    public void naoDeveSalvarTarefaSemData() {
        String expect = "Fill the due date";
        Task task = new Task();
        task.setTask("Normal task");
        try {
            taskController.save(task);
            Assert.fail(FAIL);
        } catch (ValidationException e) {
            Assert.assertEquals(expect, e.getMessage());
        }

    }

    @Test
    public void naoDeveSalvarTarefaComDataPassada() {
        String expect = "Due date must not be in past";
        Task task = new Task();
        task.setTask("Normal task");
        task.setDueDate(LocalDate.of(2005,10,10));
        try {
            taskController.save(task);
            Assert.fail(FAIL);
        } catch (ValidationException e) {
            Assert.assertEquals(expect, e.getMessage());
        }

    }
    @Test
    public void deveSalvarTarefaComSucesso() throws ValidationException {
        String expect = "Due date must not be in past";
        Task task = new Task();
        task.setTask("Normal task");
        task.setDueDate(LocalDate.of(2025,10,10));
        taskController.save(task);

        Mockito.verify(taskRepo).save(task);
    }
}
