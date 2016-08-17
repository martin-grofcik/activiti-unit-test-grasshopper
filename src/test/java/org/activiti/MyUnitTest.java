package org.activiti;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MyUnitTest {

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();

	@Test
	@Deployment(resources = {"org/activiti/test/TestSendTo.bpmn20.xml"})
	public void testFromReceiveTask1ToUserTask1() {

		ProcessInstance processInstance = getProcessInstanceToReceiveTask();

		ProcessEngineConfigurationImpl processEngineConfigurationImpl = (ProcessEngineConfigurationImpl) activitiRule.getProcessEngine().getProcessEngineConfiguration();

		CommandExecutor commandExecutor = processEngineConfigurationImpl.getCommandExecutor();
		commandExecutor.execute(new RestartInstanceActivitiCommand(processInstance.getId(), "receivetask0"));
		this.activitiRule.getRuntimeService().signal(processInstance.getId());

		Task task = this.activitiRule.getTaskService().createTaskQuery().singleResult();
		assertEquals("User Task 1", task.getName());
	}

	@Test
	@Deployment(resources = {"org/activiti/test/TestSendTo.bpmn20.xml"})
	public void testFromUserTask2ToUserTask1() {

		ProcessInstance processInstance = getProcessInstanceToUserTask2();

		ProcessEngineConfigurationImpl processEngineConfigurationImpl = (ProcessEngineConfigurationImpl) activitiRule.getProcessEngine().getProcessEngineConfiguration();

		Task userTask2 = this.activitiRule.getTaskService().createTaskQuery().singleResult();
		CommandExecutor commandExecutor = processEngineConfigurationImpl.getCommandExecutor();
		// we have to clean up side efects manually
		commandExecutor.execute(new DeleteTaskWithoutCheckCommand((TaskEntity) userTask2));
		commandExecutor.execute(new RestartInstanceActivitiCommand(processInstance.getId(), "receivetask0"));

		this.activitiRule.getRuntimeService().signal(processInstance.getId());

		Task task = this.activitiRule.getTaskService().createTaskQuery().singleResult();
		assertEquals("User Task 1", task.getName());
	}

	@Test
	@Deployment(resources = {"org/activiti/test/TestSendTo.bpmn20.xml"})
	public void testFromUserTask2ToUserTask1WithConditionChange() {

		ProcessInstance processInstance = getProcessInstanceToUserTask2();

		ProcessEngineConfigurationImpl processEngineConfigurationImpl = (ProcessEngineConfigurationImpl) activitiRule.getProcessEngine().getProcessEngineConfiguration();

		// changing conditions
		this.activitiRule.getRuntimeService().setVariable(processInstance.getId(), "goToUserTask", false);

		Task userTask2 = this.activitiRule.getTaskService().createTaskQuery().singleResult();
		CommandExecutor commandExecutor = processEngineConfigurationImpl.getCommandExecutor();
		// we have to clean up side efects manually
		commandExecutor.execute(new DeleteTaskWithoutCheckCommand((TaskEntity) userTask2));
		commandExecutor.execute(new RestartInstanceActivitiCommand(processInstance.getId(), "receivetask0"));

		this.activitiRule.getRuntimeService().signal(processInstance.getId());

		assertThat("Process instance is finished. (Bypassing all wait states)",
				this.activitiRule.getRuntimeService().createProcessInstanceQuery().count(), is(0L));
	}

	@Test
	@Deployment(resources = {"org/activiti/test/TestSendTo.bpmn20.xml"})
	public void testFromReceiveTask1ToUserTask2() {

		ProcessInstance processInstance = getProcessInstanceToReceiveTask();

		ProcessEngineConfigurationImpl processEngineConfigurationImpl = (ProcessEngineConfigurationImpl) activitiRule.getProcessEngine().getProcessEngineConfiguration();

		CommandExecutor commandExecutor = processEngineConfigurationImpl.getCommandExecutor();
		commandExecutor.execute(new RestartInstanceActivitiCommand(processInstance.getId(), "usertask1"));
		this.activitiRule.getRuntimeService().signal(processInstance.getId());

		Task task = this.activitiRule.getTaskService().createTaskQuery().singleResult();
		assertEquals("User Task 2", task.getName());
	}

	private ProcessInstance getProcessInstanceToReceiveTask() {
		ProcessInstance processInstance = getProcessInstanceToUserTask2();

		Task userTask2 = this.activitiRule.getTaskService().createTaskQuery().singleResult();
		this.activitiRule.getTaskService().complete(userTask2.getId());

		processInstance = this.activitiRule.getRuntimeService().createProcessInstanceQuery().
				processInstanceId(processInstance.getProcessInstanceId()).singleResult();

		assertEquals("receivetask1", processInstance.getActivityId());
		return processInstance;
	}

	private ProcessInstance getProcessInstanceToUserTask2() {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("goToUserTask", true);

		// start process
		ProcessInstance processInstance = this.activitiRule.getRuntimeService().startProcessInstanceByKey("TestSendTo", variables);
		assertNotNull(processInstance);

		// pass receiveTask0
		this.activitiRule.getRuntimeService().signal(processInstance.getId());

		Task task = this.activitiRule.getTaskService().createTaskQuery().singleResult();
		assertEquals("User Task 1", task.getName());

		this.activitiRule.getTaskService().complete(task.getId());

		processInstance = this.activitiRule.getRuntimeService().createProcessInstanceQuery().
							processInstanceId(processInstance.getProcessInstanceId()).singleResult();

		task = this.activitiRule.getTaskService().createTaskQuery().singleResult();
		assertEquals("User Task 2", task.getName());
		return processInstance;
	}

}
