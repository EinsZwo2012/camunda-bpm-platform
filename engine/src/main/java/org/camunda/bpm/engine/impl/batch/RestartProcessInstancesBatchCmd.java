package org.camunda.bpm.engine.impl.batch;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.RestartProcessInstanceBuilderImpl;
import org.camunda.bpm.engine.impl.RestartProcessInstancesBatchConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;
import org.camunda.bpm.engine.impl.cmd.AbstractRestartProcessInstanceCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;

/**
 * 
 * @author Anna Pazola
 *
 */
public class RestartProcessInstancesBatchCmd extends AbstractRestartProcessInstanceCmd<Batch>{

  public RestartProcessInstancesBatchCmd(CommandExecutor commandExecutor, RestartProcessInstanceBuilderImpl builder) {
    super(commandExecutor, builder);
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    List<AbstractProcessInstanceModificationCommand> instructions = builder.getInstructions();
    Collection<String> processInstanceIds = collectProcessInstanceIds();

    ensureNotEmpty(BadUserRequestException.class, "Modification instructions cannot be empty", instructions);
    ensureNotEmpty(BadUserRequestException.class, "Process instance ids cannot be empty", "processInstanceIds", processInstanceIds);
    ensureNotContainsNull(BadUserRequestException.class, "Process instance ids cannot be null", "processInstanceIds", processInstanceIds);

    commandContext.getAuthorizationManager().checkAuthorization(Permissions.CREATE, Resources.BATCH);
    ProcessDefinitionEntity processDefinition;
    try {
       processDefinition = getProcessDefinition(commandContext, builder.getProcessDefinitionId());
    } catch (NullValueException e) {
      throw new BadUserRequestException(e.getMessage());
    }
    ensureNotNull(BadUserRequestException.class, "Process definition id cannot be null", processDefinition);

    writeUserOperationLog(commandContext, processDefinition, processInstanceIds.size(), true);

    ArrayList<String> ids = new ArrayList<String>();
    ids.addAll(processInstanceIds);
    BatchEntity batch = createBatch(commandContext, instructions, ids, processDefinition);
    batch.createSeedJobDefinition();
    batch.createMonitorJobDefinition();
    batch.createBatchJobDefinition();

    batch.fireHistoricStartEvent();

    batch.createSeedJob();
    return batch;

  }

  protected BatchEntity createBatch(CommandContext commandContext, List<AbstractProcessInstanceModificationCommand> instructions,
      List<String> processInstanceIds, ProcessDefinitionEntity processDefinition) {
    ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();
    BatchJobHandler<RestartProcessInstancesBatchConfiguration> batchJobHandler = getBatchJobHandler(processEngineConfiguration);

    RestartProcessInstancesBatchConfiguration configuration = new RestartProcessInstancesBatchConfiguration(processInstanceIds, instructions, builder.getProcessDefinitionId());

    BatchEntity batch = new BatchEntity();
    batch.setType(batchJobHandler.getType());
    batch.setTotalJobs(calculateSize(processEngineConfiguration, configuration));
    batch.setBatchJobsPerSeed(processEngineConfiguration.getBatchJobsPerSeed());
    batch.setInvocationsPerBatchJob(processEngineConfiguration.getInvocationsPerBatchJob());
    batch.setConfigurationBytes(batchJobHandler.writeConfiguration(configuration));
    batch.setTenantId(processDefinition.getTenantId());
    commandContext.getBatchManager().insert(batch);

    return batch;
  }

  protected int calculateSize(ProcessEngineConfigurationImpl engineConfiguration, RestartProcessInstancesBatchConfiguration batchConfiguration) {
    int invocationsPerBatchJob = engineConfiguration.getInvocationsPerBatchJob();
    int processInstanceCount = batchConfiguration.getIds().size();

    return (int) Math.ceil(processInstanceCount / invocationsPerBatchJob);
  }

  @SuppressWarnings("unchecked")
  protected BatchJobHandler<RestartProcessInstancesBatchConfiguration> getBatchJobHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
    Map<String, BatchJobHandler<?>> batchHandlers = processEngineConfiguration.getBatchHandlers();
    return (BatchJobHandler<RestartProcessInstancesBatchConfiguration>) batchHandlers.get(Batch.TYPE_PROCESS_INSTANCE_RESTART);
  }
}
