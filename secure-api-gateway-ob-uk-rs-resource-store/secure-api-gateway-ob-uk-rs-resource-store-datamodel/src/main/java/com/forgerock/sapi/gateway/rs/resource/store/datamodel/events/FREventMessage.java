/*
 * Copyright © 2020-2022 ForgeRock AS (obst@forgerock.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgerock.sapi.gateway.rs.resource.store.datamodel.events;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.event.FREventPollingError;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.org.openbanking.datamodel.event.OBEventNotification1;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(
        description = "The resource event."
)
@Validated
public class FREventMessage {

    @JsonProperty("events")
    private List<OBEventNotification1> obEventNotification1List = new ArrayList();
    private FREventPollingError errors;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ").append(this.getClass().getSimpleName()).append(" {\n");
        sb.append("    events: ").append(this.toIndentedString(this.obEventNotification1List)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
