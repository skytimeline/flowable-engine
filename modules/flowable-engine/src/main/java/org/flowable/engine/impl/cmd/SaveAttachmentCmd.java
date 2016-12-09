/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flowable.engine.impl.cmd;

import java.io.Serializable;

import org.flowable.engine.compatibility.Activiti5CompatibilityHandler;
import org.flowable.engine.delegate.event.FlowableEngineEventType;
import org.flowable.engine.delegate.event.impl.FlowableEventBuilder;
import org.flowable.engine.impl.interceptor.Command;
import org.flowable.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.AttachmentEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.util.Activiti5Util;
import org.flowable.engine.task.Attachment;

/**
 * @author Tom Baeyens
 */
public class SaveAttachmentCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;
  protected Attachment attachment;

  public SaveAttachmentCmd(Attachment attachment) {
    this.attachment = attachment;
  }

  public Object execute(CommandContext commandContext) {
    AttachmentEntity updateAttachment = commandContext.getAttachmentEntityManager().findById(attachment.getId());

    String processInstanceId = updateAttachment.getProcessInstanceId();
    String processDefinitionId = null;
    if (updateAttachment.getProcessInstanceId() != null) {
      ExecutionEntity process = commandContext.getExecutionEntityManager().findById(processInstanceId);
      if (process != null) {
        processDefinitionId = process.getProcessDefinitionId();
        if (Activiti5Util.isActiviti5ProcessDefinitionId(commandContext, process.getProcessDefinitionId())) {
          Activiti5CompatibilityHandler activiti5CompatibilityHandler = Activiti5Util.getActiviti5CompatibilityHandler(); 
          activiti5CompatibilityHandler.saveAttachment(attachment);
          return null;
        }
      }
    }
    
    updateAttachment.setName(attachment.getName());
    updateAttachment.setDescription(attachment.getDescription());

    if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      commandContext.getProcessEngineConfiguration().getEventDispatcher()
          .dispatchEvent(FlowableEventBuilder.createEntityEvent(FlowableEngineEventType.ENTITY_UPDATED, attachment, processInstanceId, processInstanceId, processDefinitionId));
    }

    return null;
  }
}