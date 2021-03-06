package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.modification.ProcessInstanceModificationInstructionDto;
import org.camunda.bpm.engine.runtime.RestartProcessInstanceBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author Anna Pazola
 *
 */
public class RestartProcessInstanceDto {

  protected List<String> processInstanceIds;
  protected List<ProcessInstanceModificationInstructionDto> instructions;
  protected String processDefinitionId;
  protected HistoricProcessInstanceQueryDto historicProcessInstanceQuery;

  public List<String> getProcessInstanceIds() {
    return processInstanceIds;
  }

  public void setProcessInstanceIds(List<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
  }

  public List<ProcessInstanceModificationInstructionDto> getInstructions() {
    return instructions;
  }

  public void setInstructions(List<ProcessInstanceModificationInstructionDto> instructions) {
    this.instructions = instructions;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public HistoricProcessInstanceQueryDto getHistoricProcessInstanceQuery() {
    return historicProcessInstanceQuery;
  }

  public void setHistoricProcessInstanceQuery(HistoricProcessInstanceQueryDto historicProcessInstanceQuery) {
    this.historicProcessInstanceQuery = historicProcessInstanceQuery;
  }

  public void applyTo(RestartProcessInstanceBuilder builder, ProcessEngine processEngine, ObjectMapper objectMapper) {
    for (ProcessInstanceModificationInstructionDto instruction : instructions) {

      instruction.applyTo(builder, processEngine, objectMapper);
    }
  }
}
